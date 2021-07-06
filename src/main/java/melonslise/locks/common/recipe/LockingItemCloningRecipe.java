package melonslise.locks.common.recipe;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import melonslise.locks.common.item.LockingItem;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.registries.IForgeRegistryEntry;

// TODO Group
public class LockingItemCloningRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
	public final Ingredient locking;
	public final NonNullList<Ingredient> blanks;
	public final ItemStack result;

	public LockingItemCloningRecipe(Ingredient locking, NonNullList<Ingredient> blanks, ItemStack result)
	{
		this.locking = locking;
		this.blanks = blanks;
		this.result = result;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return this.result;
	}

	// TODO Add simple check like in ShapelessRecipes?
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack locking = ItemStack.EMPTY;
		ArrayList<ItemStack> blanks = Lists.newArrayList();
		for (int a = 0; a < inv.getSizeInventory(); ++a)
		{
			ItemStack stack = inv.getStackInSlot(a);
			if(stack.isEmpty())
				continue;
			if(LocksUtil.hasKey(stack, LockingItem.KEY_ID) && this.locking.test(stack))
				locking = stack;
			else
				blanks.add(stack);
		}
		return !locking.isEmpty() && RecipeMatcher.findMatches(blanks, this.blanks) != null;
	}

	// TODO Add container item compatibility?
	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		NonNullList<ItemStack> stacks = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		for(int a = 0; a < inv.getSizeInventory(); ++a)
		{
			ItemStack stack = inv.getStackInSlot(a);
			if(this.locking.test(stack))
				stacks.set(a, stack.copy());
		}
		return stacks;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory)
	{
		ItemStack locking = ItemStack.EMPTY;
		for(int a = 0; a < inventory.getSizeInventory() && locking.isEmpty(); ++a)
		{
			ItemStack stack = inventory.getStackInSlot(a);
			if(!stack.isEmpty() && LocksUtil.hasKey(stack, LockingItem.KEY_ID) && this.locking.test(stack))
				locking = stack;
		}
		return LockingItem.copyId(locking, this.result.copy());
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return this.blanks.size() + 1 <= width * height;
	}

	public static class Factory implements IRecipeFactory
	{
		// TODO Static final keys
		@Override
		public IRecipe parse(JsonContext ctx, JsonObject json)
		{
			//Ingredient locking = ShapedRecipes.deserializeIngredient(json.get("locking"));
			Ingredient locking = CraftingHelper.getIngredient(json.get("locking"), ctx); //OreDict Support
			
			NonNullList<Ingredient> blanks = deserializeIngredients(JsonUtils.getJsonArray(json, "blanks"));
			if(blanks.isEmpty())
				throw new JsonParseException("Not enough items for locking item cloning recipe");
			if(blanks.size() > 8)
				throw new JsonParseException("Too many items for locking item cloning recipe");
			ItemStack result = ShapedRecipes.deserializeItem(JsonUtils.getJsonObject(json, "result"), true);
			return new LockingItemCloningRecipe(locking, blanks, result);
		}

		private static NonNullList<Ingredient> deserializeIngredients(JsonArray array)
		{
			NonNullList<Ingredient> list = NonNullList.create();
			for(int a = 0; a < array.size(); ++a)
			{
				Ingredient ingredient = ShapedRecipes.deserializeIngredient(array.get(a));
				if(ingredient != Ingredient.EMPTY)
					list.add(ingredient);
			}
			return list;
		}
	}
}