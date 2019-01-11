package melonslise.locks.common.network.server;

import io.netty.buffer.ByteBuf;
import melonslise.locks.common.container.ContainerLockPicking;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCheckPin implements IMessage
{
	private int pin;

	public MessageCheckPin() {}

	public MessageCheckPin(int pin)
	{
		this.pin = pin;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.pin = (int) buffer.readByte();
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeByte((byte) this.pin);
	}



	public static class Handler implements IMessageHandler<MessageCheckPin, IMessage>
	{
		@Override
		public IMessage onMessage(MessageCheckPin message, MessageContext context)
		{
			EntityPlayerMP player = context.getServerHandler().player;
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					Container container = player.openContainer;
					if(container instanceof ContainerLockPicking) ((ContainerLockPicking) container).checkPin(message.pin);
				}
			};
			player.getServer().addScheduledTask(runnable);
			return null;
		}
	}
}