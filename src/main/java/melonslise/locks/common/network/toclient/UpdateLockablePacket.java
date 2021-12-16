package melonslise.locks.common.network.toclient;

import io.netty.buffer.ByteBuf;
import melonslise.locks.common.init.LocksCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateLockablePacket implements IMessage
{
	// Expandable
	private int networkID;
	private boolean locked;

	public UpdateLockablePacket() {}

	public UpdateLockablePacket(int networkID, boolean locked)
	{
		this.networkID = networkID;
		this.locked = locked;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.networkID = buf.readInt();
		this.locked = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.networkID);
		buf.writeBoolean(this.locked);
	}

	public static class Handler implements IMessageHandler<UpdateLockablePacket, IMessage>
	{
		@Override
		public IMessage onMessage(UpdateLockablePacket pkt, MessageContext ctx)
		{
			// Use runnable, lambda causes classloading issues
			Minecraft mc = Minecraft.getMinecraft();
			mc.addScheduledTask(new Runnable()
			{
				@Override
				public void run()
				{
					mc.world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null).getLoaded().get(pkt.networkID).lock.setLocked(pkt.locked);
				}
			});
			return null;
		}
	}
}