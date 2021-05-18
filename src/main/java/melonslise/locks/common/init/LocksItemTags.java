package melonslise.locks.common.init;

import melonslise.locks.Locks;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;

public final class LocksItemTags
{
	private LocksItemTags() {}

	public static final ITag.INamedTag<Item>
		KEYS = bind("keys"),
		LOCKS = bind("locks"),
		LOCK_PICKS = bind("lock_picks");

	public static ITag.INamedTag<Item> bind(String name)
	{
		return ItemTags.bind(Locks.ID + ":" + name);
	}
}