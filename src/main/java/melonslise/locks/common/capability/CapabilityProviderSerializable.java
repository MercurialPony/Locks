package melonslise.locks.common.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityProviderSerializable<T> extends CapabilityProvider<T> implements INBTSerializable
{
	public CapabilityProviderSerializable(Capability capability, T instance, Direction side)
	{
		super(capability, instance, side);
	}

	@Override
	public INBT serializeNBT()
	{
		return this.instance == null ? null : this.capability.writeNBT(this.instance, this.side);
	}

	@Override
	public void deserializeNBT(INBT nbt)
	{
		if(this.instance != null) this.capability.readNBT(this.instance, this.side, nbt);
	}
}