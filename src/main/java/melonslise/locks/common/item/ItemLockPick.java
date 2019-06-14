package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import melonslise.locks.common.container.ContainerLockPicking;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.utility.Lockable;
import melonslise.locks.utility.predicate.LocksPredicates;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemLockPick extends LocksItem
{
	public final float strength;

	public ItemLockPick(String name, Properties properties, float strength)
	{
		super(name, properties);
		this.strength = strength;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		BlockPos position = context.getPos();
		return world.getCapability(LocksCapabilities.LOCKABLES).map(lockables ->
		{
			List<Lockable> matching = lockables.getLockables().values().stream().filter(LocksPredicates.LOCKED.and(new PredicateIntersecting(context.getPos()))).collect(Collectors.toList());
			if(matching.isEmpty()) return ActionResultType.PASS;
			if(world.isRemote) return ActionResultType.SUCCESS;
			NetworkHooks.openGui((ServerPlayerEntity) context.getPlayer(), new ContainerLockPicking.Provider(position, matching.get(0)), new ContainerLockPicking.Writer(position, matching.get(0)));
			return ActionResultType.SUCCESS;
		}).orElse(ActionResultType.PASS);
	}
}