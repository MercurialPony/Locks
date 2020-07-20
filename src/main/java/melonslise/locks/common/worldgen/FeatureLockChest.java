package melonslise.locks.common.worldgen;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Orientation;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.properties.ChestType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

public class FeatureLockChest extends Feature<NoFeatureConfig>
{
	public FeatureLockChest(Function<Dynamic<?>, ? extends NoFeatureConfig> factory)
	{
		super(factory);
	}

	@Override
	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> gen, Random rand, BlockPos pos, NoFeatureConfig cfg)
	{
		//World world = ServerLifecycleHooks.getCurrentServer().getWorld(region.getDimension().getType());
		return world.getWorld().getCapability(LocksCapabilities.LOCKABLES)
			.map(lockables ->
			{
				if(lockables.get().values().stream().anyMatch(lockable1 -> lockable1.box.intersects(pos)))
					return false;
				BlockState state = world.getBlockState(pos);
				BlockPos adjPos = state.get(ChestBlock.TYPE) == ChestType.SINGLE ? pos : pos.offset(ChestBlock.getDirectionToAttached(state));
				lockables.add(new Lockable(new Cuboid6i(pos, adjPos), new Lock(rand.nextInt(), LocksConfig.randLockLen(rand), true), Orientation.fromDirection(state.get(ChestBlock.FACING), Direction.NORTH)));
				return true;
			})
			.orElse(false);
	}
}