package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.worldgen.ChestLockerFeature;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class LocksFeatures
{
	public static final DeferredRegister<Feature<?>> FEATURES = new DeferredRegister(ForgeRegistries.FEATURES, Locks.ID);

	public static final RegistryObject<Feature<NoFeatureConfig>>
		CHEST_LOCKER = add("chest_locker", new ChestLockerFeature(NoFeatureConfig::deserialize));

	private LocksFeatures() {}

	public static void register()
	{
		FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public static void addFeatures()
	{
		for(Biome biome : ForgeRegistries.BIOMES.getValues())
			biome.addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, Biome.createDecoratedFeature(CHEST_LOCKER.get(), IFeatureConfig.NO_FEATURE_CONFIG, LocksPlacements.CHEST.get(), IPlacementConfig.NO_PLACEMENT_CONFIG));
	}

	public static <T extends IFeatureConfig> RegistryObject<Feature<T>> add(String name, Feature<T> feature)
	{
		return FEATURES.register(name, () -> feature);
	}
}