package melonslise.locks.common.init;

import java.util.ArrayList;
import java.util.List;

import melonslise.locks.common.item.ItemKey;
import melonslise.locks.common.item.ItemKeyRing;
import melonslise.locks.common.item.ItemLock;
import melonslise.locks.common.item.ItemLockPick;
import melonslise.locks.common.item.ItemMasterKey;
import melonslise.locks.common.item.LocksItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

public final class LocksItems
{
	private static final List<Item> ITEMS = new ArrayList<Item>();

	public static final Item
	KEY_BLANK = add(new LocksItem("key_blank", new Item.Properties())),
	LOCK = add(new ItemLock("lock", new Item.Properties(), 7)),
	KEY = add(new ItemKey("key", new Item.Properties())),
	MASTER_KEY = add(new ItemMasterKey("master_key", new Item.Properties())),
	LOCK_PICK = add(new ItemLockPick("lock_pick", new Item.Properties(), 0.65f)),
	KEY_RING = add(new ItemKeyRing("key_ring", new Item.Properties(), 1));

	private LocksItems() {}

	public static void register(RegistryEvent.Register<Item> event)
	{
		for(Item item : ITEMS) event.getRegistry().register(item);
	}

	public static LocksItem add(LocksItem item)
	{
		ITEMS.add(item);
		return item;
	}
}