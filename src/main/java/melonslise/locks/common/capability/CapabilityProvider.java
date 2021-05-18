package melonslise.locks.common.capability;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class CapabilityProvider<A> implements ICapabilityProvider
{
	public final Capability<A> cap;
	public final A inst;
	public final LazyOptional<A> opt;

	public CapabilityProvider(Capability<A> cap, A inst)
	{
		this.cap = cap;
		this.inst = inst;
		this.opt = LazyOptional.of(() -> inst);
	}

	@Override
	public <B> LazyOptional<B> getCapability(Capability<B> cap, Direction side)
	{
		return cap == this.cap ? this.opt.cast() : LazyOptional.empty();
	}
}