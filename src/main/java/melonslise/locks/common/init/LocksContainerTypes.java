package melonslise.locks.common.init;

import java.util.ArrayList;
import java.util.List;

import melonslise.locks.Locks;
import melonslise.locks.common.container.KeyRingContainer;
import melonslise.locks.common.container.LockPickingContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.event.RegistryEvent;

public final class LocksContainerTypes
{
	private static final List<ContainerType> CONTAINER_TYPES = new ArrayList<ContainerType>(2);

	public static final ContainerType
		LOCK_PICKING = add("lock_picking", new ContainerType(LockPickingContainer.FACTORY)),
		KEY_RING = add("key_ring", new ContainerType(KeyRingContainer.FACTORY));

	private LocksContainerTypes() {}

	public static void register(RegistryEvent.Register<ContainerType<?>> event)
	{
		for(ContainerType type : CONTAINER_TYPES)
			event.getRegistry().register(type);
	}

	public static ContainerType add(String name, ContainerType type)
	{
		CONTAINER_TYPES.add((ContainerType) type.setRegistryName(Locks.ID, name));
		return type;
	}
}