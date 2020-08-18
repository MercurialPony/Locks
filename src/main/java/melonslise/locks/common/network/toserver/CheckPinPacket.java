package melonslise.locks.common.network.toserver;

import java.util.function.Supplier;

import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksContainerTypes;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class CheckPinPacket
{
	private final byte pin;

	public CheckPinPacket(byte pin)
	{
		this.pin = pin;
	}

	public static CheckPinPacket decode(PacketBuffer buf)
	{
		return new CheckPinPacket(buf.readByte());
	}

	public static void encode(CheckPinPacket pkt, PacketBuffer buf)
	{
		buf.writeByte(pkt.pin);
	}

	public static void handle(CheckPinPacket pkt, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			Container container = ctx.get().getSender().openContainer;
			if(container.getType() == LocksContainerTypes.LOCK_PICKING.get())
				((LockPickingContainer) container).checkPin(pkt.pin);
		});
		ctx.get().setPacketHandled(true);
	}
}