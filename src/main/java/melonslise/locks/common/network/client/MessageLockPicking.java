package melonslise.locks.common.network.client;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import melonslise.locks.client.gui.GuiLockPicking;
import melonslise.locks.common.container.ContainerLockPicking;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.Lockable;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.LocksUtilities;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageLockPicking implements IMessage
{
	private int id;
	private BlockPos position;

	public MessageLockPicking() {}

	public MessageLockPicking(ContainerLockPicking container)
	{
		this.id = container.windowId;
		this.position = container.position;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.id = buffer.readInt();
		this.position = LocksUtilities.readPosition(buffer);
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeInt(this.id);
		LocksUtilities.writePosition(buffer, this.position);
	}



	public static class Handler implements IMessageHandler<MessageLockPicking, IMessage>
	{
		@Override
		public IMessage onMessage(MessageLockPicking message, MessageContext context)
		{
			Minecraft mc = Minecraft.getMinecraft();
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					ArrayList<Lockable> lockables = StorageLockables.get(mc.world).matching(new PredicateIntersecting(new Box(message.position)));
					if(lockables.isEmpty()) return;
					ContainerLockPicking container = new ContainerLockPicking(mc.player, message.position, lockables.get(0));
					container.windowId = message.id;
					mc.displayGuiScreen(new GuiLockPicking(container));
				}
			};
			mc.addScheduledTask(runnable);
			return null;
		}
	}
}