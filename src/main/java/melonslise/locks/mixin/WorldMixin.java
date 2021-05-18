package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(World.class)
public class WorldMixin
{
	@Inject(at = @At("HEAD"), method = "hasNeighborSignal(Lnet/minecraft/util/math/BlockPos;)Z", cancellable = true)
	private void hasNeighborSignal(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
	{
		if(LocksUtil.locked((World) (Object) this, pos))
			cir.setReturnValue(false);
	}
}