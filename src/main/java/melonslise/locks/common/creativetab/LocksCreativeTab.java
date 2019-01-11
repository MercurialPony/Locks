package melonslise.locks.common.creativetab;

import melonslise.locks.LocksCore;
import melonslise.locks.common.item.LocksItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class LocksCreativeTab extends CreativeTabs
{
	public LocksCreativeTab()
	{
		super(LocksCore.ID);
	}

	@Override
	public ItemStack getTabIconItem()
	{
		return new ItemStack(LocksItems.lock_pick);
	}
}