package melonslise.locks.common.network.toclient;

import java.util.function.Supplier;

import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Lockable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class AddLockablePacket
{
	private final Lockable lockable;

	public AddLockablePacket(Lockable lkb)
	{
		this.lockable = lkb;
	}

	public static AddLockablePacket decode(PacketBuffer buf)
	{
		return new AddLockablePacket(Lockable.fromBuf(buf));
	}

	public static void encode(AddLockablePacket pkt, PacketBuffer buf)
	{
		Lockable.toBuf(buf, pkt.lockable);
	}

	public static void handle(AddLockablePacket pkt, Supplier<NetworkEvent.Context> ctx)
	{
		// Use runnable, lambda causes issues with class loading
		ctx.get().enqueueWork(new Runnable()
		{
			@Override
			public void run()
			{
				Minecraft.getInstance().level.getCapability(LocksCapabilities.LOCKABLE_HANDLER).ifPresent(handler -> handler.add(pkt.lockable));
			}
		});
		ctx.get().setPacketHandled(true);
	}
}