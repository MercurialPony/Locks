package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.block.PistonBlockStructureHelper;

@Mixin(PistonBlockStructureHelper.class)
public class PistonBlockStructureHelperMixin
{
	@Inject(at = @At("HEAD"), method = "resolve()Z", cancellable = true)
	private void resolve(CallbackInfoReturnable<Boolean> cir)
	{
		PistonBlockStructureHelper h = (PistonBlockStructureHelper) (Object) this;
		if(LocksUtil.locked(h.level, h.startPos))
			cir.setReturnValue(false);
	}
}