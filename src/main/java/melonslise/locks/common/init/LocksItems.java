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

public final class LocksItems
{
	public static final List<Item> ITEMS = new ArrayList<Item>(6);

	public static final Item
		KEY_BLANK = add("key_blank", new Item(new Item.Properties().group(LocksItemGroups.TAB))),
		LOCK = add("lock", new LockItem(new Item.Properties().group(LocksItemGroups.TAB))),
		KEY = add("key", new KeyItem(new Item.Properties().group(LocksItemGroups.TAB))),
		MASTER_KEY = add("master_key", new MasterKeyItem(new Item.Properties().group(LocksItemGroups.TAB))),
		LOCK_PICK = add("lock_pick", new LockPickItem(new Item.Properties().group(LocksItemGroups.TAB))),
		KEY_RING = add("key_ring", new KeyRingItem(new Item.Properties().group(LocksItemGroups.TAB), 1));

	private LocksItems() {}

	public static void register(RegistryEvent.Register<Item> event)
	{
		for(Item item : ITEMS)
			event.getRegistry().register(item);
	}

	public static Item add(String name, Item item)
	{
		ITEMS.add(item.setRegistryName(Locks.ID, name));
		return item;
	}
}