package melonslise.locks.utility.predicate;

import com.google.common.base.Predicate;

import melonslise.locks.common.world.storage.Lockable;

public class LocksSelectors
{
	private LocksSelectors() {}

	public static final Predicate<Lockable> LOCKED = new Predicate<Lockable>()
	{
		@Override
		public boolean apply(Lockable lockable)
		{
			return lockable.lock.isLocked();
		}
	};

	public static final Predicate<Lockable> NOT_LOCKED = new Predicate<Lockable>()
	{
		@Override
		public boolean apply(Lockable lockable)
		{
			return !lockable.lock.isLocked();
		}
	};
}