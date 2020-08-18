package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksFeatures;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.init.LocksPlacements;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Locks.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LocksModEvents
{
	private LocksModEvents() {}

	@SubscribeEvent
	public static void onSetup(FMLCommonSetupEvent event)
	{
		LocksCapabilities.register();
		LocksNetworks.register();
		LocksFeatures.addFeatures();
	}

	@SubscribeEvent
	public static void onConfigLoad(ModConfig.ModConfigEvent event)
	{
		if(event.getConfig().getSpec() == LocksServerConfig.SPEC)
			LocksServerConfig.load();
	}
}