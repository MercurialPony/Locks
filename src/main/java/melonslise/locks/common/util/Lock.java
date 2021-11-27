package melonslise.locks.common.util;

import java.util.Objects;
import java.util.Observable;
import java.util.Random;

import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.item.LockingItem;
import net.minecraft.item.ItemStack;

public class Lock extends Observable
{
	// TODO Visibility on all these
	public final int id;
	protected byte[] combination;
	protected boolean locked;

	//  TODO if lock is reshuffled any time other than during creation, then next time it is loaded it will have the initial combination and not the newly reshuffled one. Thankfully reshuffling like that does happen, but this should be changed if it does happen
	public final Random rng;

	protected Lock(int id, byte[] combination, boolean locked)
	{
		this.id = id;
		this.rng = createNewRng(id);
		this.combination = combination;
		this.locked = locked;
	}

	public Lock(int id, int length, boolean locked)
	{
		this.id = id;
		this.rng = createNewRng(id);
		this.combination = new byte[length];
		for(byte a = 0; a < length; ++a)
			combination[a] = a;
		this.shuffle();
		this.locked = locked;
	}
	
	private Random createNewRng(int id)
	{
		//Create RNG that relies on the overworld seed as well as the ID
		long overworldSeed = LocksUtil.getOverworldSeed();
		return new Random(id ^ Math.abs(overworldSeed) * 17317L + overworldSeed);
	}

	public static Lock from(ItemStack stack)
	{
		return new Lock(LockingItem.getOrSetId(stack), LockItem.getLength(stack), !LockItem.isOpen(stack));
	}

	public int getLength()
	{
		return this.combination.length;
	}

	public boolean isLocked()
	{
		return this.locked;
	}

	public void setLocked(boolean locked)
	{
		if(this.locked == locked)
			return;
		this.locked = locked;
		this.setChanged();
		this.notifyObservers();
	}

	public boolean checkPin(int index, int pin)
	{
		return this.combination[index] == pin;
	}
	
	public byte getPin(int index)
	{
		return this.combination[index];
	}

	public void shuffle()
	{
		LocksUtil.shuffle(this.combination, this.rng);
	}

	@Override
	public boolean equals(Object object)
	{
		if(this == object)
			return true;
		if(!(object instanceof Lock))
			return false;
		Lock lock = (Lock) object;
		return this.id == lock.id && this.locked == lock.locked && ((this.combination == null && lock.combination == null) || this.combination.equals(lock.combination));
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.id, this.combination, this.locked);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Lock{id: ");
		sb.append(id);
		sb.append(", locked: ");
		sb.append(locked);
		sb.append("}");
		return sb.toString();
	}
}