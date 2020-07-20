package melonslise.locks.common.capability;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapabilityProvider<A> implements ICapabilityProvider
{
	protected Capability<A> cap;
	protected A inst;

	public CapabilityProvider(Capability<A> cap, A inst)
	{
		this.cap = cap;
		this.inst = inst;
	}

	@Override
	public boolean hasCapability(Capability<?> cap, @Nullable EnumFacing side)
	{
		return this.cap == cap;
	}

	@Override
	public <B> B getCapability(Capability<B> cap, @Nullable EnumFacing side)
	{
		return this.hasCapability(cap, side) ? (B) this.inst : null;
	}
}