package melonslise.locks.common.init;

import melonslise.locks.common.worldgen.FeatureLockChest;
import melonslise.locks.common.worldgen.PlacementAtChest;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraftforge.registries.ForgeRegistries;

public final class LocksFeatures
{
	public static final ConfiguredFeature<?> LOCKABLES = Biome.func_222280_a(new FeatureLockChest(), new NoFeatureConfig(), new PlacementAtChest(), new NoPlacementConfig());

	private LocksFeatures() {}

	public static void register()
	{
		for(Biome biome : ForgeRegistries.BIOMES.getValues()) biome.addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, LOCKABLES);
	}
}