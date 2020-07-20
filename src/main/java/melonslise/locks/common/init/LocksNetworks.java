package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.network.LocksGuiHandler;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.CheckPinResultPacket;
import melonslise.locks.common.network.toclient.ConfigSyncPacket;
import melonslise.locks.common.network.toclient.RemoveLockablePacket;
import melonslise.locks.common.network.toclient.UpdateLockablePacket;
import melonslise.locks.common.network.toserver.CheckPinPacket;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class LocksNetworks
{
	public static final SimpleNetworkWrapper MAIN = NetworkRegistry.INSTANCE.newSimpleChannel(Locks.ID);

	private LocksNetworks() {}

	public static void register()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(Locks.instance, new LocksGuiHandler());

		MAIN.registerMessage(AddLockablePacket.Handler.class, AddLockablePacket.class, 0, Side.CLIENT);
		MAIN.registerMessage(RemoveLockablePacket.Handler.class, RemoveLockablePacket.class, 1, Side.CLIENT);
		MAIN.registerMessage(UpdateLockablePacket.Handler.class, UpdateLockablePacket.class, 2, Side.CLIENT);
		MAIN.registerMessage(CheckPinPacket.Handler.class, CheckPinPacket.class, 3, Side.SERVER);
		MAIN.registerMessage(CheckPinResultPacket.Handler.class, CheckPinResultPacket.class, 4, Side.CLIENT);
		MAIN.registerMessage(ConfigSyncPacket.Handler.class, ConfigSyncPacket.class, 5, Side.CLIENT);
	}
}