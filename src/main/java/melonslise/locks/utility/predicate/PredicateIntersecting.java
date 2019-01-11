package melonslise.locks.utility.predicate;

import com.google.common.base.Predicate;

import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.Lockable;

public class PredicateIntersecting implements Predicate<Lockable>
{
	protected Box box;

	public PredicateIntersecting(Box box)
	{
		this.box = box;
	}

	@Override
	public boolean apply(Lockable lockable)
	{
		return lockable.box == null && this.box == null || lockable.box.intersects(this.box);
	}
}