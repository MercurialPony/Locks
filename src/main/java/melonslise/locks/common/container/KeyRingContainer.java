package melonslise.locks.common.container;

import java.util.function.Consumer;

import melonslise.locks.common.init.LocksContainerTypes;
import melonslise.locks.common.init.LocksSoundEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class KeyRingContainer extends Container
{
	public static class KeyRingSlot extends SlotItemHandler
	{
		public final PlayerEntity player;

		public KeyRingSlot(PlayerEntity player, IItemHandler inv, int index, int x, int y)
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
				this.player.world.playSound(null, this.player.getPosX(), this.player.getPosY(), this.player.getPosZ(), LocksSoundEvents.KEY_RING.get(), SoundCategory.PLAYERS, 1f, 1f);
		}

		@Override
		public ItemStack onTake(PlayerEntity player, ItemStack stack)
		{
			if(!this.player.world.isRemote)
				this.player.world.playSound(null, this.player.getPosX(), this.player.getPosY(), this.player.getPosZ(), LocksSoundEvents.KEY_RING.get(), SoundCategory.PLAYERS, 1f, 1f);
			return super.onTake(player, stack);
		}
	}

	public final ItemStack stack;
	public final IItemHandler inv;
	public final int rows;

	public KeyRingContainer(int id, PlayerEntity player, ItemStack stack)
	{
		super(LocksContainerTypes.KEY_RING.get(), id);
		this.stack = stack;
		this.inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
		this.rows = inv.getSlots() / 9;
		for(int row = 0; row < rows; ++row)
			for(int column = 0; column < 9; ++column)
				this.addSlot(new KeyRingSlot(player, inv, column + row * 9, 8 + column * 18, 18 + row * 18));
		int offset = (rows - 4) * 18;
		for(int row = 0; row < 3; ++row)
			for (int column = 0; column < 9; ++column)
				this.addSlot(new Slot(player.inventory, column + row * 9 + 9, 8 + column * 18, 103 + row * 18 + offset));
		for(int column = 0; column < 9; ++column)
			this.addSlot(new Slot(player.inventory, column, 8 + column * 18, 161 + offset));
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

	public static final IContainerFactory<KeyRingContainer> FACTORY = (id, inv, buffer) ->
	{
		return new KeyRingContainer(id, inv.player, inv.player.getHeldItem(buffer.readEnumValue(Hand.class)));
	};

	public static class Provider implements INamedContainerProvider
	{
		public final ItemStack stack;

		public Provider(ItemStack stack)
		{
			this.stack = stack;
		}

		@Override
		public Container createMenu(int id, PlayerInventory inv, PlayerEntity player)
		{
			return new KeyRingContainer(id, player, this.stack);
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