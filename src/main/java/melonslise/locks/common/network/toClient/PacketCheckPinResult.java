package melonslise.locks.common.network.toClient;

import java.util.function.Supplier;

import melonslise.locks.common.container.ContainerLockPicking;
import melonslise.locks.common.init.LocksContainerTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketCheckPinResult
{
	private final boolean correct, reset;

	public PacketCheckPinResult(boolean correct, boolean reset)
	{
		this.correct = correct;
		this.reset = reset;
	}

	public static PacketCheckPinResult decode(PacketBuffer buffer)
	{
		return new PacketCheckPinResult(buffer.readBoolean(), buffer.readBoolean());
	}

	public static void encode(PacketCheckPinResult packet, PacketBuffer buffer)
	{
		buffer.writeBoolean(packet.correct);
		buffer.writeBoolean(packet.reset);
	}

	public static void handle(PacketCheckPinResult packet, Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() ->
		{
			Container container = Minecraft.getInstance().player.openContainer;
			if(container.getType() == LocksContainerTypes.LOCK_PICKING) ((ContainerLockPicking) container).handlePin(packet.correct, packet.reset);
		});
		context.get().setPacketHandled(true);
	}
}