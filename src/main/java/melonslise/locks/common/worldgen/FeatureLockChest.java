package melonslise.locks.common.worldgen;

import java.util.Random;

import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.utility.Box;
import melonslise.locks.utility.Lock;
import melonslise.locks.utility.Lockable;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.properties.ChestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class FeatureLockChest extends Feature<NoFeatureConfig>
{
	public FeatureLockChest()
	{
		super(NoFeatureConfig::func_214639_a);
	}

	@Override
	public boolean place(IWorld region, ChunkGenerator generator, Random random, BlockPos position, NoFeatureConfig config)
	{
		World world = ServerLifecycleHooks.getCurrentServer().getWorld(region.getDimension().getType());
		return world.getCapability(LocksCapabilities.LOCKABLES).map(lockables ->
		{
			if(lockables.getLockables().values().stream().anyMatch(new PredicateIntersecting(position))) return false;
			BlockState state = region.getBlockState(position);
			BlockPos adjacentPosition = state.get(ChestBlock.TYPE) == ChestType.SINGLE ? position : position.offset(ChestBlock.getDirectionToAttached(state));
			lockables.add(new Lockable(new Box(position, adjacentPosition), new Lock(random.nextInt(), LocksConfiguration.MAIN.generateLockLength(random), true), state.get(ChestBlock.FACING)));
			return true;
		}).orElse(false);
	}
}