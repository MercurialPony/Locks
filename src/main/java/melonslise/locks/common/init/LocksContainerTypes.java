package melonslise.locks.common.init;

import java.util.ArrayList;

import melonslise.locks.common.container.ContainerKeyRing;
import melonslise.locks.common.container.ContainerLockPicking;
import melonslise.locks.common.container.LocksContainerType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.event.RegistryEvent;

public final class LocksContainerTypes
{
	private static final ArrayList<ContainerType> types = new ArrayList<ContainerType>();

	public static final ContainerType
	LOCK_PICKING = add(new LocksContainerType("lock_picking", ContainerLockPicking.FACTORY)),
	KEY_RING = add(new LocksContainerType("key_ring", ContainerKeyRing.FACTORY));

	private LocksContainerTypes() {}

	public static void register(RegistryEvent.Register<ContainerType<?>> event)
	{
		for(ContainerType type : types) event.getRegistry().register(type);
	}

	public static LocksContainerType add(LocksContainerType type)
	{
		types.add(type);
		return type;
	}
}