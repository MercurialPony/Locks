package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksNetwork;
import melonslise.locks.common.network.toclient.AddLockableToChunkPacket;
import melonslise.locks.common.util.ILockableProvider;
import melonslise.locks.common.util.Lockable;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.network.PacketDistributor;

@Mixin(Chunk.class)
public class ChunkMixin
{
	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/ChunkPrimer;)V")
	private void init(World world, ChunkPrimer pr, CallbackInfo ci)
	{
		Chunk ch = (Chunk) (Object) this;
		ILockableStorage st = ch.getCapability(LocksCapabilities.LOCKABLE_STORAGE).orElse(null);
		ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
		// We trust that all checks pass (such as volume and intersect checks) due to this happening only during world gen
		for(Lockable lkb : ((ILockableProvider) pr).getLockables())
		{
			st.add(lkb);
			handler.getLoaded().put(lkb.id, lkb);
			lkb.addObserver(handler);
			LocksNetwork.MAIN.send(PacketDistributor.TRACKING_CHUNK.with(() -> ch), new AddLockableToChunkPacket(lkb, ch));
		}
	}
}