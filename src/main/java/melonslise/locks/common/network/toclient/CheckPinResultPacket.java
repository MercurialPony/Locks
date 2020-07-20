package melonslise.locks.common.network.toclient;

import io.netty.buffer.ByteBuf;
import melonslise.locks.common.container.LockPickingContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CheckPinResultPacket implements IMessage
{
	private boolean correct, reset;

	public CheckPinResultPacket() {}

	public CheckPinResultPacket(boolean correct, boolean reset)
	{
		this.correct = correct;
		this.reset = reset;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.correct = buf.readBoolean();
		this.reset = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(this.correct);
		buf.writeBoolean(this.reset);
	}

	public static class Handler implements IMessageHandler<CheckPinResultPacket, IMessage>
	{
		@Override
		public IMessage onMessage(CheckPinResultPacket pkt, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			mc.addScheduledTask(() ->
			{
				Container container = mc.player.openContainer;
				if(container instanceof LockPickingContainer)
					((LockPickingContainer) container).handlePin(pkt.correct, pkt.reset);
			});
			return null;
		}
	}
}