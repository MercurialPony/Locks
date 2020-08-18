package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.container.KeyRingContainer;
import melonslise.locks.common.container.LockPickingContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class LocksContainerTypes
{
	public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, Locks.ID);

	public static final RegistryObject<ContainerType<LockPickingContainer>>
		LOCK_PICKING = add("lock_picking", new ContainerType(LockPickingContainer.FACTORY));

	public static final RegistryObject<ContainerType<KeyRingContainer>>
		KEY_RING = add("key_ring", new ContainerType(KeyRingContainer.FACTORY));

	private LocksContainerTypes() {}

	public static void register()
	{
		CONTAINER_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public static <T extends Container> RegistryObject<ContainerType<T>> add(String name, ContainerType<T> type)
	{
		return CONTAINER_TYPES.register(name, () -> type);
	}
}