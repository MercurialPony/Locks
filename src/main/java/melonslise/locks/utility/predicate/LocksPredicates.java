package melonslise.locks.utility.predicate;

import java.util.function.Predicate;

import melonslise.locks.utility.Lockable;

public final class LocksPredicates
{
	public static final Predicate<Lockable> LOCKED = lockable -> lockable.lock.isLocked();

	public static final Predicate<Lockable> NOT_LOCKED = LOCKED.negate();

	private LocksPredicates() {}
}