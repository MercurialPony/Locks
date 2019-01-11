package melonslise.locks.common.crafting.recipe;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import melonslise.locks.common.item.api.lockable.ItemLockable;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeLockableCloning extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
	public final Ingredient lockable;
	public final NonNullList<Ingredient> blanks;
	public final ItemStack result;

	public RecipeLockableCloning(Ingredient lockable, NonNullList<Ingredient> blanks, ItemStack result)
	{
		this.lockable = lockable;
		this.blanks = blanks;
		this.result = result;
	}

	public Ingredient getLockable()
	{
		return this.lockable;
	}

	public NonNullList<Ingredient> getBlanks()
	{
		return this.blanks;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return this.result;
	}

	// TODO Add simple check like in ShapelessRecipes?
	@Override
	public boolean matches(InventoryCrafting inventory, World world)
	{
		RecipeItemHelper helper = new RecipeItemHelper();
		ItemStack lockable = ItemStack.EMPTY;
		ArrayList<ItemStack> blanks = Lists.newArrayList();
		for (int a = 0; a < inventory.getSizeInventory(); ++a)
		{
			ItemStack stack = inventory.getStackInSlot(a);
			if(stack.isEmpty()) continue;
			if(LocksUtilities.hasUUID(stack, ItemLockable.keyIdentifier) && this.lockable.apply(stack)) lockable = stack;
			else blanks.add(stack);
		}
		return !lockable.isEmpty() && RecipeMatcher.findMatches(blanks, this.blanks) != null;
	}

	// TODO Add container item compatibility?
	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory)
	{
		NonNullList<ItemStack> stacks = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
		for(int a = 0; a < inventory.getSizeInventory(); ++a)
		{
			ItemStack stack = inventory.getStackInSlot(a);
			if(this.lockable.apply(stack)) stacks.set(a, stack.copy());
		}
		return stacks;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory)
	{
		ItemStack lockable = ItemStack.EMPTY;
		for(int a = 0; a < inventory.getSizeInventory() && lockable.isEmpty(); ++a)
		{
			ItemStack stack = inventory.getStackInSlot(a);
			if(!stack.isEmpty() && LocksUtilities.hasUUID(stack, ItemLockable.keyIdentifier) && this.lockable.apply(stack)) lockable = stack;
		}
		return ItemLockable.copyID(this.result.copy(), lockable);
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return this.blanks.size() + 1 <= width * height;
	}



	// TODO Look at ShapelessOreRecipe
	public static class Factory implements IRecipeFactory
	{
		// TODO Static final keys

		@Override
		public IRecipe parse(JsonContext context, JsonObject json)
		{
			Ingredient lockable = ShapedRecipes.deserializeIngredient(json.get("lockable"));
			NonNullList<Ingredient> blanks = deserializeIngredients(JsonUtils.getJsonArray(json, "blanks"));
			if(blanks.isEmpty()) throw new JsonParseException("Not enough items for lockable recipe");
			if(blanks.size() > 8) throw new JsonParseException("Too many items for lockable recipe");
			ItemStack result = ShapedRecipes.deserializeItem(JsonUtils.getJsonObject(json, "result"), true);
			return new RecipeLockableCloning(lockable, blanks, result);
		}

	    private static NonNullList<Ingredient> deserializeIngredients(JsonArray array)
	    {
	        NonNullList<Ingredient> list = NonNullList.create();
	        for(JsonElement element : array)
	        {
	            Ingredient ingredient = ShapedRecipes.deserializeIngredient(element);
	            if(ingredient != Ingredient.EMPTY) list.add(ingredient);
	        }
	        return list;
	    }
	}
}