package melonslise.locks.common.network;

import melonslise.locks.LocksCore;
import melonslise.locks.common.network.client.MessageAddLockable;
import melonslise.locks.common.network.client.MessageCheckPinResult;
import melonslise.locks.common.network.client.MessageConfiguration;
import melonslise.locks.common.network.client.MessageKeyRing;
import melonslise.locks.common.network.client.MessageLockPicking;
import melonslise.locks.common.network.client.MessageRemoveLockables;
import melonslise.locks.common.network.client.MessageSetLockables;
import melonslise.locks.common.network.client.MessageToggleLockables;
import melonslise.locks.common.network.client.MessageToggleLockablesByID;
import melonslise.locks.common.network.server.MessageCheckPin;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class LocksNetworks
{
	public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(LocksCore.ID);

	private LocksNetworks() {}

	public static void registerMessages()
	{
		int id = -1;
		network.registerMessage(MessageCheckPin.Handler.class, MessageCheckPin.class, ++id, Side.SERVER);

		network.registerMessage(MessageCheckPinResult.Handler.class, MessageCheckPinResult.class, ++id, Side.CLIENT);
		network.registerMessage(MessageLockPicking.Handler.class, MessageLockPicking.class, ++id, Side.CLIENT);
		network.registerMessage(MessageKeyRing.Handler.class, MessageKeyRing.class, ++id, Side.CLIENT);
		network.registerMessage(MessageSetLockables.Handler.class, MessageSetLockables.class, ++id, Side.CLIENT);
		network.registerMessage(MessageAddLockable.Handler.class, MessageAddLockable.class, ++id, Side.CLIENT);
		network.registerMessage(MessageRemoveLockables.Handler.class, MessageRemoveLockables.class, ++id, Side.CLIENT);
		network.registerMessage(MessageToggleLockables.Handler.class, MessageToggleLockables.class, ++id, Side.CLIENT);
		network.registerMessage(MessageToggleLockablesByID.Handler.class, MessageToggleLockablesByID.class, ++id, Side.CLIENT);
		network.registerMessage(MessageConfiguration.Handler.class, MessageConfiguration.class, ++id, Side.CLIENT);
	}
}