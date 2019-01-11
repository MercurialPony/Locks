package melonslise.locks.common.capability.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityProviderSerializable extends CapabilityProvider implements INBTSerializable
{
	public CapabilityProviderSerializable(Capability capability, Object instance, EnumFacing side)
	{
		super(capability, instance, side);
	}

	@Override
	public NBTBase serializeNBT()
	{
		return this.capability.getStorage().writeNBT(this.capability, this.instance, this.side);
	}

	@Override
	public void deserializeNBT(NBTBase nbt)
	{
		this.capability.getStorage().readNBT(this.capability, this.instance, this.side, nbt);
	}
}