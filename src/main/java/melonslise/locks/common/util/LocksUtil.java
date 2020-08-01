package melonslise.locks.common.util;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class LocksUtil
{
	private LocksUtil() {}



	/*
	 * 
	 * Java
	 * 
	 */

	/**
	 * Shuffles the given byte array.
	 */
	public static void shuffle(byte[] array, Random rng)
	{
		for (int a = array.length - 1; a > 0; --a)
		{
			int index = rng.nextInt(a + 1);
			byte temp = array[index];
			array[index] = array[a];
			array[a] = temp;
		}
	}



	/*
	 * 
	 * Miscellaneous
	 * 
	 */

	public static BlockPos getAdjacentChest(TileEntityChest te)
	{
		BlockPos pos = null;
		te.checkForAdjacentChests();
		if(te.adjacentChestXNeg != null)
			pos = te.adjacentChestXNeg.getPos();
		else if(te.adjacentChestXPos != null)
			pos = te.adjacentChestXPos.getPos();
		else if(te.adjacentChestZNeg != null)
			pos = te.adjacentChestZNeg.getPos();
		else if(te.adjacentChestZPos != null)
				pos = te.adjacentChestZPos.getPos();
		return pos;
	}

	public static AttachFace faceFromDir(EnumFacing dir)
	{
		return dir == EnumFacing.UP ? AttachFace.CEILING : dir == EnumFacing.DOWN ? AttachFace.FLOOR : AttachFace.WALL;
	}



	/*
	 * 
	 * NBT
	 * 
	 */

	/**
	 * Returns the given stack's compound NBT. Assigns a new compound NBT if the stack doesn't have one.
	 */
	public static NBTTagCompound getTag(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		return stack.getTagCompound();
	}

	/**
	 * Checks if the given stack has an compound NBT and if it contains the given key. Doesn't work with UUIDs.
	 */
	public static boolean hasKey(ItemStack stack, String key)
	{
		return stack.hasTagCompound() && stack.getTagCompound().hasKey(key);
	}

	public static final String KEY_X1 = "x1", KEY_Y1 = "y1", KEY_Z1 = "z1", KEY_X2 = "x2", KEY_Y2 = "y2", KEY_Z2 = "z2";

	/**
	 * Reads a box as 6 consecutive integers from the compound NBT.
	 */
	public static Cuboid6i readBoxFromNBT(NBTTagCompound nbt)
	{
		return new Cuboid6i(nbt.getInteger(KEY_X1), nbt.getInteger(KEY_Y1), nbt.getInteger(KEY_Z1), nbt.getInteger(KEY_X2), nbt.getInteger(KEY_Y2), nbt.getInteger(KEY_Z2));
	}

	/**
	 * Writes the given box as 6 consecutive integers to the given compound NBT.
	 */
	public static NBTTagCompound writeBoxToNBT(Cuboid6i box)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger(KEY_X1, box.x1);
		nbt.setInteger(KEY_Y1, box.y1);
		nbt.setInteger(KEY_Z1, box.z1);
		nbt.setInteger(KEY_X2, box.x2);
		nbt.setInteger(KEY_Y2, box.y2);
		nbt.setInteger(KEY_Z2, box.z2);
		return nbt;
	}

	public static final String KEY_ID = "id", KEY_LENGTH = "length", KEY_OLD_CODE = "code", KEY_OLD_COMBINATION = "combination", KEY_LOCKED = "locked";

	/**
	 * Reads a lock as a consecutive integer, byte and boolean from the given compound NBT.
	 * Will also read the combination and use its length if the length tag is not present and will generate a random id if there's a uuid stored under the id tag for compatibility with older versions.
	 */
	public static Lock readLockFromNBT(NBTTagCompound nbt)
	{
		int id = nbt.hasUniqueId(KEY_ID) ? ThreadLocalRandom.current().nextInt() : nbt.getInteger(KEY_ID);
		int length = nbt.hasKey(KEY_LENGTH) ? nbt.getByte(KEY_LENGTH) : nbt.hasKey(KEY_OLD_CODE) ? nbt.getByteArray(KEY_OLD_CODE).length : nbt.getByteArray(KEY_OLD_COMBINATION).length;
		return new Lock(id, length, nbt.getBoolean(KEY_LOCKED));
	}

	/**
	 * Writes a lock as a consecutive integer, byte and boolean to the given compound NBT.
	 */
	public static NBTTagCompound writeLockToNBT(Lock lock)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger(KEY_ID, lock.id);
		nbt.setByte(KEY_LENGTH, (byte) lock.combination.length);
		nbt.setBoolean(KEY_LOCKED, lock.locked);
		return nbt;
	}

	public static final String KEY_BOX = "box", KEY_LOCK = "lock", KEY_ORIENTATION = "orientation", KEY_OLD_SIDE = "side";

	/**
	 * Reads a lockable as a consecutive box, lock and enum from the given compound NBT. Does not include the lockable's network ID.
	 * Will also read the side and convert it if the orientation tag is not present for compatibility with older versions.
	 */
	public static Lockable readLockableFromNBT(NBTTagCompound nbt)
	{
		Orientation orient = nbt.hasKey(KEY_ORIENTATION) ? Orientation.values()[(int) nbt.getByte(KEY_ORIENTATION)] : Orientation.fromDirection(EnumFacing.getFront((int) nbt.getByte(KEY_OLD_SIDE)), EnumFacing.NORTH);
		return new Lockable(readBoxFromNBT(nbt.getCompoundTag(KEY_BOX)), readLockFromNBT(nbt.getCompoundTag(KEY_LOCK)), orient);
	}

	/**
	 * Writes a lockable as a consecutive box, lock and enum  to the given compound NBT. Does not include the lockable's network ID.
	 */
	public static NBTTagCompound writeLockableToNBT(Lockable lockable)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag(KEY_BOX, writeBoxToNBT(lockable.box));
		nbt.setTag(KEY_LOCK, writeLockToNBT(lockable.lock));
		nbt.setByte(KEY_ORIENTATION, (byte) lockable.orient.ordinal());
		return nbt;
	}



	/*
	 * 
	 * Networking
	 * 
	 */

	/**
	 * Gets an enum of the given class by reading a byte from the given buffer.
	 */
	public static <T extends Enum<T>> T readEnumFromBuffer(ByteBuf buffer, Class<T> c)
	{
		return (T) ((Enum[]) c.getEnumConstants())[(int) buffer.readByte()];
	}

	/**
	 * Writes the given enum's ordinal as a byte to the given buffer.
	 */
	public static void writeEnumToBuffer(ByteBuf buffer, Enum<?> value)
	{
		buffer.writeByte((byte) value.ordinal());
	}

	/**
	 * Reads a box as 6 consecutive integers from the given buffer.
	 */
	public static Cuboid6i readBoxFromBuffer(ByteBuf buf)
	{
		return new Cuboid6i(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
	}

	/**
	 * Writes the given box as 6 consecutive integers to the given buffer.
	 */
	public static void writeBoxToBuffer(ByteBuf buf, Cuboid6i box)
	{
		buf.writeInt(box.x1);
		buf.writeInt(box.y1);
		buf.writeInt(box.z1);
		buf.writeInt(box.x2);
		buf.writeInt(box.y2);
		buf.writeInt(box.z2);
	}

	/**
	 * Reads a lock as a consecutive integer, byte and boolean from the given buffer. Does not include the lock's combination.
	 */
	public static Lock readLockFromBuffer(ByteBuf buf)
	{
		return new Lock(buf.readInt(), (int) buf.readByte(), buf.readBoolean());
	}

	/**
	 * Writes a lock as a consecutive integer, byte and boolean to the given buffer. Does not include the lock's combination.
	 */
	public static void writeLockToBuffer(ByteBuf buf, Lock lock)
	{
		buf.writeInt(lock.id);
		buf.writeByte((int) lock.getLength());
		buf.writeBoolean(lock.isLocked());
	}

	/**
	 * Reads a lockable as a consecutive box, lock, enum and int from the given buffer.
	 */
	public static Lockable readLockableFromBuffer(ByteBuf buf)
	{
		return new Lockable(readBoxFromBuffer(buf), readLockFromBuffer(buf), readEnumFromBuffer(buf, Orientation.class), buf.readInt());
	}

	/**
	 * Writes a lockable as a consecutive box, lock, enum and int to the given buffer.
	 */
	public static void writeLockableToBuffer(ByteBuf buf, Lockable lockable)
	{
		writeBoxToBuffer(buf, lockable.box);
		writeLockToBuffer(buf, lockable.lock);
		writeEnumToBuffer(buf, lockable.orient);
		buf.writeInt(lockable.networkID);
	}



	/*
	 * 
	 * Bounding boxes
	 * 
	 */

	public static AxisAlignedBB rotateY(AxisAlignedBB bb)
	{
		return new AxisAlignedBB(bb.minZ, bb.minY, bb.minX, bb.maxZ, bb.maxY, bb.maxX);
	}

	public static AxisAlignedBB rotateX(AxisAlignedBB bb)
	{
		return new AxisAlignedBB(bb.minX, bb.minZ, bb.minY, bb.maxX, bb.maxZ, bb.maxY);
	}

	public static boolean intersectsInclusive(AxisAlignedBB box1, AxisAlignedBB box2)
	{
		return box1.minX <= box2.maxX && box1.maxX >= box2.minX && box1.minY <= box2.maxY && box1.maxY >= box2.minY && box1.minZ <= box2.maxZ && box1.maxZ >= box2.minZ;
	}

	/**
	 * Calculates the center point of the given box's side.
	 */
	public static Vec3d getAABBSideCenter(AxisAlignedBB box, EnumFacing side)
	{
		switch(side)
		{
		case DOWN:
			return new Vec3d((box.minX + box.maxX) / 2D, box.minY, (box.minZ + box.maxZ) / 2D);
		case UP:
			return new Vec3d((box.minX + box.maxX) / 2D, box.maxY, (box.minZ + box.maxZ) / 2D);
		case NORTH:
			return new Vec3d((box.minX + box.maxX) / 2D, (box.minY + box.maxY) / 2D, box.minZ);
		case SOUTH:
			return new Vec3d((box.minX + box.maxX) / 2D, (box.minY + box.maxY) / 2D, box.maxZ);
		case WEST:
			return new Vec3d(box.minX, (box.minY + box.maxY) / 2D, (box.minZ + box.maxZ) / 2D);
		case EAST:
			return new Vec3d(box.maxX, (box.minY + box.maxY) / 2D, (box.minZ + box.maxZ) / 2D);
		default: return null;
		}
	}

	/*
	public static AxisAlignedBB rotate(AxisAlignedBB aabb, Matrix3d rotationMatrix)
	{
		Vector3d point1 = new Vector3d(aabb.minX, aabb.minY, aabb.minZ);
		Vector3d point2 = new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ);
		rotationMatrix.transform(point1);
		rotationMatrix.transform(point2);
		return new AxisAlignedBB(point1.x, point1.y, point1.z, point2.x, point2.y, point2.z);
	}
	*/
}