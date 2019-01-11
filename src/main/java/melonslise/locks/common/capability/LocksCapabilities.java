package melonslise.locks.common.capability;

import melonslise.locks.common.capability.api.CapabilityProvider;
import melonslise.locks.common.capability.api.CapabilityStorageDummy;
import melonslise.locks.common.capability.entity.CapabilityLockBounds;
import melonslise.locks.common.capability.entity.ICapabilityLockBounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class LocksCapabilities
{
	@CapabilityInject(ICapabilityLockBounds.class)
	public static Capability<ICapabilityLockBounds> lockBounds;

	private LocksCapabilities() {}

	// TODO Factories
	public static void register()
	{
		CapabilityManager.INSTANCE.register(ICapabilityLockBounds.class, new CapabilityStorageDummy(), CapabilityLockBounds.class);
	}

	public static void attach(AttachCapabilitiesEvent event)
	{
		if(event.getObject() instanceof EntityPlayer) event.addCapability(CapabilityLockBounds.ID, new CapabilityProvider(LocksCapabilities.lockBounds, new CapabilityLockBounds(), null));
	}

	public static ICapabilityLockBounds getLockBounds(EntityPlayer player)
	{
		return player.getCapability(LocksCapabilities.lockBounds, null);
	}

	public static IItemHandler getInventory(ItemStack stack)
	{
		return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
	}
}