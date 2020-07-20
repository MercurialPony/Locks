package melonslise.locks.common.capability;

import java.util.Observer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.common.util.Lockable;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface ILockableStorage extends INBTSerializable<ListNBT>, Observer
{
	void set(Int2ObjectMap<Lockable> lockables);

	Int2ObjectMap<Lockable> get();

	boolean add(Lockable lockable);

	boolean remove(int networkID);
}