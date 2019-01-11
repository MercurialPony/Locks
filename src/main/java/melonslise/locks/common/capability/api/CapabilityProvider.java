package melonslise.locks.common.capability.api;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapabilityProvider implements ICapabilityProvider
{
	protected Capability capability;
	protected Object instance;
	protected EnumFacing side;

	public CapabilityProvider(Capability capability, Object instance, EnumFacing side)
	{
		this.capability = capability;
		this.instance = instance;
		this.side = side;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return this.capability == capability;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		return this.hasCapability(capability, side) ? (T) this.instance : null;
	}
}