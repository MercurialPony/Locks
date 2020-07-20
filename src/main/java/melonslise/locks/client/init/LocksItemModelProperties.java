package melonslise.locks.client.init;

import javax.annotation.Nullable;

import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksItems;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;

@OnlyIn(Dist.CLIENT)
public final class LocksItemModelProperties
{
	private LocksItemModelProperties() {}

	public static void register()
	{
		ItemModelsProperties.func_239418_a_(LocksItems.KEY_RING, new ResourceLocation(Locks.ID, "keys"), (ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) ->
		{
			return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.map(inventory ->
				{
					int keys = 0;
					for(int a = 0; a < inventory.getSlots(); ++a)
						if(!inventory.getStackInSlot(a).isEmpty())
							++keys;
					return (float) keys / (float) inventory.getSlots();
				})
				.orElse(0f);
		});
	}
}