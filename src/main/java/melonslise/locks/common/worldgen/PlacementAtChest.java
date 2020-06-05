package melonslise.locks.common.worldgen;

import java.util.Random;
import java.util.stream.Stream;

import melonslise.locks.common.config.LocksConfiguration;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

public class PlacementAtChest extends Placement<NoPlacementConfig>
{
	public PlacementAtChest()
	{
		super(NoPlacementConfig::deserialize);
	}

	@Override
	public Stream<BlockPos> getPositions(IWorld region, ChunkGenerator<? extends GenerationSettings> generator, Random random, NoPlacementConfig config, BlockPos regionPosition)
	{
		if(!LocksConfiguration.MAIN.generateLocks.get()) return Stream.empty();
		IChunk chunk = region.getChunk(regionPosition);
		return chunk.getTileEntitiesPos().stream().map(tileEntityPosition -> region.getTileEntity(tileEntityPosition)).filter(tileEntity -> tileEntity.getType() == TileEntityType.CHEST).map(tileEntity -> tileEntity.getPos());
	}
}