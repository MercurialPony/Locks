package melonslise.locks.common.capability;

import javax.annotation.Nonnull;

import melonslise.locks.common.init.LocksItems;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

// Thanks to Gigaherz
public class InventoryKeyRing implements IItemHandlerModifiable
{
	public final int size;
	public final ItemStack stack;

	public InventoryKeyRing(ItemStack stack, int rows, int columns)
	{
		this.size = rows * columns;
		this.stack = stack;
	}

	@Override
	public int getSlots()
	{
		return this.size;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		this.validateSlotIndex(slot);
		ListNBT list = LocksUtilities.getTag(this.stack).getList("Items", Constants.NBT.TAG_COMPOUND);
		for(int a = 0; a < list.size(); a++)
		{
			CompoundNBT nbt = list.getCompound(a);
			if(nbt.getInt("Slot") != slot) continue;
			return ItemStack.read(nbt);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		this.validateSlotIndex(slot);
		CompoundNBT nbt = null;
		if(!stack.isEmpty())
		{
			nbt = new CompoundNBT();
			nbt.putInt("Slot", slot);
			stack.write(nbt);
		}
		ListNBT list = LocksUtilities.getTag(this.stack).getList("Items", Constants.NBT.TAG_COMPOUND);
		for(int a = 0; a < list.size(); a++)
		{
			CompoundNBT existing = list.getCompound(a);
			if(existing.getInt("Slot") != slot) continue;
			if(!stack.isEmpty()) list.set(a, nbt);
			else list.remove(a);
			return;
		}
		if(!stack.isEmpty()) list.add(nbt);
		LocksUtilities.getTag(this.stack).put("Items", list);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())return ItemStack.EMPTY;
		this.validateSlotIndex(slot);
		ItemStack existing = getStackInSlot(slot);
		int limit = stack.getMaxStackSize();
		if (!existing.isEmpty())
		{
			if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) return stack;
			limit -= existing.getCount();
		}
		if (limit <= 0) return stack;
		boolean reachedLimit = stack.getCount() > limit;
		if (!simulate)
		{
			if (existing.getCount() <= 0) existing = reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack;
			else existing.grow(reachedLimit ? limit : stack.getCount());
			this.setStackInSlot(slot, existing);
		}
		return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (amount == 0) return ItemStack.EMPTY;
		this.validateSlotIndex(slot);
		ItemStack existing = this.getStackInSlot(slot);
		if (existing.isEmpty()) return ItemStack.EMPTY;
		int toExtract = Math.min(amount, existing.getMaxStackSize());
		if (existing.getCount() <= toExtract)
		{
			if (!simulate) this.setStackInSlot(slot, ItemStack.EMPTY);
			return existing;
		}
		else
		{
			if (!simulate) this.setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
			return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
		}
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	private void validateSlotIndex(int slot)
	{
		if (slot < 0 || slot >= this.getSlots()) throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
	}

	// TODO Don't hard code item
	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		return stack.getItem() == LocksItems.KEY;
	}
}