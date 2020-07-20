package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.CheckPinResultPacket;
import melonslise.locks.common.network.toclient.RemoveLockablePacket;
import melonslise.locks.common.network.toclient.UpdateLockablePacket;
import melonslise.locks.common.network.toserver.CheckPinPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class LocksNetworks
{
	public static final SimpleChannel MAIN = NetworkRegistry.newSimpleChannel(new ResourceLocation(Locks.ID, "main"), () -> "locks", a -> true, a -> true);

	private LocksNetworks() {}

	public static void register()
	{
		MAIN.registerMessage(0, AddLockablePacket.class, AddLockablePacket::encode, AddLockablePacket::decode, AddLockablePacket::handle);
		MAIN.registerMessage(1, RemoveLockablePacket.class, RemoveLockablePacket::encode, RemoveLockablePacket::decode, RemoveLockablePacket::handle);
		MAIN.registerMessage(2, UpdateLockablePacket.class, UpdateLockablePacket::encode, UpdateLockablePacket::decode, UpdateLockablePacket::handle);
		MAIN.registerMessage(3, CheckPinPacket.class, CheckPinPacket::encode, CheckPinPacket::decode, CheckPinPacket::handle);
		MAIN.registerMessage(4, CheckPinResultPacket.class, CheckPinResultPacket::encode, CheckPinResultPacket::decode, CheckPinResultPacket::handle);
	}
}