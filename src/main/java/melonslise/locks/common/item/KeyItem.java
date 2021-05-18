package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class KeyItem extends LockingItem
{
	public KeyItem(Properties props)
	{
		super(props);
	}

	// TODO Sound pitch
	@Override
	public ActionResultType useOn(ItemUseContext ctx)
	{
		World world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		int id = getOrSetId(ctx.getItemInHand());
		List<Lockable> match = LocksUtil.intersecting(world, pos).filter(lkb -> lkb.lock.id == id).collect(Collectors.toList());
		if(match.isEmpty())
			return ActionResultType.PASS;
		world.playSound(ctx.getPlayer(), pos, LocksSoundEvents.LOCK_OPEN.get(), SoundCategory.BLOCKS, 1f, 1f);
		if(world.isClientSide)
			return ActionResultType.SUCCESS;
		for(Lockable lkb : match)
			lkb.lock.setLocked(!lkb.lock.isLocked());
		return ActionResultType.SUCCESS;
	}
}