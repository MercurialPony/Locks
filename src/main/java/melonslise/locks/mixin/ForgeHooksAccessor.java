package melonslise.locks.mixin;

import java.util.Deque;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraftforge.common.ForgeHooks;

@Mixin(ForgeHooks.class)
public interface ForgeHooksAccessor
{
	@Accessor(remap = false)
	public static ThreadLocal<Deque> getLootContext()
	{
		return null;
	}
}