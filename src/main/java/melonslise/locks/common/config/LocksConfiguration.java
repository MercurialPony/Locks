package melonslise.locks.common.config;

import melonslise.locks.LocksCore;
import melonslise.locks.common.network.LocksNetworks;
import melonslise.locks.common.network.client.MessageConfiguration;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;

@Config(modid = LocksCore.ID)
public class LocksConfiguration
{
	public static final Configuration main = new Configuration();
	public static final ClientConfiguration client = new ClientConfiguration();
	protected static final Configuration main_client = new Configuration();

	public static Configuration getMain(World world)
	{
		return world.isRemote ? main_client : main;
	}

	public static void synchronize(EntityPlayerMP player)
	{
		LocksNetworks.network.sendTo(new MessageConfiguration(main), player);
	}

	public static class Configuration
	{
		@Config.Comment("Length of a new lock")
		public int lock_length = 6;

		@Config.Comment("Chance of a lock pick to not break")
		public float lock_pick_strength = 0.65F;

		@Config.Comment("Maximum allowed volume of a single lock")
		public int lockable_volume = 4;

		@Config.Comment({"Blocks that can be locked.", "Syntax is the mod domain followed by the block's registry name"})
		public String[] lockable_blocks = {"minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest", "minecraft:furnace", "minecraft:dispenser", "minecraft:dropper", "minecraft:hopper",  "minecraft:wooden_door", "minecraft:iron_door", "minecraft:spruce_door", "minecraft:birch_door", "minecraft:jungle_door", "minecraft:acacia_door", "minecraft:dark_oak_door", "minecraft:trapdoor", "minecraft:iron_trapdoor", "minecraft:fence_gate", "minecraft:spruce_fence_gate", "minecraft:birch_fence_gate", "minecraft:jungle_fence_gate", "minecraft:acacia_fence_gate", "minecraft:dark_oak_fence_gate"};

		@Config.Comment("Generate every new chest with a random lock")
		public boolean generate_locks = true;

		@Config.Comment("Open locks can be removed with an empty hand while sneaking")
		public boolean remove_locks = true;

		@Config.Comment("Locked blocks cannot be destroyed in survival mode")
		public boolean unbreakable_locks = true;
	}

	public static class ClientConfiguration
	{
		@Config.Comment("Enable the client user interface when placing a lock")
		public boolean enable_cui = true;
	}
}