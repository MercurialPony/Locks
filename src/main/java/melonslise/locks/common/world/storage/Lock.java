package melonslise.locks.common.world.storage;

import java.util.Objects;
import java.util.UUID;

import melonslise.locks.utility.LocksUtilities;
import net.minecraft.nbt.NBTTagCompound;

public class Lock
{
	// TODO Protect
	public final UUID id;
	protected byte[] combination;
	// TODO Unprotect
	boolean locked;

	protected Lock(UUID id, byte[] combination, boolean locked)
	{
		this.id = id;
		this.combination = combination;
		this.locked = locked;
	}

	public Lock(UUID id, int length, boolean locked)
	{
		this.id = id;
		this.combination = new byte[length];
		for(byte a = 0; a < length; ++a) combination[a] = a;
		this.shuffle();
		this.locked = locked;
	}

	public int getLength()
	{
		return this.combination.length;
	}

	public boolean isLocked()
	{
		return this.locked;
	}

	public boolean checkPin(int index, int pin)
	{
		return this.combination[index] == pin;
	}

	void shuffle()
	{
		LocksUtilities.shuffle(this.combination);
	}

	public static final String keyID = "id", keyCombination = "combination", keyLocked = "locked";

	public static Lock readFromNBT(NBTTagCompound nbt)
	{
		return new Lock(nbt.getUniqueId(keyID), nbt.getByteArray(keyCombination), nbt.getBoolean(keyLocked));
	}

	public static NBTTagCompound writeToNBT(Lock lock)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setUniqueId(keyID, lock.id);
		nbt.setByteArray(keyCombination, lock.combination);
		nbt.setBoolean(keyLocked, lock.locked);
		return nbt;
	}

	@Override
	public boolean equals(Object object)
	{
		if(this == object) return true;
		if(!(object instanceof Lock)) return false;
		Lock lock = (Lock) object;
		return ((this.id == null && lock.id == null) || this.id.equals(lock.id)) && ((this.combination == null && lock.combination == null) || this.combination.equals(lock.combination)) && this.locked == lock.locked;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.id, this.combination, this.locked);
	}
}