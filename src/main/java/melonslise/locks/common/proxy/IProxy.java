package melonslise.locks.common.proxy;

import melonslise.locks.common.capability.ILockableStorage;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public interface IProxy
{
	LazyOptional<ILockableStorage> getLockables(World world);
}