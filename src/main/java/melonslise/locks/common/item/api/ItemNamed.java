package melonslise.locks.common.item.api;

import melonslise.locks.utility.LocksUtilities;
import net.minecraft.item.Item;

public class ItemNamed extends Item
{
	public ItemNamed(String name)
	{
		this.setRegistryName(name);
		this.setUnlocalizedName(LocksUtilities.prefixLocks(name));
	}
}