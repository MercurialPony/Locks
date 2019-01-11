package melonslise.locks.utility.predicate;

import java.util.UUID;

import com.google.common.base.Predicate;

import melonslise.locks.common.world.storage.Lockable;

public class PredicateMatching implements Predicate<Lockable>
{
	protected UUID id;

	public PredicateMatching(UUID id)
	{
		this.id = id;
	}

	@Override
	public boolean apply(Lockable lockable)
	{
		return lockable.lock.id == null && this.id == null || lockable.lock.id.equals(this.id);
	}
}