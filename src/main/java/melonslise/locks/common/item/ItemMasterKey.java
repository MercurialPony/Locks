package melonslise.locks.common.item;

import melonslise.locks.common.item.api.LocksItem;
import melonslise.locks.common.sound.LocksSounds;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMasterKey extends LocksItem
{
	public ItemMasterKey(String name)
	{
		super(name);
		this.setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos position, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		Box box = new Box(position);
		if(world.isRemote) { if(!StorageLockables.get(world).contains(new PredicateIntersecting(box))) return EnumActionResult.FAIL; }
		else if(StorageLockables.get(world).toggle(box).isEmpty()) return EnumActionResult.FAIL;
		world.playSound(player, position, LocksSounds.lock_open, SoundCategory.BLOCKS, 1F, 1F);
		return EnumActionResult.SUCCESS;
	}
}