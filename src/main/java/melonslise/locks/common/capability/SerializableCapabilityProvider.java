package melonslise.locks.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

public class SerializableCapabilityProvider<A> extends CapabilityProvider<A> implements INBTSerializable
{
	public SerializableCapabilityProvider(Capability cap, A inst)
	{
		super(cap, inst);
	}

	@Override
	public NBTBase serializeNBT()
	{
		return this.cap.getStorage().writeNBT(this.cap, this.inst, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt)
	{
		this.cap.getStorage().readNBT(this.cap, this.inst, null, nbt);
	}
}