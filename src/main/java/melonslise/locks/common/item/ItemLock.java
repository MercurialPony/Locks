package melonslise.locks.common.item;

import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksSounds;
import melonslise.locks.utility.Box;
import melonslise.locks.utility.Lock;
import melonslise.locks.utility.Lockable;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemLock extends ItemLocking
{
	public final int length;

	public ItemLock(String name, Properties properties, int length)
	{
		super(name, properties);
		this.length = length;
	}

	// TODO Sound pitch
	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		BlockPos position = context.getPos();
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		ItemStack stack = context.getItem();

		return world.getCapability(LocksCapabilities.LOCKABLES).map(lockables ->
		{
			return player.getCapability(LocksCapabilities.LOCK_POSITION).map(lockPosition ->
			{
				if(!LocksConfiguration.MAIN.canLock(world, position) || lockables.getLockables().values().stream().anyMatch(new PredicateIntersecting(position))) return ActionResultType.PASS;
				BlockPos position1 = lockPosition.get();
				if(position1 == null) lockPosition.set(position);
				else
				{
					lockPosition.set(null);
					// TODO Go through the add checks here as well
					if(world.isRemote) return ActionResultType.SUCCESS;
					if(!lockables.add(new Lockable(new Box(position1, position), this.createLock(stack), context.getFace()))) return ActionResultType.PASS;
					if(!player.isCreative()) stack.shrink(1);
					world.playSound(null, position, LocksSounds.LOCK_CLOSE, SoundCategory.BLOCKS, 1F, 1F);
				}
				return ActionResultType.SUCCESS;
			}).orElse(ActionResultType.PASS);
		}).orElse(ActionResultType.PASS);
	}

	public Lock createLock(ItemStack stack)
	{
		return new Lock(getID(stack), this.length, true);
	}
}