package melonslise.locks.common.network.toclient;

import java.util.function.Supplier;

import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksContainerTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class CheckPinResultPacket
{
	private final boolean correct, reset;

	public CheckPinResultPacket(boolean correct, boolean reset)
	{
		this.correct = correct;
		this.reset = reset;
	}

	public static CheckPinResultPacket decode(PacketBuffer buf)
	{
		return new CheckPinResultPacket(buf.readBoolean(), buf.readBoolean());
	}

	public static void encode(CheckPinResultPacket pkt, PacketBuffer buf)
	{
		buf.writeBoolean(pkt.correct);
		buf.writeBoolean(pkt.reset);
	}

	public static void handle(CheckPinResultPacket pkt, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			Container container = Minecraft.getInstance().player.openContainer;
			if(container.getType() == LocksContainerTypes.LOCK_PICKING)
				((LockPickingContainer) container).handlePin(pkt.correct, pkt.reset);
		});
		ctx.get().setPacketHandled(true);
	}
}