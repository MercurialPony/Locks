package melonslise.locks.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

@Mixin(TileEntityLockable.class)
public class TileEntityLockableMixin
{
	//Remap is false because the function is added by forge
	@Inject(at = @At("HEAD"), method = "getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", cancellable = true, remap = false)
	private void getCapability(Capability cap, @Nullable EnumFacing facing, CallbackInfoReturnable<Object> cir)
	{
		TileEntity te = (TileEntity) (Object) this;
		if(!te.isInvalid() && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && te.hasWorld() && LocksUtil.locked(te.getWorld(), te.getPos()))
			cir.setReturnValue(EmptyHandler.INSTANCE);
		//Pretend to be empty
	}
}
