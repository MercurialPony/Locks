package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksEnchantments;
import melonslise.locks.common.network.LocksGuiHandler;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LockPickItem extends Item
{
	public static final ITextComponent TOO_COMPLEX_MESSAGE = new TextComponentTranslation(Locks.ID + ".status.too_complex");
	
	public static final String KEY_STRENGTH = "Strength";
	
	public final float strength;
	
	public LockPickItem(float strength)
	{
		super();
		this.strength = strength;
	}

	/*
	public static float getOrSetStrength(ItemStack stack)
	{
		NBTTagCompound nbt = LocksUtil.getTag(stack);
		if(!nbt.hasKey(KEY_STRENGTH))
			nbt.setFloat(KEY_STRENGTH, ((LockPickItem)stack.getItem()).strength);
		return nbt.getFloat(KEY_STRENGTH);
	}
	*/
	
	public static float getStrength(ItemStack stack)
	{
		//Avoid creating blank tag compounds
		if(!stack.hasTagCompound())
			return ((LockPickItem)stack.getItem()).strength;
			
		NBTTagCompound nbt = stack.getTagCompound();
		if(!nbt.hasKey(KEY_STRENGTH))
			return ((LockPickItem)stack.getItem()).strength;
		return nbt.getFloat(KEY_STRENGTH);
	}
	
	public static boolean canPick(ItemStack stack, int cmp)
	{
		return getStrength(stack) > cmp * 0.25f;
	}
	
	public static boolean canPick(ItemStack stack, Lockable lkb)
	{
		return canPick(stack, EnchantmentHelper.getEnchantmentLevel(LocksEnchantments.COMPLEXITY, lkb.stack));
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ)
	{
		List<Lockable> matching = LocksUtil.intersecting(world, pos).filter(LocksPredicates.LOCKED).collect(Collectors.toList());
		if(matching.isEmpty())
			return EnumActionResult.PASS;
		Lockable lkb = matching.get(0);
		if(!canPick(player.getHeldItem(hand), lkb))
		{
			if(world.isRemote)
				player.sendStatusMessage(TOO_COMPLEX_MESSAGE, true);
			return EnumActionResult.PASS;
		}
		if(world.isRemote)
			return EnumActionResult.SUCCESS;
		player.openGui(Locks.instance, LocksGuiHandler.LOCK_PICKING_ID, world, lkb.networkID, hand == EnumHand.MAIN_HAND ? 0 : 1, 0);
		return EnumActionResult.SUCCESS;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> lines, ITooltipFlag flag)
	{
		super.addInformation(stack, world, lines, flag);
		float strength = LocksUtil.hasKey(stack, KEY_STRENGTH) ? stack.getTagCompound().getFloat(KEY_STRENGTH) : this.strength;
		ITextComponent txt = new TextComponentTranslation(Locks.ID + ".tooltip.strength", ItemStack.DECIMALFORMAT.format(strength));
		txt.getStyle().setColor(TextFormatting.DARK_GREEN);
		lines.add(txt.getFormattedText());
	}
}