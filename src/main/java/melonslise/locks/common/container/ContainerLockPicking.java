package melonslise.locks.common.container;

import java.util.Iterator;

import io.netty.util.internal.ThreadLocalRandom;
import melonslise.locks.common.item.ItemLockPick;
import melonslise.locks.common.network.LocksNetworks;
import melonslise.locks.common.network.client.MessageCheckPinResult;
import melonslise.locks.common.sound.LocksSounds;
import melonslise.locks.common.world.storage.Lockable;
import melonslise.locks.common.world.storage.StorageLockables;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

// TODO Sync with client container instead of gui
// TODO Sound helper, random pitch?
public class ContainerLockPicking extends Container
{
	public final EntityPlayer player;
	public final BlockPos position;
	public final Lockable lockable;
	protected int currentIndex = 0;
	protected boolean open;

	public ContainerLockPicking(EntityPlayer player, BlockPos position, Lockable lockable)
	{
		this.player = player;
		this.position = position;
		this.lockable = lockable;
	}

	// TODO block exist
	// TODO Player inventory scan helper
	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		if(player.getDistanceSqToCenter(this.position) > 9D) return false;
		Iterator<ItemStack> iterator = player.inventoryContainer.getInventory().iterator();
		while(iterator.hasNext()) if(iterator.next().getItem() instanceof ItemLockPick) return true;
		return false;
	}

	public void checkPin(int currentPin)
	{
		if(this.open || this.player.world.isRemote) return;
		boolean correct = false, reset = false;
		if(this.lockable.lock.checkPin(currentIndex, currentPin))
		{
			++this.currentIndex;
			if(this.currentIndex == this.lockable.lock.getLength()) this.open = true;
			correct = true;
			this.player.world.playSound(null, this.position, LocksSounds.pin_match, SoundCategory.BLOCKS, 1F, 1F);
		}
		else
		{
			if(this.breakPick(player))
			{
				reset = true;
				this.reset();
			}
			this.player.world.playSound(null, this.position, LocksSounds.pin_fail, SoundCategory.BLOCKS, 1F, 1F);
		}
		LocksNetworks.network.sendTo(new MessageCheckPinResult(correct, reset), (EntityPlayerMP) this.player);
	}

	protected void reset()
	{
		this.currentIndex = 0;
		//if(LocksConfiguration.getMain(this.player.world).reset_combinations) StorageLockables.get(this.player.world).shuffleCombination(this.lockable);
	}

	protected boolean breakPick(EntityPlayer player)
	{
		for(ItemStack stack : player.inventoryContainer.getInventory())
		{
			if(!(stack.getItem() instanceof ItemLockPick)) continue;
			if(ThreadLocalRandom.current().nextFloat() <= ((ItemLockPick) stack.getItem()).getStrength(player.world)) return false;
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
		if(player.world.isRemote || !this.open || !this.lockable.lock.isLocked()) return;
		StorageLockables.get(player.world).toggle(this.lockable.box);
		this.player.world.playSound(null, this.position, LocksSounds.lock_open, SoundCategory.BLOCKS, 1F, 1F);
	}
}