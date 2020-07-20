package melonslise.locks.common.network.toclient;

import java.util.function.Supplier;

import melonslise.locks.Locks;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class AddLockablePacket
{
	private final Lockable lockable;

	public AddLockablePacket(Lockable lockable)
	{
		this.lockable = lockable;
	}

	public static AddLockablePacket decode(PacketBuffer buf)
	{
		return new AddLockablePacket(LocksUtil.readLockableFromBuffer(buf));
	}

	public static void encode(AddLockablePacket pkt, PacketBuffer buf)
	{
		LocksUtil.writeLockableToBuffer(buf, pkt.lockable);
	}

	public static void handle(AddLockablePacket pkt, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> Locks.PROXY.getLockables(Minecraft.getInstance().world).ifPresent(capability -> capability.add(pkt.lockable)));
		ctx.get().setPacketHandled(true);
	}
}