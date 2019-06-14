package melonslise.locks.common.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class CapabilityStorageDummy implements IStorage
{
	@Override
	public INBT writeNBT(Capability capability, Object instance, Direction side)
	{
		return null;
	}

	@Override
	public void readNBT(Capability capability, Object instance, Direction side, INBT nbt) {}
}