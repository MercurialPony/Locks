package melonslise.locks.common.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class CapabilityProvider<A> implements ICapabilityProvider
{
	protected Capability capability;
	protected A instance;
	protected LazyOptional<A> optional;
	protected Direction side;

	public CapabilityProvider(Capability capability, A instance, Direction side)
	{
		this.capability = capability;
		this.instance = instance;
		this.optional = instance == null ? LazyOptional.empty() : LazyOptional.of(() -> instance);
		this.side = side;
	}

	@Override
	public <B> LazyOptional<B> getCapability(@Nonnull final Capability<B> capability, final @Nullable Direction side)
	{
		return this.capability.orEmpty(capability, this.optional);
	}
}