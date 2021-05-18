package melonslise.locks.common.capability;

import net.minecraft.nbt.INBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

public class SerializableCapabilityProvider<A> extends CapabilityProvider<A> implements INBTSerializable
{
	public SerializableCapabilityProvider(Capability<A> cap, A inst)
	{
		super(cap, inst);
	}

	@Override
	public INBT serializeNBT()
	{
		return this.cap.writeNBT(this.inst, null);
	}

	@Override
	public void deserializeNBT(INBT nbt)
	{
		this.cap.readNBT(this.inst, null, nbt);
	}
}