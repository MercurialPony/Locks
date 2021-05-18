package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(HopperTileEntity.class)
public class HopperTileEntityMixin
{
	@Inject(at = @At("HEAD"), method = "getContainerAt(Lnet/minecraft/world/World;DDD)Lnet/minecraft/inventory/IInventory;", cancellable = true)
	private static void getContainerAt(World world, double x, double y, double z, CallbackInfoReturnable<IInventory> cir)
	{
		if(LocksUtil.locked(world, new BlockPos(x, y, z)))
			cir.setReturnValue(null);
	}
}