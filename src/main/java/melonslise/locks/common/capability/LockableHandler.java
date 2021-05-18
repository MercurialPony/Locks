package melonslise.locks.common.capability;

import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksNetwork;
import melonslise.locks.common.init.LocksPacketDistributors;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.RemoveLockablePacket;
import melonslise.locks.common.network.toclient.UpdateLockablePacket;
import melonslise.locks.common.util.Lockable;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/*
 * Manages and handles logic for all LOADED lockables by accessing internal ILockableStorage objects.
 * This means that there is no way of getting a list of ALL lockables in a world like before
 */
public class LockableHandler implements ILockableHandler
{
	public static final ResourceLocation ID = new ResourceLocation(Locks.ID, "lockable_handler");

	public final World world;

	public AtomicInteger lastId = new AtomicInteger();

	public Int2ObjectMap<Lockable> lockables = new Int2ObjectLinkedOpenHashMap<Lockable>();

	public LockableHandler(World world)
	{
		this.world = world;
	}

	public int nextId()
	{
		return this.lastId.incrementAndGet();
	}

	@Override
	public Int2ObjectMap<Lockable> getLoaded()
	{
		return this.lockables;
	}

	@Override
	public Int2ObjectMap<Lockable> getInChunk(BlockPos pos)
	{
		return this.world.hasChunkAt(pos) ? this.world.getChunkAt(pos).getCapability(LocksCapabilities.LOCKABLE_STORAGE).orElse(null).get() : null;
	}

	@Override
	public boolean add(Lockable lkb)
	{
		if(lkb.bb.volume() > LocksServerConfig.MAX_LOCKABLE_VOLUME.get())
			return false;
		List<ILockableStorage> sts = lkb.bb.<ILockableStorage>containedChunksTo((x, z) ->
		{
			if(!this.world.hasChunk(x, z))
				return null;
			ILockableStorage st = this.world.getChunk(x, z).getCapability(LocksCapabilities.LOCKABLE_STORAGE).orElse(null);
			return st.get().values().stream().anyMatch(lkb1 -> lkb1.bb.intersects(lkb.bb)) ? null : st;
		}, true);
		if(sts == null)
			return false;

		// Add to chunk
		for(int a = 0; a < sts.size(); ++a)
			sts.get(a).add(lkb);
		// Add to world 
		this.lockables.put(lkb.id, lkb);
		lkb.addObserver(this);
		// Do client/server extras
		if(this.world.isClientSide)
			lkb.swing(10);
		else
			LocksNetwork.MAIN.send(LocksPacketDistributors.TRACKING_AREA.with(() -> sts.stream().map(st -> ((LockableStorage) st).chunk)), new AddLockablePacket(lkb));
		return true;
	}

	@Override
	public boolean remove(int id)
	{
		Lockable lkb = this.lockables.get(id);
		if(lkb == this.lockables.defaultReturnValue())
			return false;
		List<Chunk> chs = lkb.bb.containedChunksTo((x, z) -> this.world.hasChunk(x, z) ? this.world.getChunk(x, z) : null, true);

		// Remove from chunk
		for(int a = 0; a < chs.size(); ++a)
			chs.get(a).getCapability(LocksCapabilities.LOCKABLE_STORAGE).orElse(null).remove(id);
		// Remove from world
		this.lockables.remove(id);
		lkb.deleteObserver(this);
		// Do client/server extras
		if(this.world.isClientSide)
			return true;
		LocksNetwork.MAIN.send(LocksPacketDistributors.TRACKING_AREA.with(() -> chs.stream()), new RemoveLockablePacket(id));
		return true;
	}

	@Override
	public void update(Observable o, Object arg)
	{
		if(this.world.isClientSide || !(o instanceof Lockable))
			return;
		Lockable lockable = (Lockable) o;
		LocksNetwork.MAIN.send(LocksPacketDistributors.TRACKING_AREA.with(() -> lockable.bb.containedChunksTo((x, z) -> this.world.hasChunk(x, z) ? this.world.getChunk(x, z) : null, false).stream().filter(Objects::nonNull)), new UpdateLockablePacket(lockable));
	}

	@Override
	public IntNBT serializeNBT()
	{
		return IntNBT.valueOf(this.lastId.get());
	}

	@Override
	public void deserializeNBT(IntNBT nbt)
	{
		this.lastId.set(nbt.getAsInt());
	}
}