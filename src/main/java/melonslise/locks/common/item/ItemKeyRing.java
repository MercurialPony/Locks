package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.CapabilityProvider;
import melonslise.locks.common.capability.InventoryKeyRing;
import melonslise.locks.common.container.ContainerKeyRing;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksSounds;
import melonslise.locks.utility.Lockable;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import melonslise.locks.utility.predicate.PredicateMatching;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;

// TODO Add amount of keys to tooltip
public class ItemKeyRing extends LocksItem
{
	public final int rows;

	public ItemKeyRing(String name, Properties properties, int rows)
	{
		super(name, properties);
		this.rows = rows;
		this.addPropertyOverride(new ResourceLocation(Locks.ID, "keys"), (stack, world, entity) -> 
		{
			return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(inventory ->
			{
				int keys = 0;
				for(int a = 0; a < inventory.getSlots(); ++a) if(!inventory.getStackInSlot(a).isEmpty()) ++keys;
				return (float) keys / (float) inventory.getSlots();
			}).orElse(0f);
		});
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt)
	{
		return new CapabilityProvider(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new InventoryKeyRing(stack, this.rows, 9), null);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		if(!player.world.isRemote) NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerKeyRing.Provider(player.getHeldItem(hand)), new ContainerKeyRing.Writer(hand));
		return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos position = context.getPos();
		return world.getCapability(LocksCapabilities.LOCKABLES).map(lockables ->
		{
			return context.getItem().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(inventory->
			{
				List<Lockable> intersecting = lockables.getLockables().values().stream().filter(new PredicateIntersecting(position)).collect(Collectors.toList());
				if(intersecting.isEmpty()) return ActionResultType.PASS;
				for(int a = 0; a < inventory.getSlots(); ++a)
				{
					List<Lockable> matching = intersecting.stream().filter(new PredicateMatching(ItemLocking.getID(inventory.getStackInSlot(a)))).collect(Collectors.toList());
					if(matching.isEmpty()) continue;
					for(Lockable lockable : matching) lockable.lock.setLocked(!lockable.lock.isLocked());
					world.playSound(null, position, LocksSounds.LOCK_OPEN, SoundCategory.BLOCKS, 1F, 1F);
					return ActionResultType.SUCCESS;
				}
				return ActionResultType.PASS;
			}).orElse(ActionResultType.PASS);
		}).orElse(ActionResultType.PASS);
	}
}