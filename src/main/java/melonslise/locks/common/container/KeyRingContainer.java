package melonslise.locks.common.container;

import melonslise.locks.common.init.LocksSoundEvents;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class KeyRingContainer extends Container
{
	public static class KeyRingSlot extends SlotItemHandler
	{
		public final EntityPlayer player;

		public KeyRingSlot(EntityPlayer player, IItemHandler inv, int index, int x, int y)
		{
			super(inv, index, x, y);
			this.player = player;
		}

		// TODO PITCH
		@Override
		public void putStack(ItemStack stack)
		{
			super.putStack(stack);
			if(!this.player.world.isRemote)
				this.player.world.playSound(null, this.player.posX, this.player.posY, this.player.posZ, LocksSoundEvents.KEY_RING, SoundCategory.PLAYERS, 1f, 1f);
		}

		@Override
		public ItemStack onTake(EntityPlayer player, ItemStack stack)
		{
			if(!this.player.world.isRemote)
				this.player.world.playSound(null, this.player.posX, this.player.posY, this.player.posZ, LocksSoundEvents.KEY_RING, SoundCategory.PLAYERS, 1f, 1f);
			return super.onTake(player, stack);
		}
	}

	public final EntityPlayer player;
	public final ItemStack stack;
	public final IItemHandler inv;
	public final int rows;

	public KeyRingContainer(EntityPlayer player, ItemStack stack)
	{
		this.player = player;
		this.stack = stack;
		this.inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		this.rows = inv.getSlots() / 9;
		for(int row = 0; row < rows; ++row)
			for(int column = 0; column < 9; ++column)
				this.addSlotToContainer(new KeyRingSlot(player, inv, column + row * 9, 8 + column * 18, 18 + row * 18));
		int offset = (rows - 4) * 18;
		for(int row = 0; row < 3; ++row)
			for (int column = 0; column < 9; ++column)
				this.addSlotToContainer(new Slot(player.inventory, column + row * 9 + 9, 8 + column * 18, 103 + row * 18 + offset));
		for(int column = 0; column < 9; ++column)
			this.addSlotToContainer(new Slot(player.inventory, column, 8 + column * 18, 161 + offset));
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return !stack.isEmpty();
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if(slot == null || !slot.getHasStack())
			return stack;
		ItemStack stack1 = slot.getStack();
		stack = stack1.copy();
		if(index < this.inv.getSlots())
		{
			if(!this.mergeItemStack(stack1, this.inv.getSlots(), this.inventorySlots.size(), true))
				return ItemStack.EMPTY;
		}
		else if(!this.mergeItemStack(stack1, 0, this.inv.getSlots(), false))
			return ItemStack.EMPTY;
		if(stack1.isEmpty())
			slot.putStack(ItemStack.EMPTY);
		else
			slot.onSlotChanged();
		return stack;
	}
}