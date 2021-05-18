package melonslise.locks.common.network.toserver;

import java.util.function.Supplier;

import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksContainerTypes;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TryPinPacket
{
	private final byte pin;

	public TryPinPacket(byte pin)
	{
		this.pin = pin;
	}

	public static TryPinPacket decode(PacketBuffer buf)
	{
		return new TryPinPacket(buf.readByte());
	}

	public static void encode(TryPinPacket pkt, PacketBuffer buf)
	{
		buf.writeByte(pkt.pin);
	}

	public static void handle(TryPinPacket pkt, Supplier<NetworkEvent.Context> ctx)
	{
		// Use runnable, lambda causes issues with class loading
		ctx.get().enqueueWork(new Runnable()
		{
			@Override
			public void run()
			{
				Container container = ctx.get().getSender().containerMenu;
				if(container.getType() == LocksContainerTypes.LOCK_PICKING.get())
					((LockPickingContainer) container).tryPin(pkt.pin);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}