package melonslise.locks.common.config;

import java.util.Random;

import net.minecraftforge.common.ForgeConfigSpec;

public final class LocksConfig
{
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.DoubleValue GENERATION_CHANCE;
	public static final ForgeConfigSpec.IntValue MIN_GENERATED_LOCK_LENGTH;
	public static final ForgeConfigSpec.IntValue MAX_GENERATED_LOCK_LENGTH;
	public static final ForgeConfigSpec.BooleanValue RANDOMIZE_LOADED_LOCKS;

	static
	{
		ForgeConfigSpec.Builder cfg = new ForgeConfigSpec.Builder();

		GENERATION_CHANCE = cfg.comment("Chance to generate a random lock on every new chest during world generation. Set to 0 to disable").defineInRange("Generation Chance", 0.75d, 0d, 1d);
		MIN_GENERATED_LOCK_LENGTH = cfg.comment("The minimum amount of pins on every generated lock (inclusive)").defineInRange("Min Generated Lock Length", 5, 1, 30);
		MAX_GENERATED_LOCK_LENGTH = cfg.comment("The maximum amount of pins on every generated lock (exclusive)").defineInRange("Max Generated Lock Length", 9, 1, 30);
		RANDOMIZE_LOADED_LOCKS = cfg.comment("Randomize lock IDs and combinations when loading them from a structure file. Randomization works just like during world generation").define("Randomize Loaded Locks", false);

		SPEC = cfg.build();
	}

	private LocksConfig() {}

	public static int randLockLen(Random random)
	{
		return random.nextInt(MAX_GENERATED_LOCK_LENGTH.get() - MIN_GENERATED_LOCK_LENGTH.get()) + MIN_GENERATED_LOCK_LENGTH.get();
	}
}