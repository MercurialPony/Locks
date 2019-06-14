package melonslise.locks.common.init;

import melonslise.locks.common.capability.CapabilityLockPosition;
import melonslise.locks.common.capability.CapabilityLockables;
import melonslise.locks.common.capability.CapabilityProvider;
import melonslise.locks.common.capability.CapabilityProviderSerializable;
import melonslise.locks.common.capability.CapabilityStorage;
import melonslise.locks.common.capability.CapabilityStorageDummy;
import melonslise.locks.common.capability.ICapabilityLockPosition;
import melonslise.locks.common.capability.ICapabilityLockables;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public final class LocksCapabilities
{
	@CapabilityInject(ICapabilityLockables.class)
	public static final Capability<ICapabilityLockables> LOCKABLES = null;

	@CapabilityInject(ICapabilityLockPosition.class)
	public static final Capability<ICapabilityLockPosition> LOCK_POSITION = null;

	private LocksCapabilities() {}

	// TODO Why is the factory null?
	public static void register()
	{
		CapabilityManager.INSTANCE.register(ICapabilityLockables.class, new CapabilityStorage(), () -> null);
		CapabilityManager.INSTANCE.register(ICapabilityLockPosition.class, new CapabilityStorageDummy(), () -> null);
	}

	public static void attachToWorld(AttachCapabilitiesEvent<World> event)
	{
		event.addCapability(CapabilityLockables.ID, new CapabilityProviderSerializable(LOCKABLES, new CapabilityLockables(event.getObject()), null));
	}

	public static void attachToEntity(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof PlayerEntity) event.addCapability(CapabilityLockPosition.ID, new CapabilityProvider(LOCK_POSITION, new CapabilityLockPosition(), null));
	}
}