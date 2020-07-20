package melonslise.locks.common.worldgen;

import java.util.Random;

import com.mojang.serialization.Codec;

import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.Orientation;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.properties.ChestType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.StructureManager;

public class FeatureLockChest extends Feature<NoFeatureConfig>
{
	public FeatureLockChest(Codec<NoFeatureConfig> codec)
	{
		super(codec);
	}

	@Override
	public boolean func_230362_a_(ISeedReader reader, StructureManager structureManager, ChunkGenerator gen, Random rand, BlockPos pos, NoFeatureConfig cfg)
	{
		return Locks.PROXY.getLockables(reader.getWorld())
			.map(lockables ->
			{
				if(lockables.get().values().stream().anyMatch(lockable1 -> lockable1.box.intersects(pos)))
					return false;
				BlockState state = reader.getBlockState(pos);
				BlockPos adjPos = state.get(ChestBlock.TYPE) == ChestType.SINGLE ? pos : pos.offset(ChestBlock.getDirectionToAttached(state));
				lockables.add(new Lockable(new Cuboid6i(pos, adjPos), new Lock(rand.nextInt(), LocksConfig.randLockLen(rand), true), Orientation.fromDirection(state.get(ChestBlock.FACING), Direction.NORTH)));
				return true;
			})
			.orElse(false);
	}
}