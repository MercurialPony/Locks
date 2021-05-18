package melonslise.locks.common.capability;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Lockable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
		this.lockables.put(lkb.id, lkb);
		this.chunk.markUnsaved();
	}

	@Override
	public void remove(int id)
	{
		this.lockables.remove(id);
		this.chunk.markUnsaved();
	}

	@Override
	public ListNBT serializeNBT()
	{
		ListNBT list = new ListNBT();
		for(Lockable lkb : this.lockables.values())
			list.add(Lockable.toNbt(lkb));
		return list;
	}

	@Override
	public void deserializeNBT(ListNBT nbt)
	{
		ILockableHandler handler = this.chunk.getLevel().getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
		Int2ObjectMap<Lockable> lkbs = handler.getLoaded();
		for(int a = 0; a < nbt.size(); ++a)
		{
			CompoundNBT nbt1 = nbt.getCompound(a);
			Lockable lkb = lkbs.get(Lockable.idFromNbt(nbt1));
			if(lkb == lkbs.defaultReturnValue())
			{
				lkb = Lockable.fromNbt(nbt1);
				lkb.addObserver(handler);
				lkbs.put(lkb.id, lkb);
			}
			this.lockables.put(lkb.id, lkb);
		}
	}
}
