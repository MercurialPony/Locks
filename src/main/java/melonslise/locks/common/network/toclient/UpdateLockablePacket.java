package melonslise.locks.common.network.toclient;

import java.util.function.Supplier;

import melonslise.locks.Locks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class UpdateLockablePacket
{
	// Expandable
	private final int networkID;
	private final boolean locked;

	public UpdateLockablePacket(int networkID, boolean locked)
	{
		this.networkID = networkID;
		this.locked = locked;
	}

	public static UpdateLockablePacket decode(PacketBuffer buf)
	{
		return new UpdateLockablePacket(buf.readInt(), buf.readBoolean());
	}

	public static void encode(UpdateLockablePacket pkt, PacketBuffer buf)
	{
		buf.writeInt(pkt.networkID);
		buf.writeBoolean(pkt.locked);
	}

	public static void handle(UpdateLockablePacket pkt, Supplier<NetworkEvent.Context> ctx)
	{
		// Use runnable, lambda causes issues with classloading
		ctx.get().enqueueWork(new Runnable()
		{
			@Override
			public void run()
			{
				Locks.PROXY.getLockables(Minecraft.getInstance().world).ifPresent(lockables -> lockables.get().get(pkt.networkID).lock.setLocked(pkt.locked));
			}
		});
		ctx.get().setPacketHandled(true);
	}
}