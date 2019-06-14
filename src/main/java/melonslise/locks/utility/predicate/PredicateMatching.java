package melonslise.locks.utility.predicate;

import java.util.function.Predicate;

import melonslise.locks.utility.Lockable;

public class PredicateMatching implements Predicate<Lockable>
{
	protected int id;

	public PredicateMatching(int id)
	{
		this.id = id;
	}

	@Override
	public boolean test(Lockable lockable)
	{
		return lockable.lock.id == this.id;
	}
}