package melonslise.locks.common.init;

import java.util.ArrayList;

import melonslise.locks.Locks;
import melonslise.locks.common.recipe.LockingItemCloningRecipe;
import melonslise.locks.common.recipe.RecipeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;

public final class LocksRecipeSerializers
{
	public static final ArrayList<RecipeSerializer> SERIALIZERS = new ArrayList<RecipeSerializer>(1);

	public static final RecipeSerializer
		LOCKING_ITEM_CLONING = add("locking_item_cloning", new LockingItemCloningRecipe.Serializer());

	private LocksRecipeSerializers() {}

	public static void register(RegistryEvent.Register<IRecipeSerializer<?>> event)
	{
		for(RecipeSerializer serializer : SERIALIZERS)
			event.getRegistry().register(serializer);
	}

	public static RecipeSerializer add(String name, RecipeSerializer serializer)
	{
		SERIALIZERS.add((RecipeSerializer) serializer.setRegistryName(new ResourceLocation(Locks.ID, name)));
		return serializer;
	}
}