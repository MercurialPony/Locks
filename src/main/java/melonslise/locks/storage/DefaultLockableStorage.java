package melonslise.locks.storage;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.chunk.Chunk;

public class DefaultLockableStorage implements LockableStorage, AutoSyncedComponent, CopyableComponent
{
	private final Chunk chunk;

	private final Int2ObjectMap<Lockable> lockables = new Int2ObjectLinkedOpenHashMap<>();

	public DefaultLockableStorage(Chunk chunk)
	{
		this.chunk = chunk;
	}

	@Override
	public void copyFrom(Component other)
	{
		if(other instanceof DefaultLockableStorage otherStorage)
		{
			this.lockables.clear();
			this.lockables.putAll(otherStorage.lockables);
		}
	}

	public Int2ObjectMap<Lockable> get()
	{
		return this.lockables;
	}

	@Override
	public void add(Lockable lockable)
	{
		this.lockables.put(lockable.id(), lockable);

		this.chunk.setNeedsSaving(true);
	}

	@Override
	public void remove(int id)
	{
		this.lockables.remove(id);

		this.chunk.setNeedsSaving(true);
	}

	@Override
	public void readFromNbt(NbtCompound nbt)
	{
		NbtList list = nbt.getList("Lockables", NbtElement.COMPOUND_TYPE);

		for(int i = 0; i < list.size(); ++i)
		{
			Lockable lockable = Lockable.fromNbt(list.getCompound(i));
			this.lockables.put(lockable.id(), lockable);
		}
	}

	@Override
	public void writeToNbt(NbtCompound nbt)
	{
		NbtList list = new NbtList();

		for(Lockable lockable : this.lockables.values())
		{
			list.add(Lockable.toNbt(lockable));
		}

		nbt.put("Lockables", list);
	}
}