package melonslise.locks.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class LockableInfo
{
	public final Cuboid6i bb;
	public final Lock lock;
	public final Transform tr;
	public final ItemStack stack;
	public final int id;

	public LockableInfo(Cuboid6i bb, Lock lock, Transform tr, ItemStack stack, int id)
	{
		this.bb = bb;
		this.lock = lock;
		this.tr = tr;
		this.stack = stack;
		this.id = id;
	}

	public static LockableInfo fromNbt(CompoundNBT nbt)
	{
		return new LockableInfo(Cuboid6i.fromNbt(nbt.getCompound(Lockable.KEY_BB)), Lock.fromNbt(nbt.getCompound(Lockable.KEY_LOCK)), Transform.values()[(int) nbt.getByte(Lockable.KEY_TRANSFORM)], ItemStack.of(nbt.getCompound(Lockable.KEY_STACK)), nbt.getInt(Lockable.KEY_ID));
	}

	public static CompoundNBT toNbt(LockableInfo lkb)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.put(Lockable.KEY_BB, Cuboid6i.toNbt(lkb.bb));
		nbt.put(Lockable.KEY_LOCK, Lock.toNbt(lkb.lock));
		nbt.putByte(Lockable.KEY_TRANSFORM, (byte) lkb.tr.ordinal());
		nbt.put(Lockable.KEY_STACK, lkb.stack.serializeNBT());
		nbt.putInt(Lockable.KEY_ID, lkb.id);
		return nbt;
	}
}