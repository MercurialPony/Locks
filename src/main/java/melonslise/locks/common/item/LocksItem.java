package melonslise.locks.common.item;

import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksItemGroups;
import net.minecraft.item.Item;

public class LocksItem extends Item
{
	public LocksItem(String name, Properties properties)
	{
		super(properties.group(LocksItemGroups.MAIN));
		this.setRegistryName(Locks.ID, name);
	}
}