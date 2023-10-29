package melonslise.locks.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public final class ModItemTabs
{
	public static final ItemGroup MAIN = FabricItemGroup.builder()
		.displayName(Text.translatable("itemGroup.locks.main"))
		.icon(Items.APPLE::getDefaultStack)
		.entries((ctx, entries) -> RegistryScannerV2.<Item>all(ModItems.class).map(Item::getDefaultStack).forEach(entries::add)) // TODO: cache
		.build();
}