package melonslise.locks.common.capability;

import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.RemoveLockablePacket;
import melonslise.locks.common.network.toclient.UpdateLockablePacket;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;

public class LockableHandler implements ILockableHandler
{
	public static final ResourceLocation ID = new ResourceLocation(Locks.ID, "lockable_handler");
	
	public final World world;

	public AtomicInteger lastId = new AtomicInteger();
	
	//Synchronized to attempt to fix concurrency issue in chunk loading, see LockableStorage for details on the crash
	public Int2ObjectMap<Lockable> lockables = Int2ObjectMaps.synchronize(new Int2ObjectLinkedOpenHashMap<Lockable>());
	
	//FIXME bandaid fix for double chests
	//Need actual fix for double chest troubles
	public Int2ObjectMap<Lockable> emptyLockables = new Int2ObjectLinkedOpenHashMap<Lockable>();
	
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
		return LocksUtil.hasChunkAt(this.world, pos) ? this.world.getChunkFromBlockCoords(pos).getCapability(LocksCapabilities.LOCKABLE_STORAGE, null).get(): emptyLockables;
	}

	@Override
	public boolean add(Lockable lkb)
	{
		if(lkb.box.volume() > LocksConfig.SERVER.maxLockableVolume)
			return false;
		
		boolean emptyCollision = false;
		
		List<ILockableStorage> sts = lkb.box.<ILockableStorage>containedChunksTo((x, z) ->
		{
			if(!LocksUtil.hasChunk(this.world, x, z))
				return null;
			
			ILockableStorage st = this.world.getChunkFromChunkCoords(x, z).getCapability(LocksCapabilities.LOCKABLE_STORAGE, null);
			
			return st.get().values().stream().anyMatch(lkb1 -> lkb1.box.intersects(lkb.box)) ? null : st;
			
		}, true);
		if(sts == null)
			return false;
		
		// Verify client was never provided EmptyChunk...
		// This should, in theory work for most use cases, as it essentially waits for all lock related chunks to be loaded on the client before making the lock on the client
		// As the server will be sending lock packets for each chunk the lock touches anyway, this will work just fine.
		// FIXME This will FAIL for locks that stretch past the server or client render distance, probably.
		//
		// An alternative solution to this would be, when adding a lock, if there was an intersection with a lock that has the same network ID, attempt to fix any related chunks
		// Honestly that sounds better than this.
		// Another solution would be to have the client pull from a static map of locks instead of from chunk capabilities, as they won't need to keep track of very many
		
		if(this.world.isRemote)
		{
			for(int a = 0; a < sts.size(); ++a)
			{
				ILockableStorage st = sts.get(a);
				//TODO weird and messy
				if(st instanceof LockableStorage && ((LockableStorage)st).chunk instanceof EmptyChunk)
				{
					//System.out.println("Skipping Lock due to EmptyChunk");
					return false;
				}
			}
		}
		
		// Add to chunk
		for(int a = 0; a < sts.size(); ++a)
			sts.get(a).add(lkb);
		// Add to world 
		this.lockables.put(lkb.networkID, lkb);
		lkb.addObserver(this);
		// Do client/server extras
		if(this.world.isRemote)
			lkb.shake(10);
		else
			LocksUtil.sendToTrackingPlayers(lkb.box, new AddLockablePacket(lkb), world);
		return true;
	}

	@Override
	public boolean remove(int id)
	{
		Lockable lkb = this.lockables.get(id);
		if(lkb == this.lockables.defaultReturnValue())
			return false;
		List<Chunk> chs = lkb.box.containedChunksTo((x, z) -> LocksUtil.hasChunk(world, x, z) ? this.world.getChunkFromChunkCoords(x, z) : null, true);
		
		// Remove from chunk
		for(int a = 0; a < chs.size(); ++a)
			chs.get(a).getCapability(LocksCapabilities.LOCKABLE_STORAGE, null).remove(id);
		// Remove from world
		this.lockables.remove(id);
		lkb.deleteObserver(this);
		// Do client/server extras
		if(this.world.isRemote)
			return true;
		LocksUtil.sendToTrackingPlayers(lkb.box, new RemoveLockablePacket(id), world);
		return true;
	}

	@Override
	public void update(Observable o, Object arg)
	{
		if(this.world.isRemote || !(o instanceof Lockable))
			return;
		Lockable lockable = (Lockable) o;
		LocksUtil.sendToTrackingPlayers(lockable.box, new UpdateLockablePacket(lockable.networkID, lockable.lock.isLocked()), world);
	}

	@Override
	public NBTTagInt serializeNBT()
	{
		return new NBTTagInt(this.lastId.get());
	}

	@Override
	public void deserializeNBT(NBTTagInt nbt)
	{
		this.lastId.set(nbt.getInt());
	}
}
