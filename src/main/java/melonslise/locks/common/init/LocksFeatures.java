package melonslise.locks.common.init;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import melonslise.locks.Locks;
import melonslise.locks.common.worldgen.ChestLockerFeature;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class LocksFeatures
{
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Locks.ID);

	public static final RegistryObject<Feature<NoFeatureConfig>>
		CHEST_LOCKER = add("chest_locker", new ChestLockerFeature(NoFeatureConfig.field_236558_a_));

	public static ConfiguredFeature<?, ?>
		CONFIGURED_CHEST_LOCKER = null;

	private LocksFeatures() {}

	public static void register()
	{
		FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public static void configure()
	{
		CONFIGURED_CHEST_LOCKER = addConfiguredFeature("chest_locker", CHEST_LOCKER.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(LocksPlacements.CONFIGURED_CHEST));
	}

	public static void addFeatures()
	{
		LocksPlacements.configure();
		LocksFeatures.configure();
		for(Biome biome : WorldGenRegistries.field_243657_i)
			addFeatureTo(biome, GenerationStage.Decoration.TOP_LAYER_MODIFICATION, CONFIGURED_CHEST_LOCKER);
	}

	public static void addFeatureTo(Biome biome, GenerationStage.Decoration stage, ConfiguredFeature cf)
	{
		BiomeGenerationSettings settings = biome.func_242440_e();
		if(settings.field_242484_f instanceof ImmutableList)
			settings.field_242484_f = settings.field_242484_f.stream().map(ArrayList::new).collect(Collectors.toList());
		while (settings.field_242484_f.size() <= stage.ordinal())
			settings.field_242484_f.add(new ArrayList());
		settings.field_242484_f.get(stage.ordinal()).add(() -> cf);
	}

	public static <T extends IFeatureConfig> RegistryObject<Feature<T>> add(String name, Feature<T> feature)
	{
		return FEATURES.register(name, () -> feature);
	}

	public static ConfiguredFeature<?, ?> addConfiguredFeature(String name, ConfiguredFeature<?, ?> cf)
	{
		Registry.register(WorldGenRegistries.field_243653_e, new ResourceLocation(Locks.ID, name), cf);
		return cf;
	}
}