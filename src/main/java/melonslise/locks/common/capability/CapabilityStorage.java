package melonslise.locks.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityStorage<A extends INBTSerializable> implements IStorage<A>
{
	@Override
	public NBTBase writeNBT(Capability<A> cap, A inst, EnumFacing side)
	{
		return inst.serializeNBT();
	}

	@Override
	public void readNBT(Capability<A> cap, A inst, EnumFacing side, NBTBase nbt)
	{
		inst.deserializeNBT(nbt);
	}
}