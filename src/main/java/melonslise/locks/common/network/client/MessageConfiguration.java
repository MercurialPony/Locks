package melonslise.locks.common.network.client;

import io.netty.buffer.ByteBuf;
import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.config.LocksConfiguration.Configuration;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageConfiguration implements IMessage
{
	private int lock_length, lockable_volume;
	private float lock_pick_strength;
	private String[] lockable_blocks;
	private boolean generate_locks, remove_locks, unbreakable_locks;

	public MessageConfiguration()
	{
	}

	public MessageConfiguration(Configuration config)
	{
		this.lock_length = config.lock_length;
		this.lock_pick_strength = config.lock_pick_strength;
		this.lockable_volume = config.lockable_volume;
		this.lockable_blocks = config.lockable_blocks;
		this.generate_locks = config.generate_locks;
		this.remove_locks = config.remove_locks;
		this.unbreakable_locks = config.unbreakable_locks;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.lock_length = (int) buffer.readByte();
		this.lock_pick_strength = (float) buffer.readByte() * 100F;
		this.lockable_volume = buffer.readInt();
		this.lockable_blocks = new String[(int) buffer.readShort()];
		for(int a = 0; a < this.lockable_blocks.length; ++a) this.lockable_blocks[a] = ByteBufUtils.readUTF8String(buffer);
		this.generate_locks = buffer.readBoolean();
		this.remove_locks = buffer.readBoolean();
		this.unbreakable_locks = buffer.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeByte((byte) this.lock_length);
		buffer.writeByte((byte) (this.lock_pick_strength * 100F));
		buffer.writeInt(this.lockable_volume);
		buffer.writeShort((short) this.lockable_blocks.length);
		for(String string : this.lockable_blocks) ByteBufUtils.writeUTF8String(buffer, string);
		buffer.writeBoolean(this.generate_locks);
		buffer.writeBoolean(this.remove_locks);
		buffer.writeBoolean(this.unbreakable_locks);
	}



	public static class Handler implements IMessageHandler<MessageConfiguration, IMessage>
	{
		@Override
		public IMessage onMessage(MessageConfiguration message, MessageContext context)
		{
			Minecraft mc = Minecraft.getMinecraft();
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					Configuration config = LocksConfiguration.getMain(mc.world);
					config.lock_length = message.lock_length;
					config.lock_pick_strength = message.lock_pick_strength;
					config.lockable_volume = message.lockable_volume;
					config.lockable_blocks = message.lockable_blocks;
					config.generate_locks = message.generate_locks;
					config.remove_locks = message.remove_locks;
					config.unbreakable_locks = message.unbreakable_locks;
				}
			};
			mc.addScheduledTask(runnable);
			return null;
		}
	}
}