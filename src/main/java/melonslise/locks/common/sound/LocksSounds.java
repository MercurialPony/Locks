package melonslise.locks.common.sound;

import melonslise.locks.common.sound.api.SoundEventNamed;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;

public class LocksSounds
{
	public static final SoundEventNamed
	key_ring = new SoundEventNamed(LocksUtilities.createLocksDomain("key_ring")),
	lock_close = new SoundEventNamed(LocksUtilities.createLocksDomain("lock.close")),
	lock_open = new SoundEventNamed(LocksUtilities.createLocksDomain("lock.open")),
	lock_rattle = new SoundEventNamed(LocksUtilities.createLocksDomain("lock.rattle")),
	pin_fail = new SoundEventNamed(LocksUtilities.createLocksDomain("pin.fail")),
	pin_match = new SoundEventNamed(LocksUtilities.createLocksDomain("pin.match"));

	public static void register(RegistryEvent.Register<SoundEvent> event)
	{
		event.getRegistry().registerAll(key_ring, lock_close, lock_open, lock_rattle, pin_fail, pin_match);
	}
}