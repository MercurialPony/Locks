package melonslise.locks.coremod;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.util.Orientation;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public final class LocksDelegates
{
	private LocksDelegates() {}

	// TODO Disable during worldgen
	public static void onBlockUpdate(World world, BlockPos pos, BlockState oldState, BlockState newState, int flags)
	{
		// World.isRemote check not required because this is fired only on the server anyway
		if(oldState.getBlock() != newState.getBlock())
			Locks.PROXY.getLockables(world)
				.ifPresent(lockables ->
				{
					lockables.get().values().stream().filter(lockable1 -> lockable1.box.intersects(pos))
					.forEach(lockable ->
						{
							world.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 0.8f, 0.8f + world.rand.nextFloat() * 0.4f);
							world.addEntity(new ItemEntity(world, (double) pos.getX() + 0.5d, (double) pos.getY() + 0.5d, (double) pos.getZ() + 0.5d, LockItem.from(lockable.lock)));
							lockables.remove(lockable.networkID);
						});
				});
	}

	public static List<LockableInfo> takeLockablesFromWorld(World world, BlockPos start, BlockPos size)
	{
		return Locks.PROXY.getLockables(world)
			.map(lockables ->
			{
				Cuboid6i box = new Cuboid6i(start, start.add(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
				return lockables.get().values().stream().filter(lockable1 -> lockable1.box.intersects(box))
					.map(lockable ->
					{
						Cuboid6i newBox = box.intersection(lockable.box).offset(-start.getX(), -start.getY(), -start.getZ());
						return new LockableInfo(newBox, lockable.lock, lockable.orient);
					})
					.collect(Collectors.toList());
			})
			.orElse(Lists.newArrayList());
	}

	// FIXME Mutable bb
	public static void addLockablesToWorld(List<LockableInfo> infos, IWorld world, BlockPos start, PlacementSettings settings) 
	{
		Locks.PROXY.getLockables(world.getWorld())
			.ifPresent(lockables ->
			{
				for(LockableInfo lockable : infos)
				{
					BlockPos pos1 = LocksUtil.transformPos(lockable.box.x1, lockable.box.y1, lockable.box.z1, settings);
					BlockPos pos2 = LocksUtil.transformPos(lockable.box.x2, lockable.box.y2, lockable.box.z2, settings);
					Cuboid6i box = new Cuboid6i(pos1.getX() + start.getX(), pos1.getY() + start.getY(), pos1.getZ() + start.getZ(), pos2.getX() + start.getX(), pos2.getY() + start.getY(), pos2.getZ() + start.getZ());
					Lock lock = LocksConfig.RANDOMIZE_LOADED_LOCKS.get() ? new Lock(world.getRandom().nextInt(), LocksConfig.randLockLen(world.getRandom()), true) : lockable.lock;
					Orientation orient = Orientation.fromDirectionAndFace(settings.getRotation().rotate(settings.getMirror().toRotation(lockable.lockOrientation.dir).rotate(lockable.lockOrientation.dir)), lockable.lockOrientation.face, Direction.NORTH);
					lockables.add(new Lockable(box, lock, orient));
				}
			});
	}

	public static final String KEY_LOCKABLES = "lockables";

	public static void writeLockablesToNBT(CompoundNBT nbt, List<LockableInfo> infos)
	{
		ListNBT lockableNBTList = new ListNBT();
		for(LockableInfo lockable : infos)
		{
			CompoundNBT lockableNBT = new CompoundNBT();
			lockableNBT.put(LocksUtil.KEY_BOX, LocksUtil.writeBoxToNBT(lockable.box));
			lockableNBT.put(LocksUtil.KEY_LOCK, LocksUtil.writeLockToNBT(lockable.lock));
			lockableNBT.putByte(LocksUtil.KEY_ORIENTATION, (byte) lockable.lockOrientation.ordinal());
			lockableNBTList.add(lockableNBT);
		}
		nbt.put(KEY_LOCKABLES, lockableNBTList);
	}

	public static List<LockableInfo> readLockablesFromNBT(CompoundNBT nbt)
	{
		List<LockableInfo> list = Lists.newArrayList();
		ListNBT nbtList = nbt.getList(KEY_LOCKABLES, Constants.NBT.TAG_COMPOUND);
		for(int a = 0, b = nbtList.size(); a < b; ++a)
		{
			CompoundNBT lockableNBT = nbtList.getCompound(a);
			Cuboid6i box = LocksUtil.readBoxFromNBT(lockableNBT.getCompound(LocksUtil.KEY_BOX));
			Lock lock = LocksUtil.readLockFromNBT(lockableNBT.getCompound(LocksUtil.KEY_LOCK));
			Orientation orient = Orientation.values()[(int) lockableNBT.getByte(LocksUtil.KEY_ORIENTATION)];
			list.add(new LockableInfo(box, lock, orient));
		}
		return list;
	}

	@OnlyIn(Dist.CLIENT)
	public static ClippingHelper clippingHelper;

	@OnlyIn(Dist.CLIENT)
	public static void setClippingHelper(ClippingHelper clippingHelper)
	{
		LocksDelegates.clippingHelper = clippingHelper;
	}

	/*
	public static Button createLocksButton(EditStructureScreen screen, StructureBlockTileEntity tileEntity)
	{
		return new Button(screen.width / 2 + 4 + 100, 160, 50, 20, "LOCKS", button ->
		{
			// update tile
			updateLocksButton(button, tileEntity);
		});
	}

	public static void updateLocksButton(Button button, StructureBlockTileEntity tileEntity)
	{
		tileEntity.getClass().getField("randomizeLocks")
	}
	*/
}