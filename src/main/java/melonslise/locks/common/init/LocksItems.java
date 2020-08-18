package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.item.KeyItem;
import melonslise.locks.common.item.KeyRingItem;
import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.item.LockPickItem;
import melonslise.locks.common.item.MasterKeyItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class LocksItems
{
	public static DeferredRegister ITEMS = new DeferredRegister(ForgeRegistries.ITEMS, Locks.ID);

	public static final RegistryObject<Item>
		KEY_BLANK = add("key_blank", new Item(new Item.Properties().group(LocksItemGroups.TAB))),
		LOCK = add("lock", new LockItem(new Item.Properties().group(LocksItemGroups.TAB))),
		KEY = add("key", new KeyItem(new Item.Properties().group(LocksItemGroups.TAB))),
		MASTER_KEY = add("master_key", new MasterKeyItem(new Item.Properties().group(LocksItemGroups.TAB))),
		LOCK_PICK = add("lock_pick", new LockPickItem(new Item.Properties().group(LocksItemGroups.TAB))),
		KEY_RING = add("key_ring", new KeyRingItem(new Item.Properties().group(LocksItemGroups.TAB), 1));

	private LocksItems() {}

	public static void register()
	{
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public static RegistryObject<Item> add(String name, Item item)
	{
		return ITEMS.register(name, () -> item);
	}
}