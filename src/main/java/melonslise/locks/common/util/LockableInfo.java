package melonslise.locks.common.util;

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