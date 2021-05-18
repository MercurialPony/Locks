package melonslise.locks.common.config;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;

public class LocksServerConfig
{
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.IntValue MAX_LOCKABLE_VOLUME;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOCKABLE_BLOCKS;
	public static final ForgeConfigSpec.BooleanValue ALLOW_REMOVING_LOCKS;
	public static final ForgeConfigSpec.BooleanValue PROTECT_LOCKABLES;
	public static final ForgeConfigSpec.BooleanValue EASY_LOCK;

	public static Pattern[] lockableBlocks;

	static
	{
		ForgeConfigSpec.Builder cfg = new ForgeConfigSpec.Builder();

		MAX_LOCKABLE_VOLUME = cfg
			.comment("Maximum amount of blocks that can be locked at once")
			.defineInRange("Max Lockable Volume", 6, 1, Integer.MAX_VALUE);
		LOCKABLE_BLOCKS = cfg
			.comment("Blocks that can be locked. Each entry is the mod domain followed by the block's registry name. Can include regular expressions")
			.defineList("Lockable Blocks", Lists.newArrayList(".*chest", ".*barrel", ".*hopper", ".*door", ".*trapdoor", ".*fence_gate", ".*shulker_box"), e -> e instanceof String);
		ALLOW_REMOVING_LOCKS = cfg
			.comment("Open locks can be removed with an empty hand while sneaking")
			.define("Allow Removing Locks", true);
		PROTECT_LOCKABLES = cfg
			.comment("Locked blocks cannot be destroyed in survival mode")
			.define("Protect Lockables", true);
		EASY_LOCK = cfg
			.comment("Lock blocks with just one click! It's magic! (Will probably fail spectacularly with custom doors, custom double chests, etc)")
			.define("Easy Lock", true);

		SPEC = cfg.build();
	}

	private LocksServerConfig() {}

	public static void init()
	{
		lockableBlocks = LOCKABLE_BLOCKS.get().stream().map(s -> Pattern.compile(s)).toArray(Pattern[]::new);
	}

	public static boolean canLock(World world, BlockPos pos)
	{
		String name = world.getBlockState(pos).getBlock().getRegistryName().toString();
		for(Pattern p : lockableBlocks)
			if(p.matcher(name).matches())
				return true;
		return false;
	}
}