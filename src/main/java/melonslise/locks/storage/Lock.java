package melonslise.locks.storage;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import net.minecraft.nbt.NbtCompound;

import java.util.Random;

public class Lock
{
	private final int id;
	// ri is the order, value is the pin number
	private final byte[] combo;
	private boolean locked;

	private final Subject<Boolean> publisher;

	public Lock(int id, int length, boolean locked)
	{
		this.id = id;
		this.locked = locked;

		this.combo = new byte[length];
		for(byte i = 0; i < length; ++i)
		{
			combo[i] = i;
		}
		shuffle(this.combo, new Random(id));

		this.publisher = PublishSubject.create();
	}

	public static Lock fromNbt(NbtCompound nbt)
	{
		return new Lock(nbt.getInt("Id"), nbt.getByte("Length"), nbt.getBoolean("Locked"));
	}

	public static NbtCompound toNbt(Lock lock)
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putInt("Id", lock.id);
		nbt.putByte("Length", (byte) lock.combo.length);
		nbt.putBoolean("Locked", lock.locked);
		return nbt;
	}

	public int id()
	{
		return this.id;
	}

	public int length()
	{
		return this.combo.length;
	}

	public boolean locked()
	{
		return this.locked;
	}

	public void locked(boolean locked)
	{
		if(this.locked != locked)
		{
			this.locked = locked;
			this.publisher.onNext(locked);
		}
	}

	public Observable<Boolean> asObservable()
	{
		return this.publisher;
	}

	public int getPin(int index)
	{
		return this.combo[index];
	}

	public boolean checkPin(int index, int pin)
	{
		return this.getPin(index) == pin;
	}

	private static void shuffle(byte[] array, Random rng)
	{
		for (int i = array.length - 1; i > 0; --i)
		{
			int ri = rng.nextInt(i + 1);
			byte temp = array[ri];
			array[ri] = array[i];
			array[i] = temp;
		}
	}
}