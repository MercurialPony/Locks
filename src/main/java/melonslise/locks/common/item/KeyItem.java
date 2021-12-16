package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class KeyItem extends LockingItem
{
	// TODO Sound pitch
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		int id = getOrSetId(player.getHeldItem(hand));

		List<Lockable> matching = LocksUtil.intersecting(world, pos).filter(lockable1 -> lockable1.lock.id == id).collect(Collectors.toList());
		if(matching.isEmpty())
			return EnumActionResult.PASS;
		world.playSound(player, pos, LocksSoundEvents.LOCK_OPEN, SoundCategory.BLOCKS, 1F, 1F);
		if(world.isRemote)
			return EnumActionResult.SUCCESS;
		for(Lockable lockable : matching)
			lockable.lock.setLocked(!lockable.lock.isLocked());
		return EnumActionResult.SUCCESS;
	}
}