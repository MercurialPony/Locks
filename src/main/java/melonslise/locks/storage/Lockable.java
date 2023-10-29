package melonslise.locks.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class Lockable
{
	private final int id;
	private final AABBi bounds;
	private final Lock lock;
	private final ItemStack item;

	private int swingTimer, oldSwingTimer, maxSwingTimer;

	public Lockable(int id, AABBi bounds, Lock lock, ItemStack item)
	{
		this.id = id;
		this.bounds = bounds;
		this.lock = lock;
		this.item = item;

		this.lock.asObservable().subscribe(locked -> item.decrement(1));
	}

	public static Lockable fromNbt(NbtCompound nbt)
	{
		// return new Lockable(AABBi.fromNbt(nbt.getCompound("Bounds")), Lock.fromNbt(nbt.getCompound("Lock")), Transform.values()[(int) nbt.getByte(KEY_TRANSFORM)], ItemStack.of(nbt.getCompound(KEY_STACK)), nbt.getInt(KEY_ID));
		return new Lockable(0, null, null, null);
	}

	public static NbtCompound toNbt(Lockable lockable)
	{
		NbtCompound nbt = new NbtCompound();
		/*
		nbt.put(KEY_BB, Cuboid6i.toNbt(lkb.bb));
		nbt.put(KEY_LOCK, Lock.toNbt(lkb.lock));
		nbt.putByte(KEY_TRANSFORM, (byte) lkb.tr.ordinal());
		nbt.put(KEY_STACK, lkb.stack.serializeNBT());
		nbt.putInt(KEY_ID, lkb.id);
		 */
		return nbt;
	}

	public int id()
	{
		return this.id;
	}

	public AABBi bounds()
	{
		return this.bounds;
	}

	public Lock lock()
	{
		return this.lock;
	}

	public ItemStack item()
	{
		return this.item;
	}

	public void tick()
	{
		this.oldSwingTimer = this.swingTimer;
		this.swingTimer = Math.max(0, this.swingTimer - 1);
	}

	public void swing(int swingTimeInTicks)
	{
		this.maxSwingTimer = swingTimeInTicks;
	}
}