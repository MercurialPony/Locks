package melonslise.locks.common.init;

import java.util.ArrayList;
import java.util.List;

import melonslise.locks.Locks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;

public final class LocksSoundEvents
{
	private static final List<SoundEvent> SOUNDS = new ArrayList<SoundEvent>(6);

	public static final SoundEvent
		KEY_RING = add("key_ring"),
		LOCK_CLOSE = add("lock.close"),
		LOCK_OPEN = add("lock.open"),
		LOCK_RATTLE = add("lock.rattle"),
		PIN_FAIL = add("pin.fail"),
		PIN_MATCH = add("pin.match"),
		SHOCK = add("shock");

	private LocksSoundEvents() {}

	public static void register(RegistryEvent.Register<SoundEvent> event)
	{
		for(SoundEvent sound : SOUNDS)
			event.getRegistry().register(sound);
	}

	public static SoundEvent add(String name)
	{
		ResourceLocation rl = new ResourceLocation(Locks.ID, name);
		SoundEvent sound = new SoundEvent(rl).setRegistryName(rl);
		SOUNDS.add(sound);
		return sound;
	}
}