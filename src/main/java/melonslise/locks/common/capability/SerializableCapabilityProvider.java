package melonslise.locks.common.capability;

import net.minecraft.nbt.INBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

public class SerializableCapabilityProvider<A> extends CapabilityProvider<A> implements INBTSerializable
{
	public SerializableCapabilityProvider(Capability capability, A instance)
	{
		super(capability, instance);
	}

	@Override
	public INBT serializeNBT()
	{
		return this.instance == null ? null : this.capability.writeNBT(this.instance, null);
	}

	@Override
	public void deserializeNBT(INBT nbt)
	{
		if(this.instance != null)
			this.capability.readNBT(this.instance, null, nbt);
	}
}