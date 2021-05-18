package melonslise.locks.common.item;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import melonslise.locks.Locks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LockingItem extends Item
{
	public LockingItem(Properties props)
	{
		super(props.stacksTo(1));
	}

	public static final String KEY_ID = "Id";

	public static ItemStack copyId(ItemStack from, ItemStack to)
	{
		to.getOrCreateTag().putInt(KEY_ID, getOrSetId(from));
		return to;
	}

	public static int getOrSetId(ItemStack stack)
	{
		CompoundNBT nbt = stack.getOrCreateTag();
		if(!nbt.contains(KEY_ID))
			nbt.putInt(KEY_ID, ThreadLocalRandom.current().nextInt());
		return nbt.getInt(KEY_ID);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		if(!world.isClientSide)
			getOrSetId(stack);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> lines, ITooltipFlag flag)
	{
		if(stack.hasTag() && stack.getTag().contains(KEY_ID))
			lines.add(new TranslationTextComponent(Locks.ID + ".tooltip.id", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(getOrSetId(stack))).withStyle(TextFormatting.DARK_GREEN));
	}
}