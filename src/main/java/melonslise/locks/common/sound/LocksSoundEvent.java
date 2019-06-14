package melonslise.locks.common.sound;

import melonslise.locks.Locks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class LocksSoundEvent extends SoundEvent
{
	public LocksSoundEvent(String name)
	{
		super(new ResourceLocation(Locks.ID, name));
		this.setRegistryName(Locks.ID, name);
	}
}