package melonslise.locks.common.init;

import melonslise.locks.Locks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class LocksCreativeTabs
{
	private LocksCreativeTabs() {}

	public static final CreativeTabs TAB = (new CreativeTabs(Locks.ID)
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return new ItemStack(LocksItems.IRON_LOCK);
		}
	}).setRelevantEnchantmentTypes(LocksEnchantments.LOCK_TYPE);
}