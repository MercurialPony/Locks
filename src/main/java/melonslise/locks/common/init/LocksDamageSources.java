package melonslise.locks.common.init;

import net.minecraft.util.DamageSource;

public class LocksDamageSources
{
	public static final DamageSource SHOCK = new DamageSource("locks.shock").setDamageBypassesArmor();

	private LocksDamageSources() {}
}
