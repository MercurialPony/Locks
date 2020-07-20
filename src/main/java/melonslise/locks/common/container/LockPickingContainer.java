package melonslise.locks.common.container;

import java.util.function.Consumer;

import melonslise.locks.Locks;
import melonslise.locks.client.gui.LockPickingScreen;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksContainerTypes;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.LockPickItem;
import melonslise.locks.common.network.toclient.CheckPinResultPacket;
import melonslise.locks.common.util.Lockable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.fml.network.PacketDistributor;

public class LockPickingContainer extends Container
{
	public static final ITextComponent TITLE = new TranslationTextComponent(Locks.ID + ".gui.lockpicking.title");

	public PlayerEntity player;
	public BlockPos pos;
	public Lockable lockable;
	protected int currentIndex = 0;

	public LockPickingContainer(int id, PlayerEntity player, BlockPos pos, Lockable lockable)
	{
		super(LocksContainerTypes.LOCK_PICKING, id);
		this.player = player;
		this.pos = pos;
		this.lockable = lockable;
	}

	public int getCurrentIndex()
	{
		return this.currentIndex;
	}

	// TODO don't hardcode item
	@Override
	public boolean canInteractWith(PlayerEntity player)
	{
		for(ItemStack stack : player.getHeldEquipment())
			if(stack.getItem() == LocksItems.LOCK_PICK)
				return true;
		return false;
	}

	// SERVER ONLY
	public void checkPin(int currentPin)
	{
		if(this.isOpen())
			return;
		boolean correct = false, reset = false;
		if(this.lockable.lock.checkPin(currentIndex, currentPin))
		{
			++this.currentIndex;
			correct = true;
			this.player.world.playSound(null, this.pos, LocksSoundEvents.PIN_MATCH, SoundCategory.BLOCKS, 1F, 1F);
		}
		else
		{
			if(!this.breakPick(player))
				this.player.world.playSound(null, this.pos, LocksSoundEvents.PIN_FAIL, SoundCategory.BLOCKS, 1F, 1F);
			else
			{
				reset = true;
				this.reset();
			}
		}
		LocksNetworks.MAIN.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.player), new CheckPinResultPacket(correct, reset));
	}

	@OnlyIn(Dist.CLIENT)
	public void handlePin(boolean correct, boolean reset)
	{
		Screen screen = Minecraft.getInstance().currentScreen;
		if(screen instanceof LockPickingScreen)
			((LockPickingScreen) screen).handlePin(correct, reset);
		if(correct)
			++this.currentIndex;
		if(reset)
			this.reset();
	}

	public boolean isOpen()
	{
		return this.currentIndex == this.lockable.lock.getLength();
	}

	protected void reset()
	{
		this.currentIndex = 0;
	}

	// TODO Dont hardcode item too
	protected boolean breakPick(PlayerEntity player)
	{
		for(EquipmentSlotType slot : EquipmentSlotType.values())
		{
			ItemStack stack = player.getItemStackFromSlot(slot);
			if(slot.getSlotType() != EquipmentSlotType.Group.HAND || stack.getItem() != LocksItems.LOCK_PICK)
				continue;
			if(player.world.rand.nextFloat() < LockPickItem.getOrSetStrength(stack))
				return false;
			this.player.sendBreakAnimation(slot);
			stack.shrink(1);
			return true;
		}
		return false;
	}

	@Override
	public void onContainerClosed(PlayerEntity player)
	{
		super.onContainerClosed(player);
		if(!this.isOpen() || !this.lockable.lock.isLocked())
			return;
		this.lockable.lock.setLocked(!this.lockable.lock.isLocked());
		this.player.world.playSound(player, this.pos, LocksSoundEvents.LOCK_OPEN, SoundCategory.BLOCKS, 1F, 1F);
	}



	public static final IContainerFactory<LockPickingContainer> FACTORY = (id, inv, buffer) ->
	{
		BlockPos pos = buffer.readBlockPos();
		Lockable lockable = inv.player.world.getCapability(LocksCapabilities.LOCKABLES).map(lockables -> lockables.get().get(buffer.readInt())).orElse(null);
		return new LockPickingContainer(id, inv.player, pos, lockable);
	};

	// TODO Move?
	public static class Provider implements INamedContainerProvider
	{
		public final BlockPos pos;
		public final Lockable lockable;

		public Provider(BlockPos pos, Lockable lockable)
		{
			this.pos = pos;
			this.lockable = lockable;
		}

		@Override
		public Container createMenu(int id, PlayerInventory inv, PlayerEntity player)
		{
			return new LockPickingContainer(id, player, this.pos, this.lockable);
		}

		@Override
		public ITextComponent getDisplayName()
		{
			return TITLE;
		}
	}

	public static class Writer implements Consumer<PacketBuffer>
	{
		public final BlockPos pos;
		public final Lockable lockable;

		public Writer(BlockPos pos, Lockable lockable)
		{
			this.pos = pos;
			this.lockable = lockable;
		}

		@Override
		public void accept(PacketBuffer buffer)
		{
			buffer.writeBlockPos(this.pos);
			buffer.writeInt(this.lockable.networkID);
		}
	}
}