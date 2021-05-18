package melonslise.locks.mixin;

import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.init.LocksCapabilities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
	@Inject(at = @At("HEAD"), method = "sendBlockUpdated(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V")
	private void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flag, CallbackInfo ci)
	{
		if(oldState.is(newState.getBlock()))
			return;
		ServerWorld world = (ServerWorld) (Object) this;
		ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
		// create buffer list because otherwise we will be deleting elements while iterating (BAD!!)
		handler.getInChunk(pos).values().stream().filter(lkb -> lkb.bb.intersects(pos)).collect(Collectors.toList()).forEach(lkb ->
		{
			world.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundCategory.BLOCKS, 0.8f, 0.8f + world.random.nextFloat() * 0.4f);
			world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, lkb.stack));
			handler.remove(lkb.id);
		});
	}
}