package melonslise.locks.common.item;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemLocking extends LocksItem
{
	// TODO Stack size
	public ItemLocking(String name, Properties properties)
	{
		super(name, properties.maxStackSize(1));
	}

	public static final String KEY_ID = "id";

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		if(LocksUtilities.hasKey(stack, KEY_ID)) tooltip.add(new StringTextComponent(Integer.toString(stack.getTag().getInt(KEY_ID))));
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		if(world.isRemote || LocksUtilities.hasKey(stack, KEY_ID)) return;
		getID(stack);
	}

	public static ItemStack assignID(ItemStack stack, int id)
	{
		LocksUtilities.getTag(stack).putInt(KEY_ID, id);
		return stack;
	}

	public static ItemStack copyID(ItemStack from, ItemStack to)
	{
		return assignID(to, getID(from));
	}

	public static int getID(ItemStack stack)
	{
		if(!LocksUtilities.hasKey(stack, KEY_ID)) LocksUtilities.getTag(stack).putInt(KEY_ID, ThreadLocalRandom.current().nextInt());
		return stack.getTag().getInt(KEY_ID);
	}
}