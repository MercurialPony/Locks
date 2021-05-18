package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MasterKeyItem extends Item
{
	public MasterKeyItem(Properties props)
	{
		super(props.stacksTo(1));
	}

	// TODO Sound pitch
	@Override
	public ActionResultType useOn(ItemUseContext ctx)
	{
		World world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		List<Lockable> match = LocksUtil.intersecting(world, pos).collect(Collectors.toList());
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