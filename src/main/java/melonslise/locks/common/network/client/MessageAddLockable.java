package melonslise.locks.common.network.client;

import io.netty.buffer.ByteBuf;
import melonslise.locks.common.world.storage.Lockable;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAddLockable implements IMessage
{
	private Lockable lockable;

	public MessageAddLockable() {}

	public MessageAddLockable(Lockable lockable)
	{
		this.lockable = lockable;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.lockable = LocksUtilities.readLockable(buffer);
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		LocksUtilities.writeLockable(buffer, this.lockable);
	}



	public static class Handler implements IMessageHandler<MessageAddLockable, IMessage>
	{
		@Override
		public IMessage onMessage(MessageAddLockable message, MessageContext context)
		{
			Minecraft mc = Minecraft.getMinecraft();
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					StorageLockables.get(mc.world).add(message.lockable);
				}
			};
			mc.addScheduledTask(runnable);
			return null;
		}
	}
}