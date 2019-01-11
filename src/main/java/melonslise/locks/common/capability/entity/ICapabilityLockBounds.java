package melonslise.locks.common.capability.entity;

import net.minecraft.util.math.BlockPos;

// TODO
public interface ICapabilityLockBounds
{
	BlockPos get();

	void set(BlockPos position);
}