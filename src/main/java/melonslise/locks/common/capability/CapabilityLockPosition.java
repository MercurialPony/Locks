package melonslise.locks.common.capability;

import melonslise.locks.Locks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class CapabilityLockPosition implements ICapabilityLockPosition
{
	public static final ResourceLocation ID = new ResourceLocation(Locks.ID, "lock_position");

	protected BlockPos position;

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