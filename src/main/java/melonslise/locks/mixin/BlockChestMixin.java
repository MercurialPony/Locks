package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.block.BlockChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BlockChest.class)
public abstract class BlockChestMixin
{
	@Shadow
	public abstract boolean isDoubleChest(World world, BlockPos pos);
	
	@Inject(at = @At("RETURN"), method = "canPlaceBlockAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", cancellable = true)
	private void canPlaceBlockAt(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir)
	{
		BlockChest _this = (BlockChest) (Object) this;
		
		//Check for a chest to connect to, and if the chest is not a double chest and locked
		BlockPos posWest = pos.west();
		BlockPos posEast = pos.east();
		BlockPos posNorth = pos.north();
		BlockPos posSouth = pos.south();
		
		if (world.getBlockState(posWest).getBlock() == _this)
		{
			if (!this.isDoubleChest(world, posWest) && LocksUtil.locked(world, posWest))
			{
				cir.setReturnValue(false);
				return;
			}
		}
		else if (world.getBlockState(posEast).getBlock() == _this)
		{
			if (!this.isDoubleChest(world, posEast) && LocksUtil.locked(world, posEast))
			{
				cir.setReturnValue(false);
				return;
			}
		}
		else if (world.getBlockState(posNorth).getBlock() == _this)
		{
			if (!this.isDoubleChest(world, posNorth) && LocksUtil.locked(world, posNorth))
			{
				cir.setReturnValue(false);
				return;
			}
		}
		else if (world.getBlockState(posSouth).getBlock() == _this)
		{
			if (!this.isDoubleChest(world, posSouth) && LocksUtil.locked(world, posSouth))
			{
				cir.setReturnValue(false);
				return;
			}
		}
	}
}
