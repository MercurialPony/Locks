package melonslise.locks.common.config;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

import melonslise.locks.Locks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;

@Config(modid = Locks.ID)
public final class LocksConfig
{
	public static final Common COMMON = new Common();

	public static final Server SERVER = new Server();
	protected static final Server SERVER_CLIENT = new Server();

	public static final Client CLIENT = new Client();

	public static Server getServer(World world)
	{
		return world.isRemote ? SERVER_CLIENT : SERVER;
	}

	public static Server getServerClient()
	{
		return SERVER_CLIENT;
	}

	public static class Common
	{
		@Config.Name("Generation Chance")
		@Config.Comment("Chance to generate a random lock on every new chest during world generation. Set to 0 to disable")
		@Config.RangeDouble(min = 0d, max = 1d)
		@Config.SlidingOption
		public double generationChance = 0.75d;

		@Config.Name("Min Generated Lock Length")
		@Config.Comment("The minimum amount of pins on every generated lock (inclusive)")
		@Config.RangeInt(min = 1, max = 30)
		@Config.SlidingOption
		public int minGeneratedLockLength = 5;

		@Config.Name("Max Generated Lock Length")
		@Config.Comment("The maximum amount of pins on every generated lock (exclusive)")
		@Config.RangeInt(min = 1, max = 30)
		@Config.SlidingOption
		public int maxGeneratedLockLength = 9;

		public int randLockLen(Random random)
		{
			return random.nextInt(maxGeneratedLockLength - minGeneratedLockLength) + minGeneratedLockLength;
		}
	}

	public static class Server
	{
		@Config.Name("Max Lockable Volume")
		@Config.Comment("Maximum amount of blocks that can be locked at once")
		public int maxLockableVolume = 6;

		@Config.Name("Lockable Blocks")
		@Config.Comment("Blocks that can be locked. Syntax is the mod domain followed by the block's registry name. Can include regular expressions")
		public String[] lockableBlocks = { "minecraft:.*chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:hopper",  "minecraft:.*door", "minecraft:.*trapdoor", "minecraft:.*fence_gate", "minecraft:.*shulker_box" };

		@Config.Name("Allow Removing Locks")
		@Config.Comment("Open locks can be removed with an empty hand while sneaking")
		public boolean allowRemovingLocks = true;

		@Config.Name("Protect Lockables")
		@Config.Comment("Locked blocks cannot be destroyed in survival mode")
		public boolean protectLockables = true;

		@Config.Name("Default Lock Length")
		@Config.Comment("All lock items, which don't have a length nbt value will have this value set as their length. The length of a lock can still be changed by editing its nbt")
		@Config.RangeInt(min = 1, max = 30)
		@Config.SlidingOption
		public int defaultLockLength = 7;

		@Config.Name("Default Lock Pick Strength")
		@Config.Comment("All lock pick items which don't have a strength nbt value will have this value set as their strength. The strength of a lock pick can still be changed by edition its nbt")
		@Config.RangeDouble(min = 0d, max = 1d)
		@Config.SlidingOption
		public double defaultLockPickStrength = 0.35d;

		@Config.Ignore
		public Pattern[] lockableBlocksRegex;

		public void init()
		{
			this.lockableBlocksRegex = Arrays.stream(this.lockableBlocks).map(regex -> Pattern.compile(regex)).toArray(Pattern[]::new);
		}

		public boolean canLock(World world, BlockPos pos)
		{
			if(this.lockableBlocksRegex == null)
				this.init();
			for(Pattern pat : lockableBlocksRegex)
				if(pat.matcher(world.getBlockState(pos).getBlock().getRegistryName().toString()).matches())
					return true;
			return false;
		}
	}

	public static class Client
	{
		@Config.Name("Deaf Mode")
		@Config.Comment("Display visual feedback when trying to use a locked block for certain hearing impaired individuals")
		public boolean deafMode = false;
	}

	private LocksConfig() {}
}