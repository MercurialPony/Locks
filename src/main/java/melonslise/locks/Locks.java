package melonslise.locks;

import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksNetworks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = Locks.ID, name = Locks.NAME, version = Locks.VERSION, acceptedMinecraftVersions = Locks.GAMEVERSIONS)
public final class Locks
{
	public static final String
		ID = "locks",
		NAME = "Locks",
		VERSION = "2.5",
		GAMEVERSIONS = "1.12.2";

	@Mod.Instance(ID)
	public static Locks instance = null;

	@Mod.EventHandler
	public void onInitialization(FMLInitializationEvent event)
	{
		LocksCapabilities.register();
		LocksNetworks.register();
	}
}