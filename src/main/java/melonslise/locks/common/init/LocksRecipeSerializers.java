package melonslise.locks.common.init;

import java.util.ArrayList;

import melonslise.locks.common.recipe.LocksRecipeSerializer;
import melonslise.locks.common.recipe.RecipeLockingItemCloning;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;

public final class LocksRecipeSerializers
{
	private static final ArrayList<IRecipeSerializer> SERIALIZERS = new ArrayList<IRecipeSerializer>();

	public static final IRecipeSerializer LOCKING_ITEM_CLONING = add(new RecipeLockingItemCloning.Serializer("locking_item_cloning"));

	private LocksRecipeSerializers() {}

	public static void register(RegistryEvent.Register<IRecipeSerializer<?>> event)
	{
		for(IRecipeSerializer serializer : SERIALIZERS) event.getRegistry().register(serializer);
	}

	public static LocksRecipeSerializer add(LocksRecipeSerializer serializer)
	{
		SERIALIZERS.add(serializer);
		return serializer;
	}
}