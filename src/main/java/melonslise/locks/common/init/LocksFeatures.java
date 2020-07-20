package melonslise.locks.common.init;

import java.util.ArrayList;
import java.util.List;

import melonslise.locks.Locks;
import melonslise.locks.common.worldgen.FeatureLockChest;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class LocksFeatures
{
	public static final List<Feature> FEATURES = new ArrayList<>(1);

	public static final Feature
		LOCK_CHEST = add("lock_chest", new FeatureLockChest(NoFeatureConfig::deserialize));

	private LocksFeatures() {}

	public static void register(RegistryEvent.Register<Feature<?>> event)
	{
		for(Feature feature : FEATURES)
			event.getRegistry().register(feature);
	}

	public static void addFeatures()
	{
		for(Biome biome : ForgeRegistries.BIOMES.getValues())
			biome.addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, Biome.createDecoratedFeature(LOCK_CHEST, IFeatureConfig.NO_FEATURE_CONFIG, LocksPlacements.CHEST, IPlacementConfig.NO_PLACEMENT_CONFIG));
	}

	public static Feature add(String name, Feature feature)
	{
		FEATURES.add((Feature) feature.setRegistryName(Locks.ID, name));
		return feature;
	}
}