package melonslise.locks.utility;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public final class LocksUtilities
{
	private LocksUtilities() {}



	/*
	 * 
	 * Java
	 * 
	 */

	/**
	 * Shuffles the given byte array.
	 */
	public static void shuffle(byte[] array)
	{
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int a = array.length - 1; a > 0; --a)
		{
			int index = random.nextInt(a + 1);
			byte temp = array[index];
			array[index] = array[a];
			array[a] = temp;
		}
	}



	/*
	 * 
	 * NBT
	 * 
	 */

	/**
	 * Returns the given stack's compound NBT. Assigns a new compound NBT if the stack doesn't have one.
	 */
	public static CompoundNBT getTag(ItemStack stack)
	{
		if(!stack.hasTag()) stack.setTag(new CompoundNBT());
		return stack.getTag();
	}

	/**
	 * Checks if the given stack has an compound NBT and if it contains the given key. Doesn't work with UUIDs.
	 */
	public static boolean hasKey(ItemStack stack, String key)
	{
		return stack.hasTag() && stack.getTag().contains(key);
	}

	// TODO Static final keys

	/**
	 * Reads a box as 6 consecutive integers from the compound NBT.
	 */
	public static Box readBoxFromNBT(CompoundNBT nbt)
	{
		return new Box(nbt.getInt("x1"), nbt.getInt("y1"), nbt.getInt("z1"), nbt.getInt("x2"), nbt.getInt("y2"), nbt.getInt("z2"));
	}

	/**
	 * Writes the given box as 6 consecutive integers to the given compound NBT.
	 */
	public static CompoundNBT writeBoxToNBT(Box box)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("x1", box.x1);
		nbt.putInt("y1", box.y1);
		nbt.putInt("z1", box.z1);
		nbt.putInt("x2", box.x2);
		nbt.putInt("y2", box.y2);
		nbt.putInt("z2", box.z2);
		return nbt;
	}

	public static final String KEY_ID = "id", KEY_CODE = "code", KEY_LOCKED = "locked";

	/**
	 * Reads a lock as a consecutive integer, byte array and boolean from the given compound NBT.
	 */
	public static Lock readLockFromNBT(CompoundNBT nbt)
	{
		return new Lock(nbt.getInt(KEY_ID), nbt.getByteArray(KEY_CODE), nbt.getBoolean(KEY_LOCKED));
	}

	/**
	 * Writes a lock as a consecutive integer, byte array and boolean to the given compound NBT.
	 */
	public static CompoundNBT writeLockToNBT(Lock lock)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt(KEY_ID, lock.id);
		nbt.putByteArray(KEY_CODE, lock.combination);
		nbt.putBoolean(KEY_LOCKED, lock.locked);
		return nbt;
	}

	public static final String KEY_BOX = "box", KEY_LOCK = "lock", KEY_SIDE = "side";

	/**
	 * Reads a lockable as a consecutive box, lock and enum from the given compound NBT. Does not include the lockable's network ID.
	 */
	public static Lockable readLockableFromNBT(CompoundNBT nbt)
	{
		return new Lockable(readBoxFromNBT(nbt.getCompound(KEY_BOX)), readLockFromNBT(nbt.getCompound(KEY_LOCK)), Direction.byIndex((int) nbt.getByte(KEY_SIDE)));
	}

	/**
	 * Writes a lockable as a consecutive box, lock and enum to the given compound NBT. Does not include the lockable's network ID.
	 */
	public static CompoundNBT writeLockableToNBT(Lockable lockable)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.put(KEY_BOX, writeBoxToNBT(lockable.box));
		nbt.put(KEY_LOCK, writeLockToNBT(lockable.lock));
		nbt.putByte(KEY_SIDE, (byte) lockable.side.getIndex());
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
	public static Box readBoxFromBuffer(PacketBuffer buffer)
	{
		return new Box(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
	}

	/**
	 * Writes the given box as 6 consecutive integers to the given buffer.
	 */
	public static void writeBoxToBuffer(PacketBuffer buffer, Box box)
	{
		buffer.writeInt(box.x1);
		buffer.writeInt(box.y1);
		buffer.writeInt(box.z1);
		buffer.writeInt(box.x2);
		buffer.writeInt(box.y2);
		buffer.writeInt(box.z2);
	}

	/**
	 * Reads a lock as a consecutive integer, byte and boolean from the given buffer. Does not include the lock's combination.
	 */
	public static Lock readLockFromBuffer(PacketBuffer buffer)
	{
		return new Lock(buffer.readInt(), (int) buffer.readByte(), buffer.readBoolean());
	}

	/**
	 * Writes a lock as a consecutive integer, byte and boolean to the given buffer. Does not include the lock's combination.
	 */
	public static void writeLockToBuffer(PacketBuffer buffer, Lock lock)
	{
		buffer.writeInt(lock.id);
		buffer.writeByte((int) lock.getLength());
		buffer.writeBoolean(lock.isLocked());
	}

	/**
	 * Reads a lockable as a consecutive box, lock, enum and int from the given buffer.
	 */
	public static Lockable readLockableFromBuffer(PacketBuffer buffer)
	{
		return new Lockable(readBoxFromBuffer(buffer), readLockFromBuffer(buffer), buffer.readEnumValue(Direction.class), buffer.readInt());
	}

	/**
	 * Writes a lockable as a consecutive box, lock, enum and int to the given buffer.
	 */
	public static void writeLockableToBuffer(PacketBuffer buffer, Lockable lockable)
	{
		writeBoxToBuffer(buffer, lockable.box);
		writeLockToBuffer(buffer, lockable.lock);
		buffer.writeEnumValue(lockable.side);
		buffer.writeInt(lockable.networkID);
	}



	/*
	 * 
	 * Rendering
	 * 
	 */

	public static void drawTexturedRectangle(float x, float y, int u, int v, int width, int height, int textureWidth, int textureHeight)
	{
		float f = 1f / (float) textureWidth;
		float f1 = 1f / (float) textureHeight;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		builder.begin(7, DefaultVertexFormats.POSITION_TEX);
		builder.pos((double) x, (double) (y + (float) height), 0d).tex((double) ((float) u * f), (double) ((float) (v + height) * f1)).endVertex();
		builder.pos((double) (x + (float) width), (double) (y + (float) height), 0d).tex((double) ((float) (u + width) * f), (double) ((float) (v + height) * f1)).endVertex();
		builder.pos((double) (x + (float) width), (double) y, 0d).tex((double) ((float) (u + width) * f), (double) ((float) v * f1)).endVertex();
		builder.pos((double) x, (double) y, 0d).tex((double) ((float) u * f), (double) ((float) v * f1)).endVertex();
		tessellator.draw();
	}



	/*
	 * 
	 * Ray tracing
	 * 
	 */

	public static boolean intersectsInclusive(AxisAlignedBB box1, AxisAlignedBB box2)
	{
		return box1.minX <= box2.maxX && box1.maxX >= box2.minX && box1.minY <= box2.maxY && box1.maxY >= box2.minY && box1.minZ <= box2.maxZ && box1.maxZ >= box2.minZ;
	}

	/**
	 * Calculates the center point of the given box's side.
	 */
	public static Vec3d getBoxSideCenter(Box box, Direction side)
	{
		switch(side)
		{
		case DOWN: return new Vec3d((double) (box.x1 + box.x2) / 2D, (double) box.y1, (double) (box.z1 + box.z2) / 2D);
		case UP: return new Vec3d((double) (box.x1 + box.x2) / 2D, (double) box.y2, (double) (box.z1 + box.z2) / 2D);
		case NORTH: return new Vec3d((double) (box.x1 + box.x2) / 2D, (double) (box.y1 + box.y2) / 2D, (double) box.z1);
		case SOUTH: return new Vec3d((double) (box.x1 + box.x2) / 2D, (double) (box.y1 + box.y2) / 2D, (double) box.z2);
		case WEST: return new Vec3d((double) box.x1, (double) (box.y1 + box.y2) / 2D, (double) (box.z1 + box.z2) / 2D);
		case EAST: return new Vec3d((double) box.x2, (double) (box.y1 + box.y2) / 2D, (double) (box.z1 + box.z2) / 2D);
		default: return null;
		}
	}

	/**
	 * Calculates the center point of the given box's side.
	 */
	public static Vec3d getBoxSideCenter(AxisAlignedBB box, Direction side)
	{
		switch(side)
		{
		case DOWN: return new Vec3d((box.minX + box.maxX) / 2D, box.minY, (box.minZ + box.maxZ) / 2D);
		case UP: return new Vec3d((box.minX + box.maxX) / 2D, box.maxY, (box.minZ + box.maxZ) / 2D);
		case NORTH: return new Vec3d((box.minX + box.maxX) / 2D, (box.minY + box.maxY) / 2D, box.minZ);
		case SOUTH: return new Vec3d((box.minX + box.maxX) / 2D, (box.minY + box.maxY) / 2D, box.maxZ);
		case WEST: return new Vec3d(box.minX, (box.minY + box.maxY) / 2D, (box.minZ + box.maxZ) / 2D);
		case EAST: return new Vec3d(box.maxX, (box.minY + box.maxY) / 2D, (box.minZ + box.maxZ) / 2D);
		default: return null;
		}
	}
}