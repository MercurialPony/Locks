package melonslise.locks.client.proxy;

import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.proxy.IProxy;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

@OnlyIn(Dist.CLIENT)
public class ClientProxy implements IProxy
{
	// FIXME Forge 1.16 broke client world capabilities (???) so I had to use this utterly hideous and ugly hack. FIX ASAP
	public static LazyOptional<ILockableStorage> CLIENT_LOCKABLES;

	@Override
	public LazyOptional<ILockableStorage> getLockables(World world)
	{
		return !world.isRemote ? world.getCapability(LocksCapabilities.LOCKABLES) : CLIENT_LOCKABLES;
	}
}