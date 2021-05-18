package melonslise.locks.common.config;

import java.util.List;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import com.google.common.collect.Lists;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

public final class LocksConfig
{
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.DoubleValue GENERATION_CHANCE;
	public static final ForgeConfigSpec.DoubleValue GENERATION_ENCHANT_CHANCE;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> GENERATED_LOCKS;
	public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> GENERATED_LOCK_WEIGHTS;
	public static final ForgeConfigSpec.BooleanValue RANDOMIZE_LOADED_LOCKS;

	public static NavigableMap<Integer, Item> weightedGeneratedLocks;
	public static int weightTotal;

	static
	{
		ForgeConfigSpec.Builder cfg = new ForgeConfigSpec.Builder();

		GENERATION_CHANCE = cfg
			.comment("Chance to generate a random lock on every new chest during world generation. Set to 0 to disable")
			.defineInRange("Generation Chance", 0.85d, 0d, 1d);
		GENERATION_ENCHANT_CHANCE = cfg
			.comment("Chance to randomly enchant a generated lock during world generation. Set to 0 to disable")
			.defineInRange("Generation Enchant Chance", 0.4d, 0d, 1d);
		GENERATED_LOCKS = cfg
			.comment("Items that can be generated as locks (must be instance of LockItem in code!)")
			.defineList("Generated Locks", Lists.newArrayList("locks:wood_lock", "locks:iron_lock", "locks:steel_lock", "locks:gold_lock", "locks:diamond_lock"), e -> e instanceof String);
		GENERATED_LOCK_WEIGHTS= cfg
			.comment("WARNING: THE AMOUNT OF NUMBERS SHOULD BE EQUAL TO THE AMOUNT OF GENERATED LOCK ITEMS!!!", "The relative probability that the corresponding lock item will be generated on a chest. Higher number = higher chance to generate")
			.defineList("Generated Lock Chances", Lists.newArrayList(3, 3, 2, 2, 1), e -> e instanceof Integer);
		RANDOMIZE_LOADED_LOCKS = cfg
			.comment("Randomize lock IDs and combinations when loading them from a structure file. Randomization works just like during world generation")
			.define("Randomize Loaded Locks", false);

		SPEC = cfg.build();
	}

	private LocksConfig() {}

	// https://gist.github.com/raws/1667807
	public static void init()
	{
		weightedGeneratedLocks = new TreeMap<>();
		weightTotal = 0;
		List<? extends String> locks = GENERATED_LOCKS.get();
		List<? extends Integer> weights = GENERATED_LOCK_WEIGHTS.get();
		for(int a = 0; a < locks.size(); ++a)
		{
			weightTotal += weights.get(a);
			weightedGeneratedLocks.put(weightTotal, ForgeRegistries.ITEMS.getValue(new ResourceLocation(locks.get(a))));
		}
	}

	public static boolean canGen(Random rng)
	{
		return LocksUtil.chance(rng, GENERATION_CHANCE.get());
	}

	public static boolean canEnchant(Random rng)
	{
		return LocksUtil.chance(rng, GENERATION_ENCHANT_CHANCE.get());
	}

	public static ItemStack getRandomLock(Random rng)
	{
		ItemStack stack = new ItemStack(weightedGeneratedLocks.ceilingEntry(rng.nextInt(weightTotal) + 1).getValue());
		return canEnchant(rng) ? EnchantmentHelper.enchantItem(rng, stack, 5 + rng.nextInt(30), false) : stack;
	}
}