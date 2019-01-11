package melonslise.locks.common.item;

import java.util.ArrayList;

import com.google.common.base.Predicates;

import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.container.ContainerLockPicking;
import melonslise.locks.common.item.api.LocksItem;
import melonslise.locks.common.network.LocksNetworks;
import melonslise.locks.common.network.client.MessageLockPicking;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.Lockable;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.LocksUtilities;
import melonslise.locks.utility.predicate.LocksSelectors;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemLockPick extends LocksItem
{
	public ItemLockPick(String name)
	{
		super(name);
	}

	public float getStrength(World world)
	{
		return LocksConfiguration.getMain(world).lock_pick_strength;
	}

	// TODO container open event
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos position, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ArrayList<Lockable> lockables = StorageLockables.get(world).matching(Predicates.and(new PredicateIntersecting(new Box(position)), LocksSelectors.LOCKED));
		if(lockables.isEmpty()) return EnumActionResult.FAIL;
		if(!(player instanceof EntityPlayerMP)) return EnumActionResult.SUCCESS;
		ContainerLockPicking container = new ContainerLockPicking(player, position, lockables.get(0));
		if(!container.canInteractWith(player)) return EnumActionResult.FAIL;
		LocksUtilities.openContainer((EntityPlayerMP) player, container);
		LocksNetworks.network.sendTo(new MessageLockPicking(container), (EntityPlayerMP) player);
		return EnumActionResult.SUCCESS;
	}
}