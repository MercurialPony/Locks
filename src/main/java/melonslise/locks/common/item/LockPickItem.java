package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.network.LocksGuiHandler;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.client.util.ITooltipFlag;
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
	public static final String KEY_STRENGTH = "Strength";

	public static float getOrSetStrength(ItemStack stack)
	{
		NBTTagCompound nbt = LocksUtil.getTag(stack);
		if(!nbt.hasKey(KEY_STRENGTH))
			nbt.setFloat(KEY_STRENGTH, (float) LocksConfig.SERVER.defaultLockPickStrength);
		return nbt.getFloat(KEY_STRENGTH);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ)
	{
		ILockableStorage lockables = world.getCapability(LocksCapabilities.LOCKABLES, null);
		List<Lockable> matching = lockables.get().values().stream().filter(LocksPredicates.LOCKED.and(lockable1 -> lockable1.box.intersects(pos))).collect(Collectors.toList());
		if(matching.isEmpty())
			return EnumActionResult.PASS;
		if(world.isRemote)
			return EnumActionResult.SUCCESS;
		player.openGui(Locks.instance, LocksGuiHandler.LOCK_PICKING_ID, world, matching.get(0).networkID, 0, 0);
		return EnumActionResult.SUCCESS;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> lines, ITooltipFlag flag)
	{
		super.addInformation(stack, world, lines, flag);
		float strength = LocksUtil.hasKey(stack, KEY_STRENGTH) ? stack.getTagCompound().getFloat(KEY_STRENGTH) : (float) LocksConfig.getServerClient().defaultLockPickStrength;
		ITextComponent txt = new TextComponentTranslation(Locks.ID + ".tooltip.strength", ItemStack.DECIMALFORMAT.format(strength));
		txt.getStyle().setColor(TextFormatting.DARK_GREEN);
		lines.add(txt.getFormattedText());
	}
}