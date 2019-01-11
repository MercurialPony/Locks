package melonslise.locks.common.item;

import melonslise.locks.common.capability.LocksCapabilities;
import melonslise.locks.common.capability.entity.ICapabilityLockBounds;
import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.item.api.lockable.ItemLockable;
import melonslise.locks.common.sound.LocksSounds;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.Lock;
import melonslise.locks.common.world.storage.Lockable;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.LocksUtilities;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemLock extends ItemLockable
{
	public ItemLock(String name)
	{
		super(name);
	}

	public int getLength(World world)
	{
		return LocksConfiguration.getMain(world).lock_length;
	}

	// TODO Sound pitch
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos position, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		StorageLockables lockables = StorageLockables.get(world);
		ICapabilityLockBounds bounds = LocksCapabilities.getLockBounds(player);
		BlockPos position1 = bounds.get();
		if(!LocksUtilities.canLock(world, position) || lockables.contains(new PredicateIntersecting(new Box(position)))) return EnumActionResult.FAIL;
		if(position1 != null)
		{
			bounds.set(null);
			if(world.isRemote) return EnumActionResult.SUCCESS;
			ItemStack stack = player.getHeldItem(hand);
			if(!lockables.add(new Lockable(new Box(position1, position), new Lock(this.getID(stack), this.getLength(world), true), facing))) return EnumActionResult.SUCCESS;
			if(!player.isCreative()) stack.shrink(1);
			world.playSound(null, position, LocksSounds.lock_close, SoundCategory.BLOCKS, 1F, 1F);
		}
		else bounds.set(position);
		return EnumActionResult.SUCCESS;
	}
}