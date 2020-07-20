package melonslise.locks.common.init;

import melonslise.locks.common.capability.CapabilityProvider;
import melonslise.locks.common.capability.CapabilityStorage;
import melonslise.locks.common.capability.EmptyCapabilityStorage;
import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.capability.ISelection;
import melonslise.locks.common.capability.LockableStorage;
import melonslise.locks.common.capability.Selection;
import melonslise.locks.common.capability.SerializableCapabilityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public final class LocksCapabilities
{
	@CapabilityInject(ILockableStorage.class)
	public static final Capability<ILockableStorage> LOCKABLES = null;

	@CapabilityInject(ISelection.class)
	public static final Capability<ISelection> LOCK_SELECTION = null;

	private LocksCapabilities() {}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(ILockableStorage.class, new CapabilityStorage(), () -> null);
		CapabilityManager.INSTANCE.register(ISelection.class, new EmptyCapabilityStorage(), Selection::new);
	}

	public static void attachToWorld(AttachCapabilitiesEvent<World> event)
	{
		event.addCapability(LockableStorage.ID, new SerializableCapabilityProvider(LOCKABLES, new LockableStorage(event.getObject())));
	}

	public static void attachToEntity(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof PlayerEntity)
			event.addCapability(Selection.ID, new CapabilityProvider(LOCK_SELECTION, new Selection()));
	}
}