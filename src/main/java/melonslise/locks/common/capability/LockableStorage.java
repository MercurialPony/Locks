package melonslise.locks.common.capability;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;

/*
 * Internal storage for lockables with almost no handling logic
 * Also stores lockables which are shared by multiple chunks. Duplicate shared lockables are handled by checking if they have already been loaded before
 */
public class LockableStorage implements ILockableStorage
{
	public static final ResourceLocation ID = new ResourceLocation(Locks.ID, "lockable_storage");

	public final Chunk chunk;

	public Int2ObjectMap<Lockable> lockables = new Int2ObjectLinkedOpenHashMap<Lockable>();

	public LockableStorage(Chunk chunk)
	{
		this.chunk = chunk;
	}

	@Override
	public Int2ObjectMap<Lockable> get()
	{
		return this.lockables;
	}

	@Override
	public void add(Lockable lkb)
	{
		this.lockables.put(lkb.networkID, lkb);
		this.chunk.markDirty();
	}

	@Override
	public void remove(int id)
	{
		this.lockables.remove(id);
		this.chunk.markDirty();
	}

	@Override
	public NBTTagList serializeNBT()
	{
		NBTTagList list = new NBTTagList();
		for(Lockable lkb : this.lockables.values())
			list.appendTag(LocksUtil.writeLockableToNBT(lkb));
		return list;
	}

	@Override
	public void deserializeNBT(NBTTagList nbt)
	{
		//FIXME some concurrency crash in here that happens probably due to looking at lists while this thing changes them
		//[Chunk I/O Executor Thread-1/ERROR] [FML]: Unhandled exception loading chunk
		//java.lang.ArrayIndexOutOfBoundsException: -1
		//at it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap.rehash(Int2ObjectLinkedOpenHashMap.java:1665) ~[fastutil-7.1.0.jar:?]
		//at it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap.insert(Int2ObjectLinkedOpenHashMap.java:388) ~[fastutil-7.1.0.jar:?]
		//at it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap.put(Int2ObjectLinkedOpenHashMap.java:394) ~[fastutil-7.1.0.jar:?]
		//at melonslise.locks.common.capability.LockableStorage.deserializeNBT(LockableStorage.java:73) ~[LockableStorage.class:?]
		//at melonslise.locks.common.capability.LockableStorage.deserializeNBT(LockableStorage.java:18) ~[LockableStorage.class:?]
		//at melonslise.locks.common.capability.CapabilityStorage.readNBT(CapabilityStorage.java:20) ~[CapabilityStorage.class:?]
		//at melonslise.locks.common.capability.CapabilityStorage.readNBT(CapabilityStorage.java:9) ~[CapabilityStorage.class:?]
		//at melonslise.locks.common.capability.SerializableCapabilityProvider.deserializeNBT(SerializableCapabilityProvider.java:23) ~[SerializableCapabilityProvider.class:?]
		//at net.minecraftforge.common.capabilities.CapabilityDispatcher.deserializeNBT(CapabilityDispatcher.java:135) ~[CapabilityDispatcher.class:?]
		//at net.minecraft.world.chunk.storage.AnvilChunkLoader.func_75823_a(AnvilChunkLoader.java:471) ~[aye.class:?]
		//at net.minecraft.world.chunk.storage.AnvilChunkLoader.checkedReadChunkFromNBT__Async(AnvilChunkLoader.java:128) ~[aye.class:?]
		//at net.minecraft.world.chunk.storage.AnvilChunkLoader.loadChunk__Async(AnvilChunkLoader.java:92) ~[aye.class:?]
		//at net.minecraftforge.common.chunkio.ChunkIOProvider.run(ChunkIOProvider.java:70) ~[ChunkIOProvider.class:?]
		//at java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) [?:1.8.0_151]
		//at java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) [?:1.8.0_151]
		//at java.lang.Thread.run(Unknown Source) [?:1.8.0_151]
		
		//LockableHandler getLoaded was changed to be synchronized in an attempt to fix this
		
		
		//FIXME Storage is loading in locks but Handler is very, very frequently not adding them
		
		ILockableHandler handler = this.chunk.getWorld().getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
		Int2ObjectMap<Lockable> loadedLockables = handler.getLoaded();
		
		for(int a = 0; a < nbt.tagCount(); ++a)
		{
			NBTTagCompound nbt1 = nbt.getCompoundTagAt(a);
			Lockable lkb = loadedLockables.get(nbt1.getInteger(LocksUtil.KEY_ID));
			if(lkb == loadedLockables.defaultReturnValue())
			{
				lkb = LocksUtil.readLockableFromNBT(nbt1);
				//lkb.addObserver(handler);
				
				if(Locks.debug)
					Locks.logger.debug("Storage deserializing with id: "+lkb.networkID+" ::: "+lkb.toString());
				//loadedLockables.put(lkb.networkID, lkb); //trying out adding this in the chunk load handler instead
				//if(Locks.debug)
				//	Locks.logger.debug("Placing lockable into loaded with id: "+lkb.networkID+" ::: "+lkb.toString());
			}
			
			this.lockables.put(lkb.networkID, lkb);
		}
	}
}