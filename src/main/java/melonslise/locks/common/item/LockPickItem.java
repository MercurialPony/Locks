package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import melonslise.locks.Locks;
import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksEnchantments;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class LockPickItem extends Item
{
	public static final ITextComponent TOO_COMPLEX_MESSAGE = new TranslationTextComponent(Locks.ID + ".status.too_complex");

	public final float strength;

	public LockPickItem(float strength, Properties props)
	{
		super(props);
		this.strength = strength;
	}

	public static final String KEY_STRENGTH = "Strength";

	// WARNING: EXPECTS LOCKPICKITEM STACK
	public static float getOrSetStrength(ItemStack stack)
	{
		CompoundNBT nbt = stack.getOrCreateTag();
		if(!nbt.contains(KEY_STRENGTH))
			nbt.putFloat(KEY_STRENGTH, ((LockPickItem) stack.getItem()).strength);
		return nbt.getFloat(KEY_STRENGTH);
	}

	public static boolean canPick(ItemStack stack, int cmp)
	{
		return getOrSetStrength(stack) > cmp * 0.25f;
	}

	public static boolean canPick(ItemStack stack, Lockable lkb)
	{
		return canPick(stack, EnchantmentHelper.getItemEnchantmentLevel(LocksEnchantments.COMPLEXITY.get(), lkb.stack));
	}

	@Override
	public ActionResultType useOn(ItemUseContext ctx)
	{
		World world = ctx.getLevel();
		PlayerEntity player = ctx.getPlayer();
		BlockPos pos = ctx.getClickedPos();
		List<Lockable> match = LocksUtil.intersecting(world, pos).filter(LocksPredicates.LOCKED).collect(Collectors.toList());
		if(match.isEmpty())
			return ActionResultType.PASS;
		Lockable lkb = match.get(0);
		if(!canPick(ctx.getItemInHand(), lkb))
		{
			if(world.isClientSide)
				player.displayClientMessage(TOO_COMPLEX_MESSAGE, true);
			return ActionResultType.PASS;
		}
		if(world.isClientSide)
			return ActionResultType.SUCCESS;
		Hand hand = ctx.getHand();
		NetworkHooks.openGui((ServerPlayerEntity) player, new LockPickingContainer.Provider(hand, lkb), new LockPickingContainer.Writer(hand, lkb));
		return ActionResultType.SUCCESS;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> lines, ITooltipFlag flag)
	{
		super.appendHoverText(stack, world, lines, flag);
		lines.add(new TranslationTextComponent(Locks.ID + ".tooltip.strength", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(stack.hasTag() && stack.getTag().contains(KEY_STRENGTH) ? stack.getTag().getFloat(KEY_STRENGTH) : this.strength)).withStyle(TextFormatting.DARK_GREEN));
	}
}