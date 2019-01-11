package melonslise.locks.common.sound.api;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class SoundEventNamed extends SoundEvent
{
	public SoundEventNamed(ResourceLocation name)
	{
		super(name);
		this.setRegistryName(name);;
	}
}