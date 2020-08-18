package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.item.LockItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
	@Inject(at = @At("HEAD"), method = "notifyBlockUpdate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V")
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci)
	{
		ServerWorld world = (ServerWorld) (Object) this;
		if(oldState.getBlock() != newState.getBlock())
			world.getCapability(LocksCapabilities.LOCKABLES)
				.ifPresent(lockables ->
				{
					lockables.get().values().stream().filter(lockable1 -> lockable1.box.intersects(pos))
					.forEach(lockable ->
						{
							world.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 0.8f, 0.8f + world.rand.nextFloat() * 0.4f);
							world.addEntity(new ItemEntity(world, (double) pos.getX() + 0.5d, (double) pos.getY() + 0.5d, (double) pos.getZ() + 0.5d, LockItem.from(lockable.lock)));
							lockables.remove(lockable.networkID);
						});
				});
	}
}