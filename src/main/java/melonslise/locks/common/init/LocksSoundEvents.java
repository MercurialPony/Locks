package melonslise.locks.common.init;

import melonslise.locks.Locks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class LocksSoundEvents
{
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Locks.ID);

	public static final RegistryObject<SoundEvent>
		KEY_RING = add("key_ring"),
		LOCK_CLOSE = add("lock.close"),
		LOCK_OPEN = add("lock.open"),
		LOCK_RATTLE = add("lock.rattle"),
		PIN_FAIL = add("pin.fail"),
		PIN_MATCH = add("pin.match"),
		SHOCK = add("shock");

	private LocksSoundEvents() {}

	public static void register()
	{
		SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public static RegistryObject<SoundEvent> add(String name)
	{
		return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(Locks.ID, name)));
	}
}