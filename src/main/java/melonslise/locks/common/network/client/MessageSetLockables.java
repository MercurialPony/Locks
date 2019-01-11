package melonslise.locks.common.network.client;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import melonslise.locks.common.world.storage.Lockable;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSetLockables implements IMessage
{
	private ArrayList<Lockable> lockables;

	public MessageSetLockables() {}

	public MessageSetLockables(ArrayList<Lockable> lockables)
	{
		this.lockables = lockables;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.lockables = Lists.newArrayList();
		int size = buffer.readInt();
		for(int a = 0; a < size; ++a) this.lockables.add(LocksUtilities.readLockable(buffer));
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeInt(this.lockables.size());
		for(Lockable lockable : this.lockables) LocksUtilities.writeLockable(buffer, lockable);
	}



	public static class Handler implements IMessageHandler<MessageSetLockables, IMessage>
	{
		@Override
		public IMessage onMessage(MessageSetLockables message, MessageContext context)
		{
			Minecraft mc = Minecraft.getMinecraft();
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					StorageLockables.get(mc.world).setLockables(message.lockables);
				}
			};
			mc.addScheduledTask(runnable);
			return null;
		}
	}
}