package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksNetwork;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Locks.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LocksModEvents
{
	private LocksModEvents() {}

	@SubscribeEvent
	public static void onSetup(FMLCommonSetupEvent e)
	{
		LocksCapabilities.register();
		LocksNetwork.register();
	}

	@SubscribeEvent
	public static void onConfigLoad(ModConfig.ModConfigEvent e)
	{
		if(e.getConfig().getSpec() == LocksConfig.SPEC)
			LocksConfig.init();
		if(e.getConfig().getSpec() == LocksServerConfig.SPEC)
			LocksServerConfig.init();
	}
}