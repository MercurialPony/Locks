package melonslise.locks.client.init;

import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.item.LockItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;

@OnlyIn(Dist.CLIENT)
public final class LocksItemModelsProperties
{
	private LocksItemModelsProperties() {}

	public static void register()
	{
		ItemModelsProperties.register(LocksItems.KEY_RING.get(), new ResourceLocation(Locks.ID, "keys"), (stack, world, entity) ->
		{
			return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.map(inv ->
				{
					int keys = 0;
					for(int a = 0; a < inv.getSlots(); ++a)
						if(!inv.getStackInSlot(a).isEmpty())
							++keys;
					return (float) keys / inv.getSlots();
				})
				.orElse(0f);
		});
		ResourceLocation id = new ResourceLocation(Locks.ID, "open");
		IItemPropertyGetter getter = (stack, world, entity) -> LockItem.isOpen(stack) ? 1f : 0f;
		ItemModelsProperties.register(LocksItems.WOOD_LOCK.get(), id, getter);
		ItemModelsProperties.register(LocksItems.IRON_LOCK.get(), id, getter);
		ItemModelsProperties.register(LocksItems.STEEL_LOCK.get(), id, getter);
		ItemModelsProperties.register(LocksItems.GOLD_LOCK.get(), id, getter);
		ItemModelsProperties.register(LocksItems.DIAMOND_LOCK.get(), id, getter);
	}
}