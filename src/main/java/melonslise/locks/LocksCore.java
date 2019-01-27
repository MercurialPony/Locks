package melonslise.locks;

import melonslise.locks.common.capability.LocksCapabilities;
import melonslise.locks.common.network.LocksNetworks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = LocksCore.ID, name = LocksCore.NAME, version = LocksCore.VERSION, acceptedMinecraftVersions = LocksCore.GAMEVERSIONS)
public class LocksCore
{
	public static final String ID = "locks", NAME = "Locks", VERSION = "2.2.1", GAMEVERSIONS = "1.12.2";

	@Mod.EventHandler
	public void onInitialization(FMLInitializationEvent event)
	{
		LocksNetworks.registerMessages();
		LocksCapabilities.register();
	}
}