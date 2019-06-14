package melonslise.locks.utility;

import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.util.Direction;

public class Lockable extends Observable implements Observer
{
	private static final AtomicInteger networkIDs = new AtomicInteger();

	public final Box box;
	public final Lock lock;
	public final Direction side;
	public final int networkID;

	public Lockable(Box box, Lock lock, Direction side)
	{
		this.box = box;
		this.lock = lock;
		lock.addObserver(this);
		this.side = side;
		this.networkID = networkIDs.incrementAndGet();
	}

	// Client only
	public Lockable(Box box, Lock lock, Direction side, int networkID)
	{
		this.box = box;
		this.lock = lock;
		lock.addObserver(this);
		this.side = side;
		this.networkID = networkID;
	}

	@Override
	public void update(Observable lock, Object data)
	{
		this.setChanged();
		this.notifyObservers();
	}

	@Override
	public boolean equals(Object object)
	{
		if(this == object) return true;
		if(!(object instanceof Lockable)) return false;
		Lockable lockable = (Lockable) object;
		return (this.networkID == lockable.networkID) && ((this.box == null && lockable.box == null) || this.box.equals(lockable.box)) && ((this.lock == null && lockable.lock == null) || this.lock.equals(lockable.lock)) && ((this.side == null && lockable.side == null) || (this.side.equals(lockable.side)));
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.box, this.lock, this.side, this.networkID);
	}
}