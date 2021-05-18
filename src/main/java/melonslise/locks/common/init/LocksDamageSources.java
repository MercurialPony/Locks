package melonslise.locks.common.init;

import net.minecraft.util.DamageSource;

public final class LocksDamageSources
{
	public static final DamageSource SHOCK = new DamageSource("locks.shock").bypassArmor();

	private LocksDamageSources() {}
}