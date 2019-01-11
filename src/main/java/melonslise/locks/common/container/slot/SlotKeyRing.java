package melonslise.locks.common.container.slot;

import melonslise.locks.common.sound.LocksSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotKeyRing extends SlotItemHandler
{
	public final EntityPlayer player;

	public SlotKeyRing(EntityPlayer player, IItemHandler inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
		this.player = player;
	}

	@Override
	public void putStack(ItemStack stack)
	{
		super.putStack(stack);
		this.player.playSound(LocksSounds.key_ring, 1F, 1F);
	}

	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack stack)
	{
		this.player.playSound(LocksSounds.key_ring, 1F, 1F);
		return super.onTake(player, stack);
	}
}