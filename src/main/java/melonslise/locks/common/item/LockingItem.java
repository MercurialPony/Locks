package melonslise.locks.common.item;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import melonslise.locks.Locks;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LockingItem extends Item
{
	public LockingItem()
	{
		this.setMaxStackSize(1);
	}

	public static final String KEY_ID = "id";

	public static ItemStack copyId(ItemStack from, ItemStack to)
	{
		LocksUtil.getTag(to).setInteger(KEY_ID, getOrSetId(from));
		return to;
	}

	public static int getOrSetId(ItemStack stack)
	{
		NBTTagCompound nbt = LocksUtil.getTag(stack);
		if(!nbt.hasKey(KEY_ID))
			nbt.setInteger(KEY_ID, ThreadLocalRandom.current().nextInt());
		return nbt.getInteger(KEY_ID);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		if(!world.isRemote)
			getOrSetId(stack);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> lines, ITooltipFlag flag)
	{
		if(LocksUtil.hasKey(stack, KEY_ID))
		{
			ITextComponent txt = new TextComponentTranslation(Locks.ID + ".tooltip.id", ItemStack.DECIMALFORMAT.format(getOrSetId(stack)));
			txt.getStyle().setColor(TextFormatting.DARK_GREEN);
			lines.add(txt.getFormattedText());
		}
	}
}