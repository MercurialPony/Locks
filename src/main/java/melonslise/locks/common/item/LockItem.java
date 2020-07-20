package melonslise.locks.common.item;

import java.util.List;

import javax.annotation.Nullable;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.capability.ISelection;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.util.Orientation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LockItem extends LockingItem
{
	public static final byte DEFAULT_LENGTH = 7;

	public static final String KEY_LENGTH = "Length";

	public static ItemStack from(Lock lock)
	{
		ItemStack stack = new ItemStack(LocksItems.LOCK);
		NBTTagCompound nbt = LocksUtil.getTag(stack);
		nbt.setInteger(KEY_ID, lock.id);
		nbt.setInteger(KEY_LENGTH, lock.getLength());
		return stack;
	}

	public static byte getOrSetLength(ItemStack stack)
	{
		NBTTagCompound nbt = LocksUtil.getTag(stack);
		if(!nbt.hasKey(KEY_LENGTH))
			nbt.setByte(KEY_LENGTH, DEFAULT_LENGTH);
		return nbt.getByte(KEY_LENGTH);
	}

	// TODO Delegate logic to cap?
	// TODO Change null checks to optionals/whatever
	// TODO Sound pitch
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		ILockableStorage lockables = world.getCapability(LocksCapabilities.LOCKABLES, null);
		ISelection select = player.getCapability(LocksCapabilities.LOCK_SELECTION, null);
		if (!LocksConfig.getServer(world).canLock(world, pos) || lockables.get().values().stream().anyMatch(lockable1 -> lockable1.box.intersects(pos)))
			return EnumActionResult.PASS;
		BlockPos pos1 = select.get();
		if (pos1 == null)
			select.set(pos);
		else
		{
			select.set(null);
			// TODO Go through the add checks here as well
			world.playSound(player, pos, LocksSoundEvents.LOCK_CLOSE, SoundCategory.BLOCKS, 1F, 1F);
			if (world.isRemote)
				return EnumActionResult.SUCCESS;
			if (!lockables.add(new Lockable(new Cuboid6i(pos1, pos), Lock.from(stack), Orientation.fromDirection(face, player.getHorizontalFacing().getOpposite()))))
				return EnumActionResult.PASS;
			if (!player.isCreative())
				stack.shrink(1);
		}
		return EnumActionResult.SUCCESS;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		if(!this.isInCreativeTab(tab))
			return;
		ItemStack stack = new ItemStack(this);
		getOrSetLength(stack);
		items.add(stack);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> lines, ITooltipFlag flag)
	{
		super.addInformation(stack, world, lines, flag);
		if(LocksUtil.hasKey(stack, KEY_LENGTH))
		{
			ITextComponent txt = new TextComponentTranslation(Locks.ID + ".tooltip.length", ItemStack.DECIMALFORMAT.format(getOrSetLength(stack)));
			txt.getStyle().setColor(TextFormatting.DARK_GREEN);
			lines.add(txt.getFormattedText());
		}
	}
}