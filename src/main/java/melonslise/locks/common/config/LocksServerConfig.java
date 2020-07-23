package melonslise.locks.common.config;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;

public final class LocksServerConfig
{
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.IntValue MAX_LOCKABLE_VOLUME;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOCKABLE_BLOCKS;
	public static final ForgeConfigSpec.BooleanValue ALLOW_REMOVING_LOCKS;
	public static final ForgeConfigSpec.BooleanValue PROTECT_LOCKABLES;
	public static final ForgeConfigSpec.IntValue DEFAULT_LOCK_LENGTH;
	public static final ForgeConfigSpec.DoubleValue DEFAULT_LOCK_PICK_STRENGTH;

	public static List<Pattern> lockableBlocksRegex;

	static
	{
		ForgeConfigSpec.Builder cfg = new ForgeConfigSpec.Builder();

		MAX_LOCKABLE_VOLUME = cfg.comment("Maximum amount of blocks that can be locked at once").defineInRange("Max Lockable Volume", 6, 0, Integer.MAX_VALUE);
		LOCKABLE_BLOCKS = cfg.comment("Blocks that can be locked. Syntax is the mod domain followed by the block's registry name. Can include regular expressions").defineList("Lockable Blocks", Lists.newArrayList("minecraft:.*chest", "minecraft:barrel", "minecraft:dispenser", "minecraft:dropper", "minecraft:hopper",  "minecraft:.*door", "minecraft:.*trapdoor", "minecraft:.*fence_gate", "minecraft:.*shulker_box"), element -> element instanceof String);
		ALLOW_REMOVING_LOCKS = cfg.comment("Open locks can be removed with an empty hand while sneaking").define("Allow Removing Locks", true);
		PROTECT_LOCKABLES = cfg.comment("Locked blocks cannot be destroyed in survival mode").define("Protect Lockables", true);
		DEFAULT_LOCK_LENGTH = cfg.comment("All lock items, which don't have a length nbt value will have this value set as their length. The length of a lock can still be changed by editing its nbt").defineInRange("Default Lock Length", 7, 1, 30);
		DEFAULT_LOCK_PICK_STRENGTH = cfg.comment("All lock pick items which don't have a strength nbt value will have this value set as their strength. The strength of a lock pick can still be changed by edition its nbt").defineInRange("Default Lock Pick Strength", 0.3d, 0d, 1d);

		SPEC = cfg.build();
	}

	private LocksServerConfig() {}

	public static void load()
	{
		lockableBlocksRegex = LOCKABLE_BLOCKS.get().stream().map(regex -> Pattern.compile(regex)).collect(Collectors.toList());
	}

	public static boolean canLock(World world, BlockPos pos)
	{
		for(Pattern pat : lockableBlocksRegex)
			if(pat.matcher(world.getBlockState(pos).getBlock().getRegistryName().toString()).matches())
				return true;
		return false;
	}
}