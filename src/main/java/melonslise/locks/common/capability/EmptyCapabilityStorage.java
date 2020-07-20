package melonslise.locks.common.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class EmptyCapabilityStorage<A> implements IStorage<A>
{
	@Override
	public INBT writeNBT(Capability<A> capability, A instance, Direction side)
	{
		return null;
	}

	@Override
	public void readNBT(Capability<A> capability, A instance, Direction side, INBT nbt) {}
}