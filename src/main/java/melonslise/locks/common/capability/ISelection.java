package melonslise.locks.common.capability;

import net.minecraft.util.math.BlockPos;

public interface ISelection
{
	BlockPos get();

	void set(BlockPos pos);
}