package melonslise.locks.common.init;

import java.util.ArrayList;
import java.util.List;

import melonslise.locks.common.sound.LocksSoundEvent;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;

public final class LocksSounds
{
	private static final List<SoundEvent> SOUNDS = new ArrayList<SoundEvent>();

	public static final SoundEvent
	KEY_RING = add(new LocksSoundEvent("key_ring")),
	LOCK_CLOSE = add(new LocksSoundEvent("lock.close")),
	LOCK_OPEN = add(new LocksSoundEvent("lock.open")),
	LOCK_RATTLE = add(new LocksSoundEvent("lock.rattle")),
	PIN_FAIL = add(new LocksSoundEvent("pin.fail")),
	PIN_MATCH = add(new LocksSoundEvent("pin.match"));

	private LocksSounds() {}

	public static void register(RegistryEvent.Register<SoundEvent> event)
	{
		for(SoundEvent sound : SOUNDS) event.getRegistry().register(sound);
	}

	public static LocksSoundEvent add(LocksSoundEvent sound)
	{
		SOUNDS.add(sound);
		return sound;
	}
}