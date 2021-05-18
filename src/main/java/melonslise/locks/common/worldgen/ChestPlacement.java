package melonslise.locks.common.worldgen;

import java.util.Random;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.properties.ChestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

public class ChestPlacement extends Placement<NoPlacementConfig>
{
	public ChestPlacement(Codec<NoPlacementConfig> codec)
	{
		super(codec);
	}

	@Override
	public Stream<BlockPos> getPositions(WorldDecoratingHelper helper, Random rng, NoPlacementConfig cfg, BlockPos pos)
	{
		return helper.level.getChunk(pos).getBlockEntitiesPos().stream()
			.filter(tePos ->
			{
				BlockState state = helper.level.getBlockState(tePos);
				// Prevent from adding double chests twice
				return state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.RIGHT;
			});
	}
}