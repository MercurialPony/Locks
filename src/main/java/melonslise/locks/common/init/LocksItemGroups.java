package melonslise.locks.common.init;

import melonslise.locks.Locks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class LocksItemGroups
{
	private LocksItemGroups() {}

	public static final ItemGroup TAB = new ItemGroup(Locks.ID)
	{
		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(LocksItems.LOCK_PICK);
		}
	};
}