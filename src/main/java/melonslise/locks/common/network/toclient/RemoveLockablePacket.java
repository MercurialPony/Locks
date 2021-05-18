package melonslise.locks.common.network.toclient;

import java.util.function.Supplier;

import melonslise.locks.common.init.LocksCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class RemoveLockablePacket
{
	private final int id;

	public RemoveLockablePacket(int id)
	{
		this.id = id;
	}

	public static RemoveLockablePacket decode(PacketBuffer buf)
	{
		return new RemoveLockablePacket(buf.readInt());
	}

	public static void encode(RemoveLockablePacket pkt, PacketBuffer buf)
	{
		buf.writeInt(pkt.id);
	}

	public static void handle(RemoveLockablePacket pkt, Supplier<NetworkEvent.Context> ctx)
	{
		// Use runnable, lambda causes issues with class loading
		ctx.get().enqueueWork(new Runnable()
		{
			@Override
			public void run()
			{
				Minecraft.getInstance().level.getCapability(LocksCapabilities.LOCKABLE_HANDLER).ifPresent(handler -> handler.remove(pkt.id));
			}
		});
		ctx.get().setPacketHandled(true);
	}
}