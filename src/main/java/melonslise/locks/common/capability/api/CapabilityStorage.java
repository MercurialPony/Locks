package melonslise.locks.common.capability.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityStorage<T extends INBTSerializable> implements IStorage<T>
{
	@Override
	public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side)
	{
		return instance.serializeNBT();
	}

	@Override
	public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt)
	{
		instance.deserializeNBT(nbt);
	}
}