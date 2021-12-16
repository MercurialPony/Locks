package melonslise.locks.common.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import melonslise.locks.Locks;
import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

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
		public double generationChance = 0.85d;
		
		@Config.Name("Generation Enchant Chance")
		@Config.Comment("Chance to randomly enchant a generated lock during world generation. Set to 0 to disable")
		@Config.RangeDouble(min = 0d, max = 1d)
		public double generationEnchantChance = 0.4d;

		@Config.Name("Generated Locks")
		@Config.Comment("Items that can be generated as locks (must be instance of LockItem in code!)")
		public String[] generatedLocks = new String[]{"locks:wood_lock", "locks:iron_lock", "locks:steel_lock", "locks:gold_lock", "locks:diamond_lock"};

		@Config.Name("Generated Lock Chances")
		@Config.Comment("WARNING: THE AMOUNT OF NUMBERS SHOULD BE EQUAL TO THE AMOUNT OF GENERATED LOCK ITEMS!!! The relative probability that the corresponding lock item will be generated on a chest. Higher number = higher chance to generate")
		public int[] generatedLockWeights = new int[]{3, 3, 2, 2, 1};
		
		@Config.Name("Generated Lock Pins")
		@Config.Comment("WARNING: THE AMOUNT OF NUMBERS SHOULD BE EQUAL TO THE AMOUNT OF GENERATED LOCK ITEMS!!! The number of pins on the lock item. Overrides the defaults if not zero")
		public int[] generatedLockPins = new int[]{0, 0, 0, 0, 0};

		@Config.Name("Automatically Orient Placed Locks")
		@Config.Comment("Placed locks will try to orient themselves smartly to doors and chests regardless of how they were placed")
		public boolean automaticallyOrientPlacedLocks = false;
		
		@Config.Name("Skip Generation Empty Chests")
		@Config.Comment("Skip generating locks on empty chests")
		public boolean skipGenerationEmptyChests = true;
		
		@Config.Name("Chest Item List Always Generate")
		@Config.Comment("Always generate locks if the chests contain these items. Metadata can be specified (ex. minecraft:bed:0)")
		public String[] chestItemListAlwaysGenerate = new String[]{"minecraft:nether_star"};
		
		@Config.Name("Chest Item List Skip Generating")
		@Config.Comment("Worldgen assumes chests with only these items are empty. Metadata can be specified (ex. minecraft:bed:0)")
		public String[] chestItemListSkipGenerating = new String[]{
				"minecraft:beetroot_seeds",
				"minecraft:bone",
				"minecraft:book",
				"minecraft:bowl",
				"minecraft:bread",
				"minecraft:brown_mushroom",
				"minecraft:clay_ball",
				"minecraft:cobblestone",
				"minecraft:dirt",
				"minecraft:dye",
				"minecraft:egg",
				"minecraft:gravel",
				"minecraft:hay_block",
				"minecraft:melon_seeds",
				"minecraft:painting",
				"minecraft:paper",
				"minecraft:pumpkin_seeds",
				"minecraft:red_mushroom",
				"minecraft:rotten_flesh",
				"minecraft:sapling",
				"minecraft:sign",
				"minecraft:spider_eye",
				"minecraft:stick",
				"minecraft:stone",
				"minecraft:stone_button",
				"minecraft:string",
				"minecraft:vine",
				"minecraft:waterlily",
				"minecraft:web",
				"minecraft:wheat",
				"minecraft:wheat_seeds",
				"minecraft:wooden_button",
		};
		
		
		
		
		
		//@Config.Name("Randomize Loaded Locks")
		//@Config.Comment("Randomize lock IDs and combinations when loading them from a structure file. Randomization works just like during world generation")
		//public boolean randomizeLoadedLocks = false;
	}

	public static class Server
	{
		@Config.Name("Max Lockable Volume")
		@Config.Comment("Maximum amount of blocks that can be locked at once")
		public int maxLockableVolume = 6;

		@Config.Name("Lockable Blocks")
		@Config.Comment("Blocks that can be locked. Each entry is the mod domain followed by the block's registry name. Can include regular expressions")
		public String[] lockableBlocks = { "minecraft:.*chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:hopper",  "minecraft:.*door", "minecraft:.*trapdoor", "minecraft:.*fence_gate", "minecraft:.*shulker_box" };

		@Config.Name("Allow Removing Locks")
		@Config.Comment("Open locks can be removed with an empty hand while sneaking")
		public boolean allowRemovingLocks = true;

		@Config.Name("Protect Lockables")
		@Config.Comment("Locked blocks cannot be destroyed in survival mode")
		public boolean protectLockables = true;
		
		@Config.Name("Easy Lock")
		@Config.Comment("Lock blocks with just one click! It's magic! (Will probably fail spectacularly with custom doors, custom double chests, etc)")
		public boolean easyLock = true;
		
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
			for(Pattern pat : this.lockableBlocksRegex)
				if(pat.matcher(world.getBlockState(pos).getBlock().getRegistryName().toString()).matches())
					return true;
			return false;
		}
	}

	public static class Client
	{
		@Config.Name("Deaf Mode")
		@Config.Comment("Display visual feedback when trying to use a locked block for certain hearing impaired individuals")
		public boolean deafMode = true;
	}
	
	private LocksConfig() {}
	
	@Config.Ignore
	public static NavigableMap<Integer, Integer> weightedGeneratedPins;
	@Config.Ignore
	public static NavigableMap<Integer, Item> weightedGeneratedLocks;
	@Config.Ignore
	public static int weightTotal;
	@Config.Ignore
	public static Set<String> alwaysGenerateItems;
	@Config.Ignore
	public static Set<String> skipGeneratingItems;
	
	public static void init()
	{
		weightedGeneratedLocks = new TreeMap<>();
		weightedGeneratedPins = new TreeMap<>();
		alwaysGenerateItems = new HashSet<>();
		skipGeneratingItems = new HashSet<>();
		
		weightTotal = 0;
		String[] locks = COMMON.generatedLocks;
		int[] weights = COMMON.generatedLockWeights;
		int[] pins = COMMON.generatedLockPins;
		for(int a = 0; a < locks.length; ++a)
		{
			weightTotal += weights[a];
			weightedGeneratedLocks.put(weightTotal, ForgeRegistries.ITEMS.getValue(new ResourceLocation(locks[a])));
			weightedGeneratedPins.put(weightTotal, pins[a]);
		}
		
		for(int a = 0; a < COMMON.chestItemListAlwaysGenerate.length; a++)
		{
			alwaysGenerateItems.add(COMMON.chestItemListAlwaysGenerate[a].trim());
		}
		
		for(int a = 0; a < COMMON.chestItemListSkipGenerating.length; a++)
		{
			skipGeneratingItems.add(COMMON.chestItemListSkipGenerating[a].trim());
		}
	}
	
	public static boolean isItemAlwaysLocked(ItemStack stack)
	{
		String regName = stack.getItem().getRegistryName().toString();
		String regNameMeta = regName + ":" + stack.getMetadata();
		return alwaysGenerateItems.contains(regName) || alwaysGenerateItems.contains(regNameMeta);
	}
	
	public static boolean isItemSkipped(ItemStack stack)
	{
		String regName = stack.getItem().getRegistryName().toString();
		String regNameMeta = regName + ":" + stack.getMetadata();
		return skipGeneratingItems.contains(regName) || skipGeneratingItems.contains(regNameMeta);
	}
	
	public static boolean canGen(Random rng)
	{
		return LocksUtil.chance(rng, COMMON.generationChance);
	}

	public static boolean canEnchant(Random rng)
	{
		return LocksUtil.chance(rng, COMMON.generationEnchantChance);
	}

	public static ItemStack getRandomLock(Random rng)
	{
		int rngNumber = rng.nextInt(weightTotal) + 1;
		ItemStack stack = new ItemStack(weightedGeneratedLocks.ceilingEntry(rngNumber).getValue());
		int pinout = MathHelper.clamp(weightedGeneratedPins.ceilingEntry(rngNumber).getValue(), 0, 30);
		if(pinout != 0)
		{
			boolean replacePins = true;
			if(stack.getItem() instanceof LockItem)
			{
				if(((LockItem)stack.getItem()).length == pinout)
					replacePins = false;
			}
			
			if(replacePins)
			{
				NBTTagCompound nbt = LocksUtil.getTag(stack);
				nbt.setByte(LockItem.KEY_LENGTH, (byte)pinout);
			}
		}
		
		return canEnchant(rng) ? EnchantmentHelper.addRandomEnchantment(rng, stack, 5 + rng.nextInt(30), false) : stack;
	}
	
	

}