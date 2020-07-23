package melonslise.locks.common.item;

import java.util.List;

import javax.annotation.Nullable;

import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.Orientation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LockItem extends LockingItem
{
	public LockItem(Properties props)
	{
		super(props);
	}

	public static final String KEY_LENGTH = "Length";

	public static ItemStack from(Lock lock)
	{
		ItemStack stack = new ItemStack(LocksItems.LOCK);
		CompoundNBT nbt = stack.getOrCreateTag();
		nbt.putInt(KEY_ID, lock.id);
		nbt.putByte(KEY_LENGTH, (byte) lock.getLength());
		return stack;
	}

	public static byte getOrSetLength(ItemStack stack)
	{
		CompoundNBT nbt = stack.getOrCreateTag();
		if(!nbt.contains(KEY_LENGTH))
			nbt.putByte(KEY_LENGTH, LocksServerConfig.DEFAULT_LOCK_LENGTH.get().byteValue());
		return nbt.getByte(KEY_LENGTH);
	}

	// TODO Delegate logic to cap?
	// TODO Change null checks to optionals/whatever
	// TODO Sound pitch
	@Override
	public ActionResultType onItemUse(ItemUseContext ctx)
	{
		BlockPos pos = ctx.getPos();
		World world = ctx.getWorld();
		PlayerEntity player = ctx.getPlayer();
		ItemStack stack = ctx.getItem();

		return Locks.PROXY.getLockables(world)
			.map(lockables ->
			{
				return player.getCapability(LocksCapabilities.LOCK_SELECTION)
					.map(select ->
					{
						if(!LocksServerConfig.canLock(world, pos) || lockables.get().values().stream().anyMatch(lockable1 -> lockable1.box.intersects(pos)))
							return ActionResultType.PASS;
						BlockPos pos1 = select.get();
						if(pos1 == null)
							select.set(pos);
						else
						{
							select.set(null);
							// TODO Go through the add checks here as well
							world.playSound(player, pos, LocksSoundEvents.LOCK_CLOSE, SoundCategory.BLOCKS, 1F, 1F);
							if(world.isRemote)
								return ActionResultType.SUCCESS;
							if(!lockables.add(new Lockable(new Cuboid6i(pos1, pos), Lock.from(stack), Orientation.fromDirection(ctx.getFace(), ctx.getPlacementHorizontalFacing().getOpposite()))))
								return ActionResultType.PASS;
							if(!player.isCreative())
								stack.shrink(1);
						}
						return ActionResultType.SUCCESS;
					})
					.orElse(ActionResultType.PASS);
			})
			.orElse(ActionResultType.PASS);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> lines, ITooltipFlag flag)
	{
		super.addInformation(stack, world, lines, flag);
		int length = stack.hasTag() && stack.getTag().contains(KEY_LENGTH) ? stack.getTag().getByte(KEY_LENGTH) : LocksServerConfig.DEFAULT_LOCK_LENGTH.get();
		lines.add(new TranslationTextComponent(Locks.ID + ".tooltip.length", ItemStack.DECIMALFORMAT.format(length)).func_240699_a_(TextFormatting.DARK_GREEN));
	}
}