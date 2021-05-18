package melonslise.locks.common.network.toclient;

import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Lockable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent;

public class AddLockableToChunkPacket
{
	private final Lockable lockable;
	private final int x, z;

	public AddLockableToChunkPacket(Lockable lkb, int x, int z)
	{
		this.lockable = lkb;
		this.x = x;
		this.z = z;
	}

	public AddLockableToChunkPacket(Lockable lkb, ChunkPos pos)
	{
		this(lkb, pos.x, pos.z);
	}

	public AddLockableToChunkPacket(Lockable lkb, Chunk ch)
	{
		this(lkb, ch.getPos());
	}

	public static AddLockableToChunkPacket decode(PacketBuffer buf)
	{
		return new AddLockableToChunkPacket(Lockable.fromBuf(buf), buf.readInt(), buf.readInt());
	}

	public static void encode(AddLockableToChunkPacket pkt, PacketBuffer buf)
	{
		Lockable.toBuf(buf, pkt.lockable);
		buf.writeInt(pkt.x);
		buf.writeInt(pkt.z);
	}

	public static void handle(AddLockableToChunkPacket pkt, Supplier<NetworkEvent.Context> ctx)
	{
		// Use runnable, lambda causes issues with class loading
		ctx.get().enqueueWork(new Runnable()
		{
			@Override
			public void run()
			{
				Minecraft mc = Minecraft.getInstance();
				ILockableStorage st = mc.level.getChunk(pkt.x, pkt.z).getCapability(LocksCapabilities.LOCKABLE_STORAGE).orElse(null);
				ILockableHandler handler = mc.level.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
				Int2ObjectMap<Lockable> lkbs = handler.getLoaded();
				Lockable lkb = lkbs.get(pkt.lockable.id);
				if(lkb == lkbs.defaultReturnValue())
				{
					lkb = pkt.lockable;
					lkb.addObserver(handler);
					lkbs.put(lkb.id, lkb);
				}
				st.add(lkb);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}