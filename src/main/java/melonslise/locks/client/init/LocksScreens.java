package melonslise.locks.client.init;

import melonslise.locks.client.gui.KeyRingScreen;
import melonslise.locks.client.gui.LockPickingScreen;
import melonslise.locks.common.init.LocksContainerTypes;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LocksScreens
{
	private LocksScreens() {}

	public static void register()
	{
		ScreenManager.register(LocksContainerTypes.LOCK_PICKING.get(), LockPickingScreen::new);
		ScreenManager.register(LocksContainerTypes.KEY_RING.get(), KeyRingScreen::new);
	}
}