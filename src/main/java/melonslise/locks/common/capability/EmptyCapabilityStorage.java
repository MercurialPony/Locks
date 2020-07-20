package melonslise.locks.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class EmptyCapabilityStorage<A> implements IStorage<A>
{
	@Override
	public NBTBase writeNBT(Capability<A> cap, A inst, EnumFacing side)
	{
		return null;
	}

	@Override
	public void readNBT(Capability<A> cap, A inst, EnumFacing side, NBTBase nbt) {}
}