package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksContainerTypes;
import melonslise.locks.common.init.LocksFeatures;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.init.LocksPlacements;
import melonslise.locks.common.init.LocksRecipeSerializers;
import melonslise.locks.common.init.LocksSoundEvents;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Locks.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LocksModEvents
{
	private LocksModEvents() {}

	@SubscribeEvent
	public static void onSetup(FMLCommonSetupEvent event)
	{
		LocksCapabilities.register();
		LocksNetworks.register();
		LocksFeatures.addFeatures();
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		LocksItems.register(event);
	}

	@SubscribeEvent
	public static void registerSoundEvents(RegistryEvent.Register<SoundEvent> event)
	{
		LocksSoundEvents.register(event);
	}

	@SubscribeEvent
	public static void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> event)
	{
		LocksContainerTypes.register(event);
	}

	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event)
	{
		LocksFeatures.register(event);
	}

	@SubscribeEvent
	public static void registerPlacements(RegistryEvent.Register<Placement<?>> event)
	{
		LocksPlacements.register(event);
	}

	@SubscribeEvent
	public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
	{
		LocksRecipeSerializers.register(event);
	}

	@SubscribeEvent
	public static void onConfigLoad(ModConfig.ModConfigEvent event)
	{
		if(event.getConfig().getSpec() == LocksServerConfig.SPEC)
			LocksServerConfig.load();
	}
}