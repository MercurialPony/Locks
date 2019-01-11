package melonslise.locks.common.capability;

import javax.annotation.Nonnull;

import melonslise.locks.common.item.ItemKey;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class InventoryKeyRing extends ItemStackHandler
{
	public InventoryKeyRing(int rows, int columns)
	{
		super(rows * columns);
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
    	// Compare to LocksItems.key instead?
    	return stack.getItem() instanceof ItemKey;
    }
}