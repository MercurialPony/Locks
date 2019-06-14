package melonslise.locks.client.init;

import melonslise.locks.client.gui.ScreenKeyRing;
import melonslise.locks.client.gui.ScreenLockPicking;
import melonslise.locks.common.init.LocksContainerTypes;
import net.minecraft.client.gui.ScreenManager;

public final class LocksScreens
{
	private LocksScreens() {}

	public static void register()
	{
		ScreenManager.registerFactory(LocksContainerTypes.LOCK_PICKING, ScreenLockPicking::new);
		ScreenManager.registerFactory(LocksContainerTypes.KEY_RING, ScreenKeyRing::new);
	}
}