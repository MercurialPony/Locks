package melonslise.locks.common.world.storage;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class Lockable
{
	public final Box box;
	public final Lock lock;
	public final EnumFacing side;

	public Lockable(Box box, Lock lock, EnumFacing side)
	{
		this.box = box;
		this.lock = lock;
		this.side = side;
	}

	public static final String keyBox = "box", keyLock = "lock", keySide = "side";

	public static Lockable readFromNBT(NBTTagCompound nbt)
	{
		return new Lockable(Box.readFromNBT(nbt.getCompoundTag(keyBox)), Lock.readFromNBT(nbt.getCompoundTag(keyLock)), EnumFacing.getFront((int) nbt.getByte(keySide)));
	}

	public static NBTTagCompound writeToNBT(Lockable lockable)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag(keyBox, Box.writeToNBT(lockable.box));
		nbt.setTag(keyLock, Lock.writeToNBT(lockable.lock));
		nbt.setByte(keySide, (byte) lockable.side.getIndex());
		return nbt;
	}

	@Override
	public boolean equals(Object object)
	{
		if(this == object) return true;
		if(!(object instanceof Lockable)) return false;
		Lockable lockable = (Lockable) object;
		return ((this.box == null && lockable.box == null) || this.box.equals(lockable.box)) && ((this.lock == null && lockable.lock == null) || this.lock.equals(lockable.lock)) && ((this.side == null && lockable.side == null) || (this.side.equals(lockable.side)));
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.box, this.lock, this.side);
	}
}