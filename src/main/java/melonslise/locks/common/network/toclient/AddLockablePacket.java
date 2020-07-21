package melonslise.locks.common.network.toclient;

import io.netty.buffer.ByteBuf;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AddLockablePacket implements IMessage
{
	private Lockable lockable;

	public AddLockablePacket() {}

	public AddLockablePacket(Lockable lockable)
	{
		this.lockable = lockable;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.lockable = LocksUtil.readLockableFromBuffer(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		LocksUtil.writeLockableToBuffer(buf, this.lockable);
	}

	public static class Handler implements IMessageHandler<AddLockablePacket, IMessage>
	{
		@Override
		public IMessage onMessage(AddLockablePacket pkt, MessageContext ctx)
		{
			// Use runnable, lambda causes classloading issues
			Minecraft mc = Minecraft.getMinecraft();
			mc.addScheduledTask(new Runnable()
			{
				@Override
				public void run()
				{
					mc.world.getCapability(LocksCapabilities.LOCKABLES, null).add(pkt.lockable);
				}
			});
			return null;
		}
	}
}