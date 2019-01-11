package melonslise.locks.common.network.client;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageToggleLockablesByID implements IMessage
{
	private Box box;
	private UUID id;
	
	public MessageToggleLockablesByID() {}

	public MessageToggleLockablesByID(Box box, UUID id)
	{
		this.box = box;
		this.id = id;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.box = LocksUtilities.readBox(buffer);
		this.id = LocksUtilities.readUUID(buffer);
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		LocksUtilities.writeBox(buffer, this.box);
		LocksUtilities.writeUUID(buffer, this.id);
	}



	public static class Handler implements IMessageHandler<MessageToggleLockablesByID, IMessage>
	{
		@Override
		public IMessage onMessage(MessageToggleLockablesByID message, MessageContext context)
		{
			Minecraft mc = Minecraft.getMinecraft();
			Runnable  runnable = new Runnable()
			{
				@Override
				public void run()
				{
					StorageLockables.get(mc.world).toggle(message.box, message.id);
				}
			};
			mc.addScheduledTask(runnable);
			return null;
		}
	}
}