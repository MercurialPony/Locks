package melonslise.locks.common.network.client;

import io.netty.buffer.ByteBuf;
import melonslise.locks.client.gui.GuiLockPicking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCheckPinResult implements IMessage
{
	private boolean correct, reset;

	public MessageCheckPinResult() {}

	public MessageCheckPinResult(boolean correct, boolean reset)
	{
		this.correct = correct;
		this.reset = reset;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.correct = buffer.readBoolean();
		this.reset = buffer.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{

		buffer.writeBoolean(this.correct);
		buffer.writeBoolean(this.reset);
	}



	public static class Handler implements IMessageHandler<MessageCheckPinResult, IMessage>
	{
		@Override
		public IMessage onMessage(MessageCheckPinResult message, MessageContext context)
		{
			Minecraft mc = Minecraft.getMinecraft();
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					GuiScreen gui = mc.currentScreen;
					if(!(gui instanceof GuiLockPicking)) return;
					GuiLockPicking lockPicking = (GuiLockPicking) gui;
					lockPicking.handlePin(message.correct);
					if(message.reset) lockPicking.reset();
				}
			};
			mc.addScheduledTask(runnable);
			return null;
		}
	}
}