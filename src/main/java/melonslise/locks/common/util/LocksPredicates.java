package melonslise.locks.common.util;

import java.util.function.Predicate;

public final class LocksPredicates
{
	public static final Predicate<Lockable> LOCKED = lkb -> lkb.lock.isLocked();

	public static final Predicate<Lockable> NOT_LOCKED = lkb -> !lkb.lock.isLocked();

	private LocksPredicates() {}
}