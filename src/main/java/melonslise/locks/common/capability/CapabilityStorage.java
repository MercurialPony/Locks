package melonslise.locks.common.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityStorage<A extends INBTSerializable> implements IStorage<A>
{
	@Override
	public INBT writeNBT(Capability<A> capability, A instance, Direction side)
	{
		return instance.serializeNBT();
	}

	@Override
	public void readNBT(Capability<A> capability, A instance, Direction side, INBT nbt)
	{
		instance.deserializeNBT(nbt);
	}
}