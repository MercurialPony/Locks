package melonslise.locks.common.capability;

import java.util.Observer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.utility.Lockable;
import net.minecraftforge.common.util.INBTSerializable;

public interface ICapabilityLockables extends INBTSerializable, Observer
{
	void setLockables(Int2ObjectMap<Lockable> lockables);

	Int2ObjectMap<Lockable> getLockables();

	boolean add(Lockable lockable);

	boolean remove(int networkID);
}