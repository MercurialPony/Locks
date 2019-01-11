package melonslise.locks.common.world;

import io.netty.util.internal.ThreadLocalRandom;
import melonslise.locks.common.item.LocksItems;
import melonslise.locks.common.item.api.lockable.ItemLockable;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.Lockable;
import melonslise.locks.common.world.storage.StorageLockables;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

public class ListenerLockables implements IWorldEventListener
{
	@Override
	public void notifyBlockUpdate(World world, BlockPos position, IBlockState oldState, IBlockState newState, int flags)
	{
		if(oldState.getBlock() == newState.getBlock() || world.isRemote) return;
		for(Lockable lockable : StorageLockables.get(world).remove(new Box(position)))
		{
			world.playSound(null, position, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 0.8F, 0.8F + ThreadLocalRandom.current().nextFloat() * 0.4F);
			world.spawnEntity(new EntityItem(world, (double) position.getX() + 0.5D, (double) position.getY() + 0.5D, (double) position.getZ() + 0.5D, ItemLockable.assignID(new ItemStack(LocksItems.lock), lockable.lock.id)));
		}
	}

	@Override
	public void notifyLightSet(BlockPos position) {}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}

	@Override
	public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent sound, SoundCategory category, double x, double y, double z, float volume, float pitch) {}

	@Override
	public void playRecord(SoundEvent sound, BlockPos position) {}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}

	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}

	@Override
	public void onEntityAdded(Entity entity) {}

	@Override
	public void onEntityRemoved(Entity entity) {}

	@Override
	public void broadcastSound(int soundID, BlockPos position, int data) {}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos position, int data) {}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos position, int progress) {}
}