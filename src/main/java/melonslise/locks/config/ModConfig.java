package melonslise.locks.config;

import de.siphalor.tweed4.annotated.AConfigBackground;
import de.siphalor.tweed4.annotated.ATweedConfig;
import de.siphalor.tweed4.config.ConfigEnvironment;
import de.siphalor.tweed4.config.ConfigScope;

@ATweedConfig(serializer = "tweed4:gson", scope = ConfigScope.GAME, environment = ConfigEnvironment.UNIVERSAL, tailors = "tweed4:coat")
@AConfigBackground("minecraft:textures/block/stripped_oak_log.png")
public class ModConfig
{
	public static General general = new General();

	@AConfigBackground("minecraft:textures/block/barrel_top.png")
	public static class General
	{

	}

	public static Worldgen worldgen = new Worldgen();

	@AConfigBackground("minecraft:textures/block/mossy_cobblestone.png")
	public static class Worldgen
	{

	}

	public static Visual visual = new Visual();

	@AConfigBackground("minecraft:textures/block/bamboo_block.png")
	public static class Visual
	{

	}
}