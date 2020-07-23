package melonslise.locks.common.util;

import java.util.Random;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.template.PlacementSettings;

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

	public static BlockPos transformPos(int x, int y, int z, PlacementSettings settings)
	{
		switch(settings.getMirror())
		{
		case LEFT_RIGHT:
			z = -z + 1;
			break;
		case FRONT_BACK:
			x = -x + 1;
			break;
		default:
			break;
		}
		int x1 = settings.func_207664_d().getX();
		int z1 = settings.func_207664_d().getZ();
		switch(settings.getRotation())
		{
		case COUNTERCLOCKWISE_90:
			return new BlockPos(x1 - z1 + z, y, x1 + z1 - x + 1);
		case CLOCKWISE_90:
			return new BlockPos(x1 + z1 - z + 1, y, z1 - x1 + x);
		case CLOCKWISE_180:
			return new BlockPos(x1 + x1 - x + 1, y, z1 + z1 - z + 1);
		default:
			return new BlockPos(x, y, z);
		}
	}

	public static AttachFace faceFromDir(Direction dir)
	{
		return dir == Direction.UP ? AttachFace.CEILING : dir == Direction.DOWN ? AttachFace.FLOOR : AttachFace.WALL;
	}



	/*
	 * 
	 * NBT
	 * 
	 */

	public static final String KEY_X1 = "x1", KEY_Y1 = "y1", KEY_Z1 = "z1", KEY_X2 = "x2", KEY_Y2 = "y2", KEY_Z2 = "z2";

	/**
	 * Reads a box as 6 consecutive integers from the compound NBT.
	 */
	public static Cuboid6i readBoxFromNBT(CompoundNBT nbt)
	{
		return new Cuboid6i(nbt.getInt(KEY_X1), nbt.getInt(KEY_Y1), nbt.getInt(KEY_Z1), nbt.getInt(KEY_X2), nbt.getInt(KEY_Y2), nbt.getInt(KEY_Z2));
	}

	/**
	 * Writes the given box as 6 consecutive integers to the given compound NBT.
	 */
	public static CompoundNBT writeBoxToNBT(Cuboid6i box)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt(KEY_X1, box.x1);
		nbt.putInt(KEY_Y1, box.y1);
		nbt.putInt(KEY_Z1, box.z1);
		nbt.putInt(KEY_X2, box.x2);
		nbt.putInt(KEY_Y2, box.y2);
		nbt.putInt(KEY_Z2, box.z2);
		return nbt;
	}

	public static final String KEY_ID = "id", KEY_LENGTH = "length", KEY_OLD_CODE = "code", KEY_OLD_COMBINATION = "combination", KEY_LOCKED = "locked";

	/**
	 * Reads a lock as a consecutive integer, byte and boolean from the given compound NBT.
	 * Will also read the combination and use its length if the length tag is not present and will generate a random id if there's a uuid stored under the id tag for compatibility with older versions.
	 */
	public static Lock readLockFromNBT(CompoundNBT nbt)
	{
		int id = nbt.hasUniqueId(KEY_ID) ? ThreadLocalRandom.current().nextInt() : nbt.getInt(KEY_ID);
		int length = nbt.contains(KEY_LENGTH) ? nbt.getByte(KEY_LENGTH) : nbt.contains(KEY_OLD_CODE) ? nbt.getByteArray(KEY_OLD_CODE).length : nbt.getByteArray(KEY_OLD_COMBINATION).length;
		return new Lock(id, length, nbt.getBoolean(KEY_LOCKED));
	}

	/**
	 * Writes a lock as a consecutive integer, byte and boolean to the given compound NBT.
	 */
	public static CompoundNBT writeLockToNBT(Lock lock)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt(KEY_ID, lock.id);
		nbt.putByte(KEY_LENGTH, (byte) lock.combination.length);
		nbt.putBoolean(KEY_LOCKED, lock.locked);
		return nbt;
	}

	public static final String KEY_BOX = "box", KEY_LOCK = "lock", KEY_ORIENTATION = "orientation", KEY_OLD_SIDE = "side";

	/**
	 * Reads a lockable as a consecutive box, lock and enum from the given compound NBT. Does not include the lockable's network ID.
	 * Will also read the side and convert it if the orientation tag is not present for compatibility with older versions.
	 */
	public static Lockable readLockableFromNBT(CompoundNBT nbt)
	{
		Orientation orient = nbt.contains(KEY_ORIENTATION) ? Orientation.values()[(int) nbt.getByte(KEY_ORIENTATION)] : Orientation.fromDirection(Direction.byIndex((int) nbt.getByte(KEY_OLD_SIDE)), Direction.NORTH);
		return new Lockable(readBoxFromNBT(nbt.getCompound(KEY_BOX)), readLockFromNBT(nbt.getCompound(KEY_LOCK)), orient);
	}

	/**
	 * Writes a lockable as a consecutive box, lock and enum  to the given compound NBT. Does not include the lockable's network ID.
	 */
	public static CompoundNBT writeLockableToNBT(Lockable lockable)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.put(KEY_BOX, writeBoxToNBT(lockable.box));
		nbt.put(KEY_LOCK, writeLockToNBT(lockable.lock));
		nbt.putByte(KEY_ORIENTATION, (byte) lockable.orient.ordinal());
		return nbt;
	}



	/*
	 * 
	 * Networking
	 * 
	 */

	/**
	 * Reads a box as 6 consecutive integers from the given buffer.
	 */
	public static Cuboid6i readBoxFromBuffer(PacketBuffer buf)
	{
		return new Cuboid6i(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
	}

	/**
	 * Writes the given box as 6 consecutive integers to the given buffer.
	 */
	public static void writeBoxToBuffer(PacketBuffer buf, Cuboid6i box)
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
	public static Lock readLockFromBuffer(PacketBuffer buf)
	{
		return new Lock(buf.readInt(), (int) buf.readByte(), buf.readBoolean());
	}

	/**
	 * Writes a lock as a consecutive integer, byte and boolean to the given buffer. Does not include the lock's combination.
	 */
	public static void writeLockToBuffer(PacketBuffer buf, Lock lock)
	{
		buf.writeInt(lock.id);
		buf.writeByte((int) lock.getLength());
		buf.writeBoolean(lock.isLocked());
	}

	/**
	 * Reads a lockable as a consecutive box, lock, enum and int from the given buffer.
	 */
	public static Lockable readLockableFromBuffer(PacketBuffer buf)
	{
		return new Lockable(readBoxFromBuffer(buf), readLockFromBuffer(buf), buf.readEnumValue(Orientation.class), buf.readInt());
	}

	/**
	 * Writes a lockable as a consecutive box, lock, enum and int to the given buffer.
	 */
	public static void writeLockableToBuffer(PacketBuffer buf, Lockable lockable)
	{
		writeBoxToBuffer(buf, lockable.box);
		writeLockToBuffer(buf, lockable.lock);
		buf.writeEnumValue(lockable.orient);
		buf.writeInt(lockable.networkID);
	}



	/*
	 * 
	 * Bounding boxes
	 * 
	 */

	public static boolean intersectsInclusive(AxisAlignedBB box1, AxisAlignedBB box2)
	{
		return box1.minX <= box2.maxX && box1.maxX >= box2.minX && box1.minY <= box2.maxY && box1.maxY >= box2.minY && box1.minZ <= box2.maxZ && box1.maxZ >= box2.minZ;
	}

	/**
	 * Calculates the center point of the given box's side.
	 */
	public static Vec3d getAABBSideCenter(AxisAlignedBB box, Direction side)
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