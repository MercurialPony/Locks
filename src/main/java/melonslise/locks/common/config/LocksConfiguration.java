package melonslise.locks.common.config;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;

// TODO Translation
public class LocksConfiguration
{
	public static final LocksConfiguration MAIN;
	public static final ForgeConfigSpec SPEC;

	static
	{
		final Pair<LocksConfiguration, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(LocksConfiguration::new);
		MAIN = pair.getLeft();
		SPEC = pair.getRight();
	}

	public final ForgeConfigSpec.IntValue lockableVolume;
	public final ForgeConfigSpec.ConfigValue<List<? extends String>> lockableBlocks;
	public final ForgeConfigSpec.BooleanValue generateLocks;
	public final ForgeConfigSpec.BooleanValue removableLocks;
	public final ForgeConfigSpec.BooleanValue unbreakableLocks;
	public final ForgeConfigSpec.IntValue generationLowerBound;
	public final ForgeConfigSpec.IntValue generationUpperBound;

	public LocksConfiguration(ForgeConfigSpec.Builder builder)
	{
		builder.push("Configuration");
		this.lockableVolume = builder.comment("Maximum allowed volume of a single new lock").defineInRange("Lockable volume", 6, 0, Integer.MAX_VALUE);
		this.lockableBlocks = builder.comment("Blocks that can be locked. Syntax is the mod domain followed by the block's registry name").defineList("Lockable blocks", Lists.newArrayList("minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest", "minecraft:barrel", "minecraft:dispenser", "minecraft:dropper", "minecraft:hopper",  "minecraft:wooden_door", "minecraft:iron_door", "minecraft:spruce_door", "minecraft:birch_door", "minecraft:jungle_door", "minecraft:acacia_door", "minecraft:dark_oak_door", "minecraft:oak_trapdoor", "minecraft:spruce_trapdoor", "minecraft:birch_trapdoor", "minecraft:jungle_trapdoor", "minecraft:acacia_trapdoor", "minecraft:dark_oak_trapdoor", "minecraft:iron_trapdoor", "minecraft:fence_gate", "minecraft:spruce_fence_gate", "minecraft:birch_fence_gate", "minecraft:jungle_fence_gate", "minecraft:acacia_fence_gate", "minecraft:dark_oak_fence_gate"), element -> element instanceof String);
		this.generateLocks = builder.comment("Generate every new chest with a random lock").define("Generate locks", true);
		this.generationLowerBound = builder.comment("The minimum amount of pins on every generated lock (inclusive)").defineInRange("Generation lower bound", 5, 1, 30);
		this.generationUpperBound = builder.comment("The maximum amount of pins on every generated lock (exclusive)").defineInRange("Generation upper bound", 9, 1, 30);
		this.removableLocks = builder.comment("Open locks can be removed with an empty hand while sneaking").define("Removable locks", true);
		this.unbreakableLocks = builder.comment("Locked blocks cannot be destroyed in survival mode").define("Unbreakable locks", true);
		builder.pop();
	}

	public boolean canLock(World world, BlockPos position)
	{
		return this.lockableBlocks.get().contains(world.getBlockState(position).getBlock().getRegistryName().toString());
	}

	public int generateLockLength(Random random)
	{
		return random.nextInt(this.generationUpperBound.get() - this.generationLowerBound.get()) + this.generationLowerBound.get();
	}
}