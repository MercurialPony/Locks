package melonslise.locks.mixin;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.VanillaInventoryCodeHooks;
import net.minecraftforge.items.wrapper.EmptyHandler;

@Mixin(VanillaInventoryCodeHooks.class)
public class VanillaInventoryCodeHooksMixin
{
	//Remap is false because the whole class is added by forge
	//World worldIn, double x, double y, double z, final EnumFacing side
	@Inject(at = @At("HEAD"), method = "getItemHandler(Lnet/minecraft/world/World;DDDLnet/minecraft/util/EnumFacing;)Lorg/apache/commons/lang3/tuple/Pair;", cancellable = true, remap = false)
	private static void getItemHandler(World world, double x, double y, double z, final EnumFacing side, CallbackInfoReturnable<Pair> cir)
	{
		//Rare situation where world is null
		if(world == null)
			return;
		
		BlockPos pos = new BlockPos(x,y,z);
		if(LocksUtil.locked(world, pos))
		{
			TileEntity te = world.getTileEntity(pos);
			
			if(te == null)
				cir.setReturnValue(null);
			else
				cir.setReturnValue(ImmutablePair.<IItemHandler, Object>of(EmptyHandler.INSTANCE, te));
			
			//A null here passes the handler down the line, past forge's VanillaInventoryCodeHooks
			//So if there is a tile entity, pretend like the inventory is totally empty with EmptyHandler.INSTANCE
		}
	}
}
