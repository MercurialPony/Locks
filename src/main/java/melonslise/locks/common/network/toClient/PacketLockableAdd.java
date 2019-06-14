package melonslise.locks.common.network.toClient;

import java.util.function.Supplier;

import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.utility.Lockable;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketLockableAdd
{
	private final Lockable lockable;

	public PacketLockableAdd(Lockable lockable)
	{
		this.lockable = lockable;
	}

	public static PacketLockableAdd decode(PacketBuffer buffer)
	{
		return new PacketLockableAdd(LocksUtilities.readLockableFromBuffer(buffer));
	}

	public static void encode(PacketLockableAdd packet, PacketBuffer buffer)
	{
		LocksUtilities.writeLockableToBuffer(buffer, packet.lockable);
	}

	public static void handle(PacketLockableAdd packet, Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> Minecraft.getInstance().world.getCapability(LocksCapabilities.LOCKABLES).ifPresent(capability -> capability.add(packet.lockable)));
		context.get().setPacketHandled(true);
	}
}