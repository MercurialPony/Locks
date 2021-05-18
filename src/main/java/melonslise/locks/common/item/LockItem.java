package melonslise.locks.common.item;

import java.util.List;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.capability.ISelection;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.Transform;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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
	public final int length;
	public final int enchantmentValue;
	public final int resistance;

	public LockItem(int length, int enchVal, int resist, Properties props)
	{
		super(props);
		this.length = length;
		this.enchantmentValue = enchVal;
		this.resistance = resist;
	}

	public static final String KEY_OPEN = "Open";

	public static boolean isOpen(ItemStack stack)
	{
		return stack.getOrCreateTag().getBoolean(KEY_OPEN);
	}

	public static void setOpen(ItemStack stack, boolean open)
	{
		stack.getOrCreateTag().putBoolean(KEY_OPEN, open);
	}

	public static final String KEY_LENGTH = "Length";

	// WARNING: EXPECTS LOCKITEM STACK
	public static byte getOrSetLength(ItemStack stack)
	{
		CompoundNBT nbt = stack.getOrCreateTag();
		if(!nbt.contains(KEY_LENGTH))
			nbt.putByte(KEY_LENGTH, (byte) ((LockItem) stack.getItem()).length);
		return nbt.getByte(KEY_LENGTH);
	}

	// WARNING: EXPECTS LOCKITEM STACK
	public static int getResistance(ItemStack stack)
	{
		return ((LockItem) stack.getItem()).resistance;
	}

	@Override
	public ActionResultType useOn(ItemUseContext ctx)
	{
		World world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		if (!LocksServerConfig.canLock(world, pos) ||  ctx.getLevel().getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null).getInChunk(pos).values().stream().anyMatch(lkb -> lkb.bb.intersects(pos)))
			return ActionResultType.PASS;
		return LocksServerConfig.EASY_LOCK.get() ? this.easyLock(ctx) : this.freeLock(ctx);
	}

	public ActionResultType freeLock(ItemUseContext ctx)
	{
		PlayerEntity player = ctx.getPlayer();
		BlockPos pos = ctx.getClickedPos();
		ISelection select = player.getCapability(LocksCapabilities.SELECTION).orElse(null);
		BlockPos pos1 = select.get();
		if (pos1 == null)
			select.set(pos);
		else
		{
			World world = ctx.getLevel();
			select.set(null);
			// FIXME Go through the add checks here as well
			world.playSound(player, pos, LocksSoundEvents.LOCK_CLOSE.get(), SoundCategory.BLOCKS, 1f, 1f);
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			ItemStack stack = ctx.getItemInHand();
			ItemStack lockStack = stack.copy();
			lockStack.setCount(1);
			ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
			if (!handler.add(new Lockable(new Cuboid6i(pos1, pos), Lock.from(stack), Transform.fromDirection(ctx.getClickedFace(), player.getDirection().getOpposite()), lockStack, world)))
				return ActionResultType.PASS;
			if (!player.isCreative())
				stack.shrink(1);
		}
		return ActionResultType.SUCCESS;
	}

	public ActionResultType easyLock(ItemUseContext ctx)
	{
		PlayerEntity player = ctx.getPlayer();
		World world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		world.playSound(player, pos, LocksSoundEvents.LOCK_CLOSE.get(), SoundCategory.BLOCKS, 1f, 1f);
		if(world.isClientSide)
			return ActionResultType.SUCCESS;
		BlockState state = world.getBlockState(pos);
		BlockPos pos1 = pos;
		if(state.hasProperty(BlockStateProperties.CHEST_TYPE) && state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE)
			pos1 = pos.relative(ChestBlock.getConnectedDirection(state));
		else if(state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF))
		{
			pos1 = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
			if(state.hasProperty(BlockStateProperties.DOOR_HINGE) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
			{
				Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
				BlockPos pos2 = pos1.relative(state.getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.LEFT ? dir.getClockWise() : dir.getCounterClockWise());
				if(world.getBlockState(pos2).is(state.getBlock()))
					pos1 = pos2;
			}
		}
		ItemStack stack = ctx.getItemInHand();
		ItemStack lockStack = stack.copy();
		lockStack.setCount(1);
		ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
		if (!handler.add(new Lockable(new Cuboid6i(pos, pos1), Lock.from(stack), Transform.fromDirection(ctx.getClickedFace(), player.getDirection().getOpposite()), lockStack, world)))
			return ActionResultType.PASS;
		if (!player.isCreative())
			stack.shrink(1);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(!isOpen(stack))
			return super.use(world, player, hand);
		setOpen(stack, false);
		world.playSound(player, player.getX(), player.getY(), player.getZ(), LocksSoundEvents.PIN_MATCH.get(), SoundCategory.PLAYERS, 1f, 1f);
		return super.use(world, player, hand);
	}

	@Override
	public boolean isEnchantable(ItemStack p_77616_1_)
	{
		return true;
	}

	@Override
	public int getEnchantmentValue()
	{
		return this.enchantmentValue;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> lines, ITooltipFlag flag)
	{
		super.appendHoverText(stack, world, lines, flag);
		lines.add(new TranslationTextComponent(Locks.ID + ".tooltip.length", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(stack.hasTag() && stack.getTag().contains(KEY_LENGTH) ? stack.getTag().getByte(KEY_LENGTH) : this.length)).withStyle(TextFormatting.DARK_GREEN));
	}
}