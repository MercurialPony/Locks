package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksSounds;
import melonslise.locks.utility.Lockable;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMasterKey extends LocksItem
{
	public ItemMasterKey(String name, Properties properties)
	{
		super(name, properties);
	}

	// TODO Sound pitch
	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos position = context.getPos();
		return world.getCapability(LocksCapabilities.LOCKABLES).map(lockables ->
		{
			List<Lockable> matching = lockables.getLockables().values().stream().filter(new PredicateIntersecting(position)).collect(Collectors.toList());
			if(matching.isEmpty()) return ActionResultType.PASS;
			for(Lockable lockable : matching) lockable.lock.setLocked(!lockable.lock.isLocked());
			world.playSound(null, position, LocksSounds.LOCK_OPEN, SoundCategory.BLOCKS, 1F, 1F);
			return ActionResultType.SUCCESS;
		}).orElse(ActionResultType.PASS);
	}
}