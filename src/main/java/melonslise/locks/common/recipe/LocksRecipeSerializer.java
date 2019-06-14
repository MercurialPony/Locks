package melonslise.locks.common.recipe;

import melonslise.locks.Locks;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class LocksRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RecipeLockingItemCloning>
{
	public LocksRecipeSerializer(String name)
	{
		this.setRegistryName(Locks.ID, name);
	}
}