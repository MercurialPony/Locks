package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PistonBlock.class)
public class PistonBlockMixin
{
	// Before getPistonPushReaction call
	@Inject(at = @At(value = "INVOKE", target = "net/minecraft/block/BlockState.getPistonPushReaction()Lnet/minecraft/block/material/PushReaction;", ordinal = 0), method = "isPushable(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;ZLnet/minecraft/util/Direction;)Z", cancellable = true)
	private static void isPushable(BlockState state, World world, BlockPos pos, Direction dir, boolean flag, Direction dir1, CallbackInfoReturnable<Boolean> cir)
	{
		if(LocksUtil.locked(world, pos))
			cir.setReturnValue(false);
	}
}