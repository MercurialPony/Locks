package melonslise.locks.common.network.client;

import io.netty.buffer.ByteBuf;
import melonslise.locks.client.gui.GuiKeyRing;
import melonslise.locks.common.container.ContainerKeyRing;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageKeyRing implements IMessage
{
	private int id;
	private EnumHand hand;

	public MessageKeyRing() {}

	public MessageKeyRing(ContainerKeyRing container, EnumHand hand)
	{
		this.id = container.windowId;
		this.hand = hand;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.id = buffer.readInt();
		this.hand = LocksUtilities.readEnum(buffer, EnumHand.class);
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeInt(this.id);
		LocksUtilities.writeEnum(buffer, this.hand);
	}



	public static class Handler implements IMessageHandler<MessageKeyRing, IMessage>
	{
		@Override
		public IMessage onMessage(MessageKeyRing message, MessageContext context)
		{
			Minecraft mc = Minecraft.getMinecraft();
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					ContainerKeyRing container = new ContainerKeyRing(mc.player, mc.player.getHeldItem(message.hand));
					container.windowId = message.id;
					mc.displayGuiScreen(new GuiKeyRing(container));
				}
			};
			mc.addScheduledTask(runnable);
			return null;
		}
	}
}