package melonslise.locks.common.recipe;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import melonslise.locks.common.init.LocksRecipeSerializers;
import melonslise.locks.common.item.ItemLocking;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.RecipeMatcher;

// TODO Group
public class RecipeLockingItemCloning extends SpecialRecipe
{
	public final Ingredient locking;
	public final NonNullList<Ingredient> blanks;
	public final ItemStack result;

	public RecipeLockingItemCloning(ResourceLocation id, Ingredient locking, NonNullList<Ingredient> blanks, ItemStack result)
	{
		super(id);
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
	public boolean matches(CraftingInventory inventory, World world)
	{
		RecipeItemHelper helper = new RecipeItemHelper();
		ItemStack locking = ItemStack.EMPTY;
		ArrayList<ItemStack> blanks = Lists.newArrayList();
		for (int a = 0; a < inventory.getSizeInventory(); ++a)
		{
			ItemStack stack = inventory.getStackInSlot(a);
			if(stack.isEmpty()) continue;
			if(LocksUtilities.hasKey(stack, ItemLocking.KEY_ID) && this.locking.test(stack)) locking = stack;
			else blanks.add(stack);
		}
		return !locking.isEmpty() && RecipeMatcher.findMatches(blanks, this.blanks) != null;
	}

	// TODO Add container item compatibility?
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inventory)
	{
		NonNullList<ItemStack> stacks = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
		for(int a = 0; a < inventory.getSizeInventory(); ++a)
		{
			ItemStack stack = inventory.getStackInSlot(a);
			if(this.locking.test(stack)) stacks.set(a, stack.copy());
		}
		return stacks;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inventory)
	{
		ItemStack locking = ItemStack.EMPTY;
		for(int a = 0; a < inventory.getSizeInventory() && locking.isEmpty(); ++a)
		{
			ItemStack stack = inventory.getStackInSlot(a);
			if(!stack.isEmpty() && LocksUtilities.hasKey(stack, ItemLocking.KEY_ID) && this.locking.test(stack)) locking = stack;
		}
		return ItemLocking.copyID(locking, this.result.copy());
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return this.blanks.size() + 1 <= width * height;
	}

	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return LocksRecipeSerializers.LOCKING_ITEM_CLONING;
	}

	public static class Serializer extends LocksRecipeSerializer
	{
		public Serializer(String name)
		{
			super(name);
		}

		// TODO Static final keys
		@Override
		public RecipeLockingItemCloning read(ResourceLocation id, JsonObject json)
		{
			Ingredient locking = Ingredient.deserialize(json.get("locking"));
			NonNullList<Ingredient> blanks = deserializeIngredients(JSONUtils.getJsonArray(json, "blanks"));
			if(blanks.isEmpty()) throw new JsonParseException("Not enough items for locking item cloning recipe");
			if(blanks.size() > 8) throw new JsonParseException("Too many items for locking item cloning recipe");
			ItemStack result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
			return new RecipeLockingItemCloning(id, locking, blanks, result);
		}

		private static NonNullList<Ingredient> deserializeIngredients(JsonArray array)
		{
			NonNullList<Ingredient> list = NonNullList.create();
			for(int a = 0; a < array.size(); ++a)
			{
				Ingredient ingredient = Ingredient.deserialize(array.get(a));
				if (!ingredient.hasNoMatchingItems()) list.add(ingredient);
			}
			return list;
		}

		@Override
		public RecipeLockingItemCloning read(ResourceLocation id, PacketBuffer buffer)
		{
			Ingredient locking = Ingredient.read(buffer);
			int size = buffer.readVarInt();
			NonNullList<Ingredient> blanks = NonNullList.withSize(size, Ingredient.EMPTY);
			for(int a = 0; a < size; ++a) blanks.set(a, Ingredient.read(buffer));
			ItemStack result = buffer.readItemStack();
			return new RecipeLockingItemCloning(id, locking, blanks, result);
		}

		@Override
		public void write(PacketBuffer buffer, RecipeLockingItemCloning recipe)
		{
			recipe.locking.write(buffer);
			buffer.writeVarInt(recipe.blanks.size());
			for(Ingredient ingredient : recipe.blanks) ingredient.write(buffer);
			buffer.writeItemStack(recipe.result);
		}
	}
}