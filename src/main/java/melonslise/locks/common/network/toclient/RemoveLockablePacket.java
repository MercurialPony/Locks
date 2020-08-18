package melonslise.locks.common.network.toclient;

import java.util.function.Supplier;

import melonslise.locks.common.init.LocksCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class RemoveLockablePacket
{
	private final int networkID;

	public RemoveLockablePacket(int networkID)
	{
		this.networkID = networkID;
	}

	public static RemoveLockablePacket decode(PacketBuffer buf)
	{
		return new RemoveLockablePacket(buf.readInt());
	}

	public static void encode(RemoveLockablePacket pkt, PacketBuffer buf)
	{
		buf.writeInt(pkt.networkID);
	}

	public static void handle(RemoveLockablePacket pkt, Supplier<NetworkEvent.Context> ctx)
	{
		// Use runnable, lambda causes issues with classloading
		ctx.get().enqueueWork(new Runnable()
		{
			@Override
			public void run()
			{
				Minecraft.getInstance().world.getCapability(LocksCapabilities.LOCKABLES).ifPresent(lockables -> lockables.remove(pkt.networkID));
			}
		});
		ctx.get().setPacketHandled(true);
	}
}