package melonslise.locks.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.IBlockReader;

@Mixin(ExplosionContext.class)
public class ExplosionContextMixin
{
	@Inject(at = @At("RETURN"), method = "getBlockExplosionResistance(Lnet/minecraft/world/Explosion;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Ljava/util/Optional;", cancellable = true)
	private void getBlockExplosionResistance(Explosion ex, IBlockReader world, BlockPos pos, BlockState state, FluidState fluid, CallbackInfoReturnable<Optional<Float>> cir)
	{
		cir.setReturnValue(cir.getReturnValue().map(r -> Math.max(r, LocksUtil.intersecting(ex.level, pos).filter(LocksPredicates.LOCKED).findFirst().map(lkb -> LockItem.getResistance(lkb.stack)).orElse(0))));
	}
}