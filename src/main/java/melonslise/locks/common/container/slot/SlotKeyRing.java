package melonslise.locks.common.container.slot;

import melonslise.locks.common.init.LocksSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

// TODO Remove this class and use container/inventory instead
public class SlotKeyRing extends SlotItemHandler
{
	public final PlayerEntity player;

	public SlotKeyRing(PlayerEntity player, IItemHandler itemHandler, int index, int xPosition, int yPosition)
	{
		super(itemHandler, index, xPosition, yPosition);
		this.player = player;
	}

	// TODO PITCH
	@Override
	public void putStack(ItemStack stack)
	{
		super.putStack(stack);
		if(!this.player.world.isRemote) this.player.world.playSound(null, this.player.posX, this.player.posY, this.player.posZ, LocksSounds.KEY_RING, SoundCategory.PLAYERS, 1f, 1f);
	}

	@Override
	public ItemStack onTake(PlayerEntity player, ItemStack stack)
	{
		if(!this.player.world.isRemote) this.player.world.playSound(null, this.player.posX, this.player.posY, this.player.posZ, LocksSounds.KEY_RING, SoundCategory.PLAYERS, 1f, 1f);
		return super.onTake(player, stack);
	}
}