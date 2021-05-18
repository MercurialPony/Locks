package melonslise.locks.common.init;

import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.IPlacementConfig;

public class LocksConfiguredPlacements
{
	public static final ConfiguredPlacement<?> CONFIGURED_CHEST = LocksPlacements.CHEST.get().configured(IPlacementConfig.NONE);

	private LocksConfiguredPlacements() {}
}