package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.properties.ChestType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ChestBlock.class)
public class ChestBlockMixin
{
	@Inject(at = @At("HEAD"), method = "candidatePartnerFacing(Lnet/minecraft/item/BlockItemUseContext;Lnet/minecraft/util/Direction;)Lnet/minecraft/util/Direction;", cancellable = true)
	private void candidatePartnerFacing(BlockItemUseContext ctx, Direction dir, CallbackInfoReturnable<Direction> cir)
	{
		World world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos().relative(dir);
		BlockState state = world.getBlockState(pos);
		cir.setReturnValue(state.is((ChestBlock) (Object) this) && state.getValue(ChestBlock.TYPE) == ChestType.SINGLE && !LocksUtil.locked(world, pos)  ? state.getValue(ChestBlock.FACING) : null);
	}
}