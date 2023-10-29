package melonslise.locks.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public interface LockableManager
{
	Int2ObjectMap<Lockable> loaded();

	void add(Lockable lockable);

	void remove(int id);
}