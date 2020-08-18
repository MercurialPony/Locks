package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.recipe.LockingItemCloningRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class LocksRecipeSerializers
{
	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Locks.ID);

	public static final RegistryObject<IRecipeSerializer<LockingItemCloningRecipe>>
		LOCKING_ITEM_CLONING = add("locking_item_cloning", new LockingItemCloningRecipe.Serializer());

	private LocksRecipeSerializers() {}

	public static void register()
	{
		RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public static <T extends IRecipe> RegistryObject<IRecipeSerializer<T>> add(String name, IRecipeSerializer<T> serializer)
	{
		return RECIPE_SERIALIZERS.register(name, () -> serializer);
	}
}