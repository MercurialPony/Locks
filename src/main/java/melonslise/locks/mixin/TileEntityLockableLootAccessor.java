package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.NonNullList;

@Mixin(TileEntityLockableLoot.class)
public interface TileEntityLockableLootAccessor
{
	@Invoker("getItems")
	public NonNullList<ItemStack> getItems();
}
