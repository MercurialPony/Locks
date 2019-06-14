package melonslise.locks.common.container;

import java.util.function.Consumer;

import melonslise.locks.common.container.slot.SlotKeyRing;
import melonslise.locks.common.init.LocksContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContainerKeyRing extends Container
{
	public final ItemStack stack;
	public final IItemHandler stackInventory;
	public final int rows;

	public ContainerKeyRing(int id, PlayerEntity player, ItemStack stack)
	{
		super(LocksContainerTypes.KEY_RING, id);
		this.stack = stack;
		this.stackInventory = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
		this.rows = stackInventory.getSlots() / 9;
		for(int row = 0; row < rows; ++row) for(int column = 0; column < 9; ++column) this.addSlot(new SlotKeyRing(player, stackInventory, column + row * 9, 8 + column * 18, 18 + row * 18));
		int offset = (rows - 4) * 18;
		for(int row = 0; row < 3; ++row) for (int column = 0; column < 9; ++column) this.addSlot(new Slot(player.inventory, column + row * 9 + 9, 8 + column * 18, 103 + row * 18 + offset));
		for(int column = 0; column < 9; ++column) this.addSlot(new Slot(player.inventory, column, 8 + column * 18, 161 + offset));
	}

	@Override
	public boolean canInteractWith(PlayerEntity player)
	{
		return !this.stack.isEmpty();
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int index)
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

	public static final IContainerFactory FACTORY = (id, inventory, buffer) ->
	{
		return new ContainerKeyRing(id, inventory.player, inventory.player.getHeldItem(buffer.readEnumValue(Hand.class)));
	};

	public static class Provider implements INamedContainerProvider
	{
		public final ItemStack stack;

		public Provider(ItemStack stack)
		{
			this.stack = stack;
		}

		@Override
		public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player)
		{
			return new ContainerKeyRing(id, player, this.stack);
		}

		@Override
		public ITextComponent getDisplayName()
		{
			return this.stack.getDisplayName();
		}
	}

	public static class Writer implements Consumer<PacketBuffer>
	{
		public final Hand hand;

		public Writer(Hand hand)
		{
			this.hand = hand;
		}

		@Override
		public void accept(PacketBuffer buffer)
		{
			buffer.writeEnumValue(this.hand);
		}
	}
}