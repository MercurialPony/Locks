package melonslise.locks.server.proxy;

import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.proxy.IProxy;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerProxy implements IProxy
{
	@Override
	public LazyOptional<ILockableStorage> getLockables(World world)
	{
		return world.getCapability(LocksCapabilities.LOCKABLES);
	}
}