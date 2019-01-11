package melonslise.locks.utility;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.ArrayUtils;

import io.netty.buffer.ByteBuf;
import melonslise.locks.LocksCore;
import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.Lock;
import melonslise.locks.common.world.storage.Lockable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LocksUtilities
{
	private LocksUtilities() {}



	/*
	 * 
	 * Domains
	 * 
	 */

	/**
	 * Prefixes the given path string with the mod's ID.
	 */
	public static String prefixLocks(String path)
	{
		return String.join(".", LocksCore.ID, path);
	}

	/**
	 * Creates a resource location with the mod's ID as the domain and the given path.
	 */
	public static ResourceLocation createLocksDomain(String path)
	{
		return new ResourceLocation(LocksCore.ID, path);
	}



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
	 * Item stacks
	 * 
	 */

	/**
	 * Returns the given stack's NBT tag. Assigns a new NBT tag if the stack doesn't have one.
	 */
	public static NBTTagCompound getTag(ItemStack stack)
	{
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		return stack.getTagCompound();
	}

	/**
	 * Checks if the given stack has an NBT tag and if it contains the given key. Doesn't work with UUIDs.
	 */
	public static boolean hasKey(ItemStack stack, String key)
	{
		return stack.hasTagCompound() && stack.getTagCompound().hasKey(key);
	}

	public static boolean hasUUID(ItemStack stack, String key)
	{
		return stack.hasTagCompound() && stack.getTagCompound().hasUniqueId(key);
	}



	/*
	 * 
	 * Networking
	 * 
	 */

	/**
	 * Reads a UUID as 2 consecutive longs from the given buffer.
	 */
	public static UUID readUUID(ByteBuf buffer)
	{
		return new UUID(buffer.readLong(), buffer.readLong());
	}

	/**
	 * Writes the given UUID as 2 consecutive longs to the given buffer.
	 */
	public static void writeUUID(ByteBuf buffer, UUID uuid)
	{
		buffer.writeLong(uuid.getMostSignificantBits());
		buffer.writeLong(uuid.getLeastSignificantBits());
	}

	/**
	 * Reads a position as 3 consecutive integers from the given buffer.
	 */
	public static BlockPos readPosition(ByteBuf buffer)
	{
		return new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
	}

	/**
	 * Writes the given position as 3 consecutive integers to the given buffer.
	 */
	public static void writePosition(ByteBuf buffer, BlockPos position)
	{
		buffer.writeInt(position.getX());
		buffer.writeInt(position.getY());
		buffer.writeInt(position.getZ());
	}

	/**
	 * Gets an enum of the given class by reading a byte from the given buffer.
	 */
	public static <T extends Enum<T>> T readEnum(ByteBuf buffer, Class<T> c)
	{
		return (T) ((Enum[]) c.getEnumConstants())[(int) buffer.readByte()];
	}

	/**
	 * Writes the given enum's ordinal as a byte to the given buffer.
	 */
	public static void writeEnum(ByteBuf buffer, Enum<?> value)
	{
		buffer.writeByte((byte) value.ordinal());
	}

	/**
	 * Reads a box as 6 consecutive integers from the given buffer.
	 */
	public static Box readBox(ByteBuf buffer)
	{
		return new Box(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
	}

	/**
	 * Writes the given box as 6 consecutive integers to the given buffer. Does not include the lock's combination.
	 */
	public static void writeBox(ByteBuf buffer, Box box)
	{
		buffer.writeInt(box.x1);
		buffer.writeInt(box.y1);
		buffer.writeInt(box.z1);
		buffer.writeInt(box.x2);
		buffer.writeInt(box.y2);
		buffer.writeInt(box.z2);
	}

	/**
	 * Reads a lock as a consecutive UUID, byte and boolean from the given buffer. Does not include the lock's combination.
	 * 
	 */
	public static Lock readLock(ByteBuf buffer)
	{
		return new Lock(readUUID(buffer), (int) buffer.readByte(), buffer.readBoolean());
	}

	/**
	 * Writes a lock as a consecutive UUID, byte and boolean to the given buffer.
	 */
	public static void writeLock(ByteBuf buffer, Lock lock)
	{
		writeUUID(buffer, lock.id);
		buffer.writeByte((int) lock.getLength());
		buffer.writeBoolean(lock.isLocked());
	}

	/**
	 * Reads a lockable as a consecutive box, lock and enum from the given buffer.
	 */
	public static Lockable readLockable(ByteBuf buffer)
	{
		return new Lockable(readBox(buffer), readLock(buffer), readEnum(buffer, EnumFacing.class));
	}

	/**
	 * Writes a lockable as a consecutive box, lock and enum to the given buffer.
	 */
	public static void writeLockable(ByteBuf buffer, Lockable lockable)
	{
		writeBox(buffer, lockable.box);
		writeLock(buffer, lockable.lock);
		writeEnum(buffer, lockable.side);
	}

	// TODO
	public static int openContainer(EntityPlayerMP player, Container container)
	{
		player.getNextWindowId();
		player.openContainer = container;
		player.openContainer.windowId = player.currentWindowId;
		player.openContainer.addListener(player);
		return player.currentWindowId;
	}



	/*
	 * 
	 * Configuration
	 * 
	 */

	/**
	 * Checks if the block at the given position in the given world is listed in the mod's configuration.
	 */
	public static boolean canLock(World world, BlockPos position)
	{
		return ArrayUtils.contains(LocksConfiguration.getMain(world).lockable_blocks, world.getBlockState(position).getBlock().getRegistryName().toString());
	}



	/*
	 * 
	 * Rendering
	 * 
	 */

	/**
	 * Returns the client player's interpolated position.
	 */
	public static Vec3d getRenderOrigin(float partialTick)
	{
		Minecraft mc = Minecraft.getMinecraft();
		return new Vec3d(
				mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * (double) partialTick,
				mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * (double) partialTick,
				mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * (double) partialTick);
	}



	/*
	 * 
	 * Bounding boxes
	 * 
	 */

	/**
	 * Calculates the center point of the given box's side.
	 */
	public static Vec3d getBoxSideCenter(Box box, EnumFacing side)
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
	public static Vec3d getBoxSideCenter(AxisAlignedBB box, EnumFacing side)
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



	/*
	 * 
	 * Miscellaneous
	 * 
	 */

	/**
	 * Finds the position of a chest adjacent to the given one or null if none were found.
	 */
	public static BlockPos getAdjacentChest(TileEntityChest chest)
	{
		BlockPos position = null;
		if(chest.adjacentChestXNeg != null) position = chest.adjacentChestXNeg.getPos();
		else if(chest.adjacentChestXPos != null) position = chest.adjacentChestXPos.getPos();
		else if(chest.adjacentChestZNeg != null) position = chest.adjacentChestZNeg.getPos();
		else if(chest.adjacentChestZPos != null) position = chest.adjacentChestZPos.getPos();
		return position;
	}
}