package melonslise.locks.utility.predicate;

import java.util.function.Predicate;

import melonslise.locks.utility.Box;
import melonslise.locks.utility.Lockable;
import net.minecraft.util.math.BlockPos;

public class PredicateIntersecting implements Predicate<Lockable>
{
	protected Box box;

	public PredicateIntersecting(Box box)
	{
		this.box = box;
	}

	public PredicateIntersecting(BlockPos position)
	{
		this(new Box(position));
	}

	@Override
	public boolean test(Lockable lockable)
	{
		return lockable.box.intersects(this.box);
	}
}