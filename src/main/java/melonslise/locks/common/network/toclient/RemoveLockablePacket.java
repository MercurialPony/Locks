package melonslise.locks.common.network.toclient;

import io.netty.buffer.ByteBuf;
import melonslise.locks.common.init.LocksCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RemoveLockablePacket implements IMessage
{
	private int networkID;

	public RemoveLockablePacket() {}

	public RemoveLockablePacket(int networkID)
	{
		this.networkID = networkID;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.networkID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.networkID);
	}

	public static class Handler implements IMessageHandler<RemoveLockablePacket, IMessage>
	{
		@Override
		public IMessage onMessage(RemoveLockablePacket pkt, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			mc.addScheduledTask(() -> mc.world.getCapability(LocksCapabilities.LOCKABLES, null).remove(pkt.networkID));
			return null;
		}
	}
}