package melonslise.locks.common.init;

import java.util.Arrays;
import java.util.stream.Stream;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;

public final class LocksPacketDistributors
{
	public static final PacketDistributor<Stream<Chunk>> TRACKING_AREA = new PacketDistributor<>((pd, s) ->
		pkt ->
		{
			// Convert each chunk to a stream of tracking players
			// Merge all streams into one
			// Remove duplicate players
			// Send packet
			s.get()
				.flatMap(chunk -> ((ServerChunkProvider) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false))
				.distinct()
				.forEach(p -> p.connection.send(pkt));
		}, NetworkDirection.PLAY_TO_CLIENT);

	private LocksPacketDistributors() {}
}