package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksNetwork;
import melonslise.locks.common.network.toclient.AddLockableToChunkPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkManager;
import net.minecraftforge.fml.network.PacketDistributor;

@Mixin(ChunkManager.class)
public class ChunkManagerMixin
{
	@Inject(at = @At("TAIL"), method = "playerLoadedChunk(Lnet/minecraft/entity/player/ServerPlayerEntity;[Lnet/minecraft/network/IPacket;Lnet/minecraft/world/chunk/Chunk;)V")
	private void playerLoadedChunk(ServerPlayerEntity player, IPacket<?>[] pkts, Chunk ch, CallbackInfo ci)
	{
		ch.getCapability(LocksCapabilities.LOCKABLE_STORAGE).orElse(null).get().values()
			.forEach(lkb -> LocksNetwork.MAIN.send(PacketDistributor.TRACKING_CHUNK.with(() -> ch), new AddLockableToChunkPacket(lkb, ch)));
	}
}