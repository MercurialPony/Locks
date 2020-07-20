package melonslise.locks;

import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.config.LocksServerConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod(Locks.ID)
public final class Locks
{
	public static final String ID = "locks";

	public Locks()
	{
		ModLoadingContext.get().registerConfig(Type.SERVER, LocksServerConfig.SPEC);
		ModLoadingContext.get().registerConfig(Type.COMMON, LocksConfig.SPEC);
	}
}