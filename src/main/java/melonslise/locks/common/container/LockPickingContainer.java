package melonslise.locks.common.container;

import melonslise.locks.Locks;
import melonslise.locks.client.gui.LockPickingGui;
import melonslise.locks.common.init.LocksDamageSources;
import melonslise.locks.common.init.LocksEnchantments;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.LockPickItem;
import melonslise.locks.common.network.toclient.CheckPinResultPacket;
import melonslise.locks.common.util.Lockable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LockPickingContainer extends Container
{
	public static final ITextComponent TITLE = new TextComponentTranslation(Locks.ID + ".gui.lockpicking.title");

	public EntityPlayer player;
	public EnumHand hand;
	public Vec3d pos;
	public Lockable lockable;

	protected int currentIndex = 0;
	
	public int shocking, sturdy, complexity;
	
	public LockPickingContainer(EntityPlayer player, EnumHand hand, Lockable lockable)
	{
		this.player = player;
		this.hand = hand;
		this.lockable = lockable;
		Lockable.State state = lockable.getLockState(player.world);
		if(state == null)
			this.pos = lockable.box.center();
		else
			this.pos = state.pos;
		
		this.shocking = EnchantmentHelper.getEnchantmentLevel(LocksEnchantments.SHOCKING, this.lockable.stack);
		this.sturdy = EnchantmentHelper.getEnchantmentLevel(LocksEnchantments.STURDY, this.lockable.stack);
		this.complexity = EnchantmentHelper.getEnchantmentLevel(LocksEnchantments.COMPLEXITY, this.lockable.stack);
	}

	public int getCurrentIndex()
	{
		return this.currentIndex;
	}

	// TODO don't hardcode item
	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return this.lockable.lock.isLocked() && this.isValidPick(player.getHeldItem(this.hand));
	}
	
	protected float getBreakChanceMultiplier(int pin)
	{
		return Math.abs(this.lockable.lock.getPin(this.currentIndex) - pin) == 1 ? 0.33f : 1f;
	}

	// SERVER ONLY
	public void checkPin(int currentPin)
	{
		if(this.isOpen())
			return;
		boolean correct = false;
		boolean reset = false;
		if(this.lockable.lock.checkPin(currentIndex, currentPin))
		{
			++this.currentIndex;
			correct = true;
			this.player.world.playSound(null, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.PIN_MATCH, SoundCategory.BLOCKS, 1F, 1F);
		}
		else
		{
			if(!this.tryBreakPick(player, currentPin))
			{
				int pinDistance = Math.abs(this.lockable.lock.getPin(this.currentIndex) - currentPin);
				this.player.world.playSound(null, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.PIN_FAIL, SoundCategory.BLOCKS, 1F, pinDistance <= 1 ? 1.25F : 1F);
			}
			else
			{
				reset = true;
				this.reset();
				if(this.shocking > 0)
				{
					this.player.attackEntityFrom(LocksDamageSources.SHOCK, shocking * 1.5f);
					this.player.world.playSound(null, this.player.posX, this.player.posY, this.player.posZ, LocksSoundEvents.SHOCK, SoundCategory.BLOCKS, 1f, 1f);
				}
			}
		}
		LocksNetworks.MAIN.sendTo(new CheckPinResultPacket(correct, reset), (EntityPlayerMP) this.player);
	}

	@SideOnly(Side.CLIENT)
	public void handlePin(boolean correct, boolean reset)
	{
		Gui screen = Minecraft.getMinecraft().currentScreen;
		if(screen instanceof LockPickingGui)
			((LockPickingGui) screen).handlePin(correct, reset);
		if(correct)
			++this.currentIndex;
		if(reset)
		{
			this.reset();
			player.getHeldItem(this.hand).shrink(1); //Shrink on client
		}
	}

	public boolean isOpen()
	{
		return this.currentIndex == this.lockable.lock.getLength();
	}

	protected void reset()
	{
		this.currentIndex = 0;
	}
	
	public boolean isValidPick(ItemStack stack)
	{
		return stack.getItem() instanceof LockPickItem && LockPickItem.canPick(stack, this.complexity);
	}

	// TODO Dont hardcode item too
	protected boolean tryBreakPick(EntityPlayer player, int pin)
	{
		ItemStack pickStack = player.getHeldItem(this.hand);
		if(!isValidPick(pickStack))
			return false;
		
		float sturdyModifier = this.sturdy == 0 ? 1f : 0.75f + this.sturdy * 0.5f;
		float ch = LockPickItem.getStrength(pickStack) / sturdyModifier;
		float ex = (1f - ch) * (1f - this.getBreakChanceMultiplier(pin));
		
		if(player.world.rand.nextFloat() < ex + ch)
			return false;
		
		this.player.renderBrokenItemStack(pickStack);
		pickStack.shrink(1);
		return true;
	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed(player);
		if(!this.isOpen() || !this.lockable.lock.isLocked())
			return;
		this.lockable.lock.setLocked(!this.lockable.lock.isLocked());
		this.player.world.playSound(player, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.LOCK_OPEN, SoundCategory.BLOCKS, 1F, 1F);
	}
}