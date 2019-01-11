package melonslise.locks.common.item;

import java.util.UUID;

import com.google.common.base.Predicates;

import melonslise.locks.common.capability.InventoryKeyRing;
import melonslise.locks.common.capability.LocksCapabilities;
import melonslise.locks.common.capability.api.CapabilityProviderSerializable;
import melonslise.locks.common.container.ContainerKeyRing;
import melonslise.locks.common.item.api.LocksItem;
import melonslise.locks.common.item.api.lockable.ItemLockable;
import melonslise.locks.common.network.LocksNetworks;
import melonslise.locks.common.network.client.MessageKeyRing;
import melonslise.locks.common.sound.LocksSounds;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.LocksUtilities;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import melonslise.locks.utility.predicate.PredicateMatching;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemKeyRing extends LocksItem
{
	public final int rows;

	public ItemKeyRing(String name, int rows)
	{
		super(name);
		this.setMaxStackSize(1);
		this.rows = rows;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		return new CapabilityProviderSerializable(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new InventoryKeyRing(this.rows, 9), null);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!(player instanceof EntityPlayerMP)) return ActionResult.newResult(EnumActionResult.PASS, stack);
		ContainerKeyRing container = new ContainerKeyRing(player, stack);
		if(!container.canInteractWith(player)) return ActionResult.newResult(EnumActionResult.PASS, stack);
		LocksUtilities.openContainer((EntityPlayerMP) player, container);
		LocksNetworks.network.sendTo(new MessageKeyRing(container, hand), (EntityPlayerMP) player);
		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	// TODO Simplify
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos position, EnumHand hand, EnumFacing facing, float x, float y, float z)
	{
		ItemStack stack = player.getHeldItem(hand);
		StorageLockables lockables = StorageLockables.get(world);
		IItemHandler inventory = LocksCapabilities.getInventory(stack);
		Box box = new Box(position);
		boolean toggled = false;
		for(int a = 0; a < inventory.getSlots(); ++a)
		{
			UUID id = ItemLockable.getID(inventory.getStackInSlot(a));
			if(world.isRemote) { if(StorageLockables.get(world).contains(Predicates.and(new PredicateIntersecting(box), new PredicateMatching(id)))) toggled = true; }
			else if(!StorageLockables.get(world).toggle(box, id).isEmpty()) toggled = true;
		}
		if(toggled) world.playSound(player, position, LocksSounds.lock_open, SoundCategory.BLOCKS, 1F, 1F);;
		return toggled ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
	}
}