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
		public void set(ItemStack stack)
		{
			super.set(stack);
			if(!this.player.level.isClientSide)
				this.player.level.playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(), LocksSoundEvents.KEY_RING.get(), SoundCategory.PLAYERS, 1f, 1f);
		}

		@Override
		public ItemStack onTake(PlayerEntity player, ItemStack stack)
		{
			if(!this.player.level.isClientSide)
				this.player.level.playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(), LocksSoundEvents.KEY_RING.get(), SoundCategory.PLAYERS, 1f, 1f);
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
			for(int col = 0; col < 9; ++col)
				this.addSlot(new KeyRingSlot(player, inv, col + row * 9, 8 + col * 18, 18 + row * 18));

		int offset = (rows - 4) * 18;
		for(int row = 0; row < 3; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new Slot(player.inventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + offset));

		for(int coll = 0; coll < 9; ++coll)
			this.addSlot(new Slot(player.inventory, coll, 8 + coll * 18, 161 + offset));
	}

	@Override
	public boolean stillValid(PlayerEntity player)
	{
		return !this.stack.isEmpty();
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if(slot == null || !slot.hasItem())
			return stack;
		ItemStack stack1 = slot.getItem();
		stack = stack1.copy();
		if(index < this.inv.getSlots())
		{
			if(!this.moveItemStackTo(stack1, this.inv.getSlots(), this.slots.size(), true))
				return ItemStack.EMPTY;
		}
		else if(!this.moveItemStackTo(stack1, 0, this.inv.getSlots(), false))
			return ItemStack.EMPTY;
		if(stack1.isEmpty())
			slot.set(ItemStack.EMPTY);
		else
			slot.setChanged();
		return stack;
	}

	public static final IContainerFactory<KeyRingContainer> FACTORY = (id, inv, buffer) ->
	{
		return new KeyRingContainer(id, inv.player, inv.player.getItemInHand(buffer.readEnum(Hand.class)));
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
			return this.stack.getHoverName();
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
			buffer.writeEnum(this.hand);
		}
	}
}