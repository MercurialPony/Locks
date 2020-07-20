package melonslise.locks.coremod;

import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Orientation;

public class LockableInfo
{
	public final Cuboid6i box;
	public final Lock lock;
	public final Orientation lockOrientation;

	public LockableInfo(Cuboid6i box, Lock lock,  Orientation lockOrientation)
	{
		this.box = box;
		this.lock = lock;
		this.lockOrientation = lockOrientation;
	}
}