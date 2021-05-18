package melonslise.locks.common.network.toclient;

import java.util.function.Supplier;

import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksContainerTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TryPinResultPacket
{
	private final boolean correct, reset;

	public TryPinResultPacket(boolean correct, boolean reset)
	{
		this.correct = correct;
		this.reset = reset;
	}

	public static TryPinResultPacket decode(PacketBuffer buf)
	{
		return new TryPinResultPacket(buf.readBoolean(), buf.readBoolean());
	}

	public static void encode(TryPinResultPacket pkt, PacketBuffer buf)
	{
		buf.writeBoolean(pkt.correct);
		buf.writeBoolean(pkt.reset);
	}

	public static void handle(TryPinResultPacket pkt, Supplier<NetworkEvent.Context> ctx)
	{
		// Use runnable, lambda causes issues with class loading
		ctx.get().enqueueWork(new Runnable()
		{
			@Override
			public void run()
			{
				Container container = Minecraft.getInstance().player.containerMenu;
				if(container.getType() == LocksContainerTypes.LOCK_PICKING.get())
					((LockPickingContainer) container).handlePin(pkt.correct, pkt.reset);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}