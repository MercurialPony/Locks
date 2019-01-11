package melonslise.locks.common.item;

import java.util.UUID;

import com.google.common.base.Predicates;

import melonslise.locks.common.item.api.lockable.ItemLockable;
import melonslise.locks.common.sound.LocksSounds;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import melonslise.locks.utility.predicate.PredicateMatching;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemKey extends ItemLockable
{
	public ItemKey(String name)
	{
		super(name);
	}

	// TODO Sound helper and random pitch?
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos position, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		Box box = new Box(position);
		UUID id = this.getID(player.getHeldItem(hand));
		if(world.isRemote) { if(!StorageLockables.get(world).contains(Predicates.and(new PredicateIntersecting(box), new PredicateMatching(id)))) return EnumActionResult.FAIL; }
		else if(StorageLockables.get(world).toggle(box, id).isEmpty()) return EnumActionResult.FAIL;
		world.playSound(player, position, LocksSounds.lock_open, SoundCategory.BLOCKS, 1F, 1F);
		return EnumActionResult.SUCCESS;
	}
}