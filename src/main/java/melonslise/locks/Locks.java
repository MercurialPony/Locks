package melonslise.locks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import melonslise.locks.common.command.CommandLocksDebug;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksNetworks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Locks.ID, name = Locks.NAME, version = Locks.VERSION, acceptedMinecraftVersions = Locks.GAMEVERSIONS)
public final class Locks
{
	public static final String
		ID = "locks",
		NAME = "Locks",
		VERSION = "3.0.0",
		GAMEVERSIONS = "1.12.2";

	@Mod.Instance(ID)
	public static Locks instance = null;
	
	public static boolean debug = false;
	
	public static Logger logger = LogManager.getLogger("Locks");
	
	@Mod.EventHandler
	public void onInitialization(FMLInitializationEvent event)
	{
		LocksCapabilities.register();
		LocksNetworks.register();
		LocksConfig.init();
	}
	
	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandLocksDebug());
	}
}