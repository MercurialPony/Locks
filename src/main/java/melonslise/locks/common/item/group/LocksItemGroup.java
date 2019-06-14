package melonslise.locks.common.item.group;

import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class LocksItemGroup extends ItemGroup
{
	public LocksItemGroup()
	{
		super(Locks.ID);
	}

	@Override
	public ItemStack createIcon()
	{
		return new ItemStack(LocksItems.LOCK_PICK);
	}
}