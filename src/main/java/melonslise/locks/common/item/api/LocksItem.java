package melonslise.locks.common.item.api;

import melonslise.locks.common.creativetab.LocksCreativeTabs;

public class LocksItem extends ItemNamed
{
	public LocksItem(String name)
	{
		super(name);
		this.setCreativeTab(LocksCreativeTabs.tab);
	}
}