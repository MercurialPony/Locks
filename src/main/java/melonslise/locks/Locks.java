package melonslise.locks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import melonslise.locks.common.config.LocksClientConfig;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksContainerTypes;
import melonslise.locks.common.init.LocksEnchantments;
import melonslise.locks.common.init.LocksFeatures;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksPlacements;
import melonslise.locks.common.init.LocksRecipeSerializers;
import melonslise.locks.common.init.LocksSoundEvents;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod(Locks.ID)
public final class Locks
{
	public static final String ID = "locks";

	public static final Logger LOGGER = LogManager.getLogger();

	public Locks()
	{
		ModLoadingContext.get().registerConfig(Type.SERVER, LocksServerConfig.SPEC);
		ModLoadingContext.get().registerConfig(Type.COMMON, LocksConfig.SPEC);
		ModLoadingContext.get().registerConfig(Type.CLIENT, LocksClientConfig.SPEC);

		LocksItems.register();
		LocksEnchantments.register();
		LocksSoundEvents.register();
		LocksPlacements.register();
		LocksFeatures.register();
		LocksContainerTypes.register();
		LocksRecipeSerializers.register();
	}
}