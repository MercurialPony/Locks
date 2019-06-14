package melonslise.locks.common.network.toServer;

import java.util.function.Supplier;

import melonslise.locks.common.container.ContainerLockPicking;
import melonslise.locks.common.init.LocksContainerTypes;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketCheckPin
{
	private final byte pin;

	public PacketCheckPin(byte pin)
	{
		this.pin = pin;
	}

	public static PacketCheckPin decode(PacketBuffer buffer)
	{
		return new PacketCheckPin(buffer.readByte());
	}

	public static void encode(PacketCheckPin packet, PacketBuffer buffer)
	{
		buffer.writeByte(packet.pin);
	}

	public static void handle(PacketCheckPin packet, Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() ->
		{
			Container container = context.get().getSender().openContainer;
			if(container.getType() == LocksContainerTypes.LOCK_PICKING) ((ContainerLockPicking) container).checkPin(packet.pin);
		});
		context.get().setPacketHandled(true);
	}
}