package melonslise.locks.common.capability.entity;

import melonslise.locks.utility.LocksUtilities;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class CapabilityLockBounds implements ICapabilityLockBounds
{
	public static final ResourceLocation ID = LocksUtilities.createLocksDomain("lock_bounds");
	private BlockPos position;

	@Override
	public BlockPos get()
	{
		return this.position;
	}

	@Override
	public void set(BlockPos position)
	{
		this.position = position;
	}
}