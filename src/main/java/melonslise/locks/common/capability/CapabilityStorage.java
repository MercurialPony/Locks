package melonslise.locks.common.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityStorage<T extends INBTSerializable> implements IStorage<T>
{
	@Override
	public INBT writeNBT(Capability<T> capability, T instance, Direction side)
	{
		return instance.serializeNBT();
	}

	@Override
	public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt)
	{
		instance.deserializeNBT(nbt);
	}
}