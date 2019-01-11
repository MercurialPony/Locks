package melonslise.locks.common.item;

import melonslise.locks.common.capability.LocksCapabilities;
import melonslise.locks.common.item.api.LocksItem;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

public class LocksItems
{
	public static final Item 
	lock = new ItemLock("lock"),
	lock_pick = new ItemLockPick("lock_pick"),
	key  = new ItemKey("key"),
	master_key = new ItemMasterKey("master_key"),
	key_blank = new LocksItem("key_blank"),
	key_ring = new ItemKeyRing("key_ring", 1);

	private LocksItems() {}

	public static void register(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(lock, lock_pick, key, master_key, key_blank, key_ring);
	}

	// TODO Don't create new mdl instances every time
	@SideOnly(Side.CLIENT)
	public static void registerModels(ModelRegistryEvent event)
	{
		registerModels(lock, lock_pick, key, master_key, key_blank);
		ItemMeshDefinition md = new ItemMeshDefinition()
		{
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack)
			{
				int keys = 0;
				IItemHandler inventory = LocksCapabilities.getInventory(stack);
				for(int a = 0; a < inventory.getSlots() && keys < 3; ++a) if(!inventory.getStackInSlot(a).isEmpty()) ++keys;
				return new ModelResourceLocation(key_ring.getRegistryName() + "_" + keys, "inventory");
			}
		};
		ModelLoader.setCustomMeshDefinition(key_ring, md);
		ModelLoader.registerItemVariants(key_ring, new ModelResourceLocation(key_ring.getRegistryName() + "_" + 0, "inventory"), new ModelResourceLocation(key_ring.getRegistryName() + "_" + 1, "inventory"), new ModelResourceLocation(key_ring.getRegistryName() + "_" + 2, "inventory"), new ModelResourceLocation(key_ring.getRegistryName() + "_" + 3, "inventory"));
	}

	public static void registerModels(Item... items)
	{
		for(Item item : items) ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	public static void registerVariants(Item item, String... variants)
	{
		
	}
}