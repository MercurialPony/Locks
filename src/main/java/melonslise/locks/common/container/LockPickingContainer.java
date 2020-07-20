package melonslise.locks.common.container;

import org.apache.commons.lang3.tuple.Pair;

import melonslise.locks.Locks;
import melonslise.locks.client.gui.LockPickingGui;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.LockPickItem;
import melonslise.locks.common.network.toclient.CheckPinResultPacket;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.Orientation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
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
	public Vec3d pos;
	public Lockable lockable;

	protected int currentIndex = 0;

	public LockPickingContainer(EntityPlayer player, Lockable lockable)
	{
		this.player = player;
		this.lockable = lockable;
		Pair<Vec3d, Orientation> state = lockable.getLockState(player.world);
		if(state == null)
			this.pos = lockable.box.center();
		else
			this.pos = lockable.getLockState(player.world).getLeft();
	}

	public int getCurrentIndex()
	{
		return this.currentIndex;
	}

	// TODO don't hardcode item
	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		for(ItemStack stack : player.getHeldEquipment())
			if(stack.getItem() == LocksItems.LOCK_PICK)
				return true;
		return false;
	}

	// SERVER ONLY
	public void checkPin(int currentPin)
	{
		if(this.isOpen())
			return;
		boolean correct = false, reset = false;
		if(this.lockable.lock.checkPin(currentIndex, currentPin))
		{
			++this.currentIndex;
			correct = true;
			this.player.world.playSound(null, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.PIN_MATCH, SoundCategory.BLOCKS, 1F, 1F);
		}
		else
		{
			if(!this.breakPick(player))
				this.player.world.playSound(null, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.PIN_FAIL, SoundCategory.BLOCKS, 1F, 1F);
			else
			{
				reset = true;
				this.reset();
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
			this.reset();
	}

	public boolean isOpen()
	{
		return this.currentIndex == this.lockable.lock.getLength();
	}

	protected void reset()
	{
		this.currentIndex = 0;
	}

	// TODO Dont hardcode item too
	protected boolean breakPick(EntityPlayer player)
	{
		for(ItemStack stack : player.getHeldEquipment())
		{
			if(stack.getItem() != LocksItems.LOCK_PICK)
				continue;
			if(player.world.rand.nextFloat() < LockPickItem.getOrSetStrength(stack))
				return false;
			this.player.renderBrokenItemStack(stack);
			stack.shrink(1);
			return true;
		}
		return false;
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