package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.worldgen.LockChestsFeature;
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
		LOCK_CHESTS = add("lock_chests", new LockChestsFeature(NoFeatureConfig.CODEC));

	private LocksFeatures() {}

	public static void register()
	{
		FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public static <T extends IFeatureConfig> RegistryObject<Feature<T>> add(String name, Feature<T> feature)
	{
		return FEATURES.register(name, () -> feature);
	}
}