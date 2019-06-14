package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.network.toClient.PacketCheckPinResult;
import melonslise.locks.common.network.toClient.PacketLockableAdd;
import melonslise.locks.common.network.toClient.PacketLockableRemove;
import melonslise.locks.common.network.toClient.PacketLockableStatus;
import melonslise.locks.common.network.toServer.PacketCheckPin;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class LocksNetworks
{
	public static final SimpleChannel MAIN = NetworkRegistry.newSimpleChannel(new ResourceLocation(Locks.ID, "main"), () -> "locks", a -> true, a -> true);

	private LocksNetworks() {}

	public static void register()
	{
		int id = -1;
		MAIN.registerMessage(++id, PacketLockableAdd.class, PacketLockableAdd::encode, PacketLockableAdd::decode, PacketLockableAdd::handle);
		MAIN.registerMessage(++id, PacketLockableRemove.class, PacketLockableRemove::encode, PacketLockableRemove::decode, PacketLockableRemove::handle);
		MAIN.registerMessage(++id, PacketLockableStatus.class, PacketLockableStatus::encode, PacketLockableStatus::decode, PacketLockableStatus::handle);
		MAIN.registerMessage(++id, PacketCheckPin.class, PacketCheckPin::encode, PacketCheckPin::decode, PacketCheckPin::handle);
		MAIN.registerMessage(++id, PacketCheckPinResult.class, PacketCheckPinResult::encode, PacketCheckPinResult::decode, PacketCheckPinResult::handle);
	}
}