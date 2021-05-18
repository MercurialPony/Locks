package melonslise.locks.common.init;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public final class LocksConfiguredFeatures
{
	public static final ConfiguredFeature<?, ?> CONFIGURED_LOCK_CHESTS = add("lock_chests", LocksFeatures.LOCK_CHESTS.get().configured(IFeatureConfig.NONE).decorated(LocksConfiguredPlacements.CONFIGURED_CHEST));

	private LocksConfiguredFeatures() {}

	public static void addTo(BiomeLoadingEvent e)
	{
		e.getGeneration().addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, LocksConfiguredFeatures.CONFIGURED_LOCK_CHESTS);
	}

	public static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> add(String name, ConfiguredFeature<FC, ?> cf)
	{
		return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, name, cf);
	}
}