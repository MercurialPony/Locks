package melonslise.locks.common.network.toClient;

import java.util.function.Supplier;

import melonslise.locks.common.init.LocksCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketLockableRemove
{
	private final int networkID;

	public PacketLockableRemove(int networkID)
	{
		this.networkID = networkID;
	}

	public static PacketLockableRemove decode(PacketBuffer buffer)
	{
		return new PacketLockableRemove(buffer.readInt());
	}

	public static void encode(PacketLockableRemove packet, PacketBuffer buffer)
	{
		buffer.writeInt(packet.networkID);
	}

	public static void handle(PacketLockableRemove packet, Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> Minecraft.getInstance().world.getCapability(LocksCapabilities.LOCKABLES).ifPresent(capability -> capability.remove(packet.networkID)));
		context.get().setPacketHandled(true);
	}
}