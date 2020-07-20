package melonslise.locks.common.init;

import java.util.ArrayList;
import java.util.List;

import melonslise.locks.Locks;
import melonslise.locks.common.worldgen.PlacementAtChest;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent;

public final class LocksPlacements
{
	public static final List<Placement> PLACEMENTS = new ArrayList<>(1);

	public static final Placement
		CHEST = add("chest", new PlacementAtChest(NoPlacementConfig.field_236555_a_));

	private LocksPlacements() {}

	public static void register(RegistryEvent.Register<Placement<?>> event)
	{
		for(Placement pl : PLACEMENTS)
			event.getRegistry().register(pl);
	}

	public static Placement add(String name, Placement pl)
	{
		PLACEMENTS.add((Placement) pl.setRegistryName(Locks.ID, name));
		return pl;
	}
}