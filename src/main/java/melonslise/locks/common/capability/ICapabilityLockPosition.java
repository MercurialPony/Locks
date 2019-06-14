package melonslise.locks.common.capability;

import net.minecraft.util.math.BlockPos;

public interface ICapabilityLockPosition
{
	BlockPos get();

	void set(BlockPos position);
}