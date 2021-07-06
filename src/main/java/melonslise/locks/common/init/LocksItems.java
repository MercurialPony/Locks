package melonslise.locks.common.init;

import java.util.ArrayList;
import java.util.List;

import melonslise.locks.Locks;
import melonslise.locks.common.item.KeyItem;
import melonslise.locks.common.item.KeyRingItem;
import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.item.LockPickItem;
import melonslise.locks.common.item.MasterKeyItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.oredict.OreDictionary;

public final class LocksItems
{
	public static final List<Item> ITEMS = new ArrayList<Item>(18);
	public static final String OREDICT_LOCK = "locksLock";
	public static final String OREDICT_LOCKPICK = "locksLockPick";

	public static final Item
		SPRING = add("spring", new Item().setCreativeTab(LocksCreativeTabs.TAB)),
		WOOD_LOCK_MECHANISM  = add("wood_lock_mechanism", new Item().setCreativeTab(LocksCreativeTabs.TAB)),
		IRON_LOCK_MECHANISM  = add("iron_lock_mechanism", new Item().setCreativeTab(LocksCreativeTabs.TAB)),
		STEEL_LOCK_MECHANISM  = add("steel_lock_mechanism", new Item().setCreativeTab(LocksCreativeTabs.TAB)),
		KEY_BLANK = add("key_blank", new Item().setCreativeTab(LocksCreativeTabs.TAB)),
		WOOD_LOCK = add("wood_lock", new LockItem(5,15,4.0f).setCreativeTab(LocksCreativeTabs.TAB)),
		IRON_LOCK = add("iron_lock", new LockItem(7,14,12.0f).setCreativeTab(LocksCreativeTabs.TAB)),
		STEEL_LOCK = add("steel_lock", new LockItem(9,12,20.0f).setCreativeTab(LocksCreativeTabs.TAB)),
		GOLD_LOCK = add("gold_lock", new LockItem(6,22,6.0f).setCreativeTab(LocksCreativeTabs.TAB)),
		DIAMOND_LOCK = add("diamond_lock", new LockItem(11,10,100.0f).setCreativeTab(LocksCreativeTabs.TAB)),
		KEY = add("key", new KeyItem().setCreativeTab(LocksCreativeTabs.TAB)),
		MASTER_KEY = add("master_key", new MasterKeyItem().setCreativeTab(LocksCreativeTabs.TAB)),
		KEY_RING = add("key_ring", new KeyRingItem(1).setCreativeTab(LocksCreativeTabs.TAB)),
		WOOD_LOCK_PICK = add("wood_lock_pick", new LockPickItem(0.2f).setCreativeTab(LocksCreativeTabs.TAB)),
		IRON_LOCK_PICK = add("iron_lock_pick", new LockPickItem(0.35f).setCreativeTab(LocksCreativeTabs.TAB)),
		STEEL_LOCK_PICK = add("steel_lock_pick", new LockPickItem(0.7f).setCreativeTab(LocksCreativeTabs.TAB)),
		GOLD_LOCK_PICK = add("gold_lock_pick", new LockPickItem(0.25f).setCreativeTab(LocksCreativeTabs.TAB)),
		DIAMOND_LOCK_PICK = add("diamond_lock_pick", new LockPickItem(0.85f).setCreativeTab(LocksCreativeTabs.TAB));
		
	private LocksItems() {}

	public static void register(RegistryEvent.Register<Item> event)
	{
		for(Item item : ITEMS)
			event.getRegistry().register(item);
		
		OreDictionary.registerOre(OREDICT_LOCK, WOOD_LOCK);
		OreDictionary.registerOre(OREDICT_LOCK, IRON_LOCK);
		OreDictionary.registerOre(OREDICT_LOCK, STEEL_LOCK);
		OreDictionary.registerOre(OREDICT_LOCK, GOLD_LOCK);
		OreDictionary.registerOre(OREDICT_LOCK, DIAMOND_LOCK);
		
		OreDictionary.registerOre(OREDICT_LOCKPICK, WOOD_LOCK_PICK);
		OreDictionary.registerOre(OREDICT_LOCKPICK, IRON_LOCK_PICK);
		OreDictionary.registerOre(OREDICT_LOCKPICK, STEEL_LOCK_PICK);
		OreDictionary.registerOre(OREDICT_LOCKPICK, GOLD_LOCK_PICK);
		OreDictionary.registerOre(OREDICT_LOCKPICK, DIAMOND_LOCK_PICK);
	}

	public static Item add(String name, Item item)
	{
		ITEMS.add(item.setRegistryName(Locks.ID, name).setUnlocalizedName(Locks.ID + "." + name));
		return item;
	}
}