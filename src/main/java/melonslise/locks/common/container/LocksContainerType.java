package melonslise.locks.common.container;

import melonslise.locks.Locks;
import net.minecraft.inventory.container.ContainerType;

public class LocksContainerType extends ContainerType
{
	public LocksContainerType(String name, IFactory factory)
	{
		super(factory);
		this.setRegistryName(Locks.ID, name);
	}
}