package melonslise.locks.common.network.toClient;

import java.util.function.Supplier;

import melonslise.locks.common.init.LocksCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketLockableStatus
{
	// Expandable
	private final int networkID;
	private final boolean locked;

	public PacketLockableStatus(int networkID, boolean locked)
	{
		this.networkID = networkID;
		this.locked = locked;
	}

	public static PacketLockableStatus decode(PacketBuffer buffer)
	{
		return new PacketLockableStatus(buffer.readInt(), buffer.readBoolean());
	}

	public static void encode(PacketLockableStatus packet, PacketBuffer buffer)
	{
		buffer.writeInt(packet.networkID);
		buffer.writeBoolean(packet.locked);
	}

	public static void handle(PacketLockableStatus packet, Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> Minecraft.getInstance().world.getCapability(LocksCapabilities.LOCKABLES).ifPresent(capability -> capability.getLockables().get(packet.networkID).lock.setLocked(packet.locked)));
		context.get().setPacketHandled(true);
	}
}