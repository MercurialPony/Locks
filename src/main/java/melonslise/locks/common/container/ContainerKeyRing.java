package melonslise.locks.common.container;

import melonslise.locks.common.capability.LocksCapabilities;
import melonslise.locks.common.container.slot.SlotKeyRing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerKeyRing extends Container
{
	public final IItemHandler stackInventory;
	public final InventoryPlayer playerInventory;
	public final ItemStack stack;
	public final EntityPlayer player;
	public final int rows;

	// TODO Not only multiples of 9
	public ContainerKeyRing(EntityPlayer player, ItemStack stack)
	{
		this.player = player;
		this.stack = stack;
		this.playerInventory = player.inventory;
		this.stackInventory = LocksCapabilities.getInventory(stack);
		this.rows = stackInventory.getSlots() / 9;
		for(int row = 0; row < rows; ++row) for(int column = 0; column < 9; ++column) this.addSlotToContainer(new SlotKeyRing(player, this.stackInventory, column, 8 + column * 18, 18));
		int offset = (rows - 4) * 18;
		for(int row = 0; row < 3; ++row) for (int column = 0; column < 9; ++column) this.addSlotToContainer(new Slot(this.playerInventory, column + row * 9 + 9, 8 + column * 18, 103 + row * 18 + offset));
		for(int column = 0; column < 9; ++column) this.addSlotToContainer(new Slot(this.playerInventory, column, 8 + column * 18, 161 + offset));
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if(slot == null || !slot.getHasStack()) return stack;
		ItemStack stack1 = slot.getStack();
		stack = stack1.copy();
		if(index < this.stackInventory.getSlots()) { if(!this.mergeItemStack(stack1, this.stackInventory.getSlots(), this.inventorySlots.size(), true)) return ItemStack.EMPTY; }
		else if(!this.mergeItemStack(stack1, 0, this.stackInventory.getSlots(), false)) return ItemStack.EMPTY;
		if(stack1.isEmpty()) slot.putStack(ItemStack.EMPTY);
		else slot.onSlotChanged();
		return stack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return !stack.isEmpty();
	}
}