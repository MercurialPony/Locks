package melonslise.locks.common.network.client;

import io.netty.buffer.ByteBuf;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageToggleLockables implements IMessage
{
	private Box box;

	public MessageToggleLockables() {}

	public MessageToggleLockables(Box box)
	{
		this.box = box;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.box = LocksUtilities.readBox(buffer);
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		LocksUtilities.writeBox(buffer, this.box);
	}



	public static class Handler implements IMessageHandler<MessageToggleLockables, IMessage>
	{
		@Override
		public IMessage onMessage(MessageToggleLockables message, MessageContext context)
		{
			Minecraft mc = Minecraft.getMinecraft();
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					StorageLockables.get(mc.world).toggle(message.box);
				}
			};
			mc.addScheduledTask(runnable);
			return null;
		}
	}
}