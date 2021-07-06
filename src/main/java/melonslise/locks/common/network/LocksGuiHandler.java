package melonslise.locks.common.network;

import melonslise.locks.client.gui.KeyRingGui;
import melonslise.locks.client.gui.LockPickingGui;
import melonslise.locks.common.container.KeyRingContainer;
import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class LocksGuiHandler implements IGuiHandler
{
	public static final int
		KEY_RING_ID = 0,
		LOCK_PICKING_ID = 1;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		switch(id)
		{
		case KEY_RING_ID:
			return new KeyRingContainer(player, player.getHeldItem(x == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));
		case LOCK_PICKING_ID:
			return new LockPickingContainer(player, y == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND, world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null).getLoaded().get(x));
		default:
			return null;
		}
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		switch(id)
		{
		case KEY_RING_ID:
			return new KeyRingGui((KeyRingContainer) this.getServerGuiElement(id, player, world, x, y, z));
		case LOCK_PICKING_ID:
			return new LockPickingGui((LockPickingContainer) this.getServerGuiElement(id, player, world, x, y, z));
		default:
			return null;
		}
	}
}