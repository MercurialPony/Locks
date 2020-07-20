package melonslise.locks.common.event;

import java.util.List;
import java.util.stream.Collectors;

import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksPredicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Locks.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LocksForgeEvents
{
	private LocksForgeEvents() {}

	@SubscribeEvent
	public static void attachCapabilitiesToWorld(AttachCapabilitiesEvent<World> event)
	{
		LocksCapabilities.attachToWorld(event);
	}

	@SubscribeEvent
	public static void attachCapabilitiesToEntity(AttachCapabilitiesEvent<Entity> event)
	{
		LocksCapabilities.attachToEntity(event);
	}

	public static void syncLockables(World world, DimensionType dimension)
	{
		world.getCapability(LocksCapabilities.LOCKABLES).ifPresent(lockables ->
		{
			for(Lockable lockable : lockables.get().values())
				LocksNetworks.MAIN.send(PacketDistributor.DIMENSION.with(() -> dimension), new AddLockablePacket(lockable));
		});
	}

	public static void syncLockables(World world)
	{
		syncLockables(world, world.dimension.getType());
	}

	@SubscribeEvent
	public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		syncLockables(event.getPlayer().world);
	}

	@SubscribeEvent
	public static void onPlayerChangeDim(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		syncLockables(event.getPlayer().world, event.getTo());
	}

	// TODO Sync only if died in another dim
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		syncLockables(event.getPlayer().world);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
			return;
		event.player.getCapability(LocksCapabilities.LOCK_SELECTION)
			.ifPresent(select ->
			{
				if(select.get() == null)
					return;
				for(ItemStack stack : event.player.getHeldEquipment())
					if(stack.getItem() instanceof LockItem)
						return;
				select.set(null);
			});
	}

	public static final ITextComponent LOCKED_MESSAGE = new TranslationTextComponent(Locks.ID + ".status.locked");

	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		BlockPos pos = event.getPos();
		World world = event.getWorld();
		PlayerEntity player = event.getPlayer();
		world.getCapability(LocksCapabilities.LOCKABLES)
			.ifPresent(lockables ->
			{
				List<Lockable> intersecting = lockables.get().values().stream().filter(lockable1 -> lockable1.box.intersects(pos)).collect(Collectors.toList());
				if(intersecting.isEmpty())
					return;
				if(event.getHand() != Hand.MAIN_HAND) // TODO Better way to prevent firing multiple times
				{
					event.setUseBlock(Result.DENY);
					return;
				}
				if(intersecting.stream().anyMatch(LocksPredicates.LOCKED))
				{
					intersecting.stream().filter(LocksPredicates.LOCKED).forEach(lockable -> lockable.shake(20));
					event.setUseBlock(Result.DENY);
					world.playSound(player, pos, LocksSoundEvents.LOCK_RATTLE, SoundCategory.BLOCKS, 1f, 1f); // TODO Play sound only if item returns fail/pass
					player.swingArm(Hand.MAIN_HAND);
					if(world.isRemote && LocksConfig.DEAF_MODE.get())
						player.sendStatusMessage(LOCKED_MESSAGE, true);
					return;
				}
				if(LocksServerConfig.ALLOW_REMOVING_LOCKS.get() && player.isSneaking() && event.getItemStack().isEmpty())
				{
					List<Lockable> matching = intersecting.stream().filter(LocksPredicates.NOT_LOCKED).collect(Collectors.toList());
					if(matching.isEmpty())
						return;
					event.setUseBlock(Result.DENY);
					world.playSound(player, pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 0.8f, 0.8f + world.rand.nextFloat() * 0.4f);
					player.swingArm(Hand.MAIN_HAND);
					if(!world.isRemote)
						for(Lockable lockable : matching)
						{
							world.addEntity(new ItemEntity(world, (double) pos.getX() + 0.5d, (double) pos.getY() + 0.5d, (double) pos.getZ() + 0.5d, LockItem.from(lockable.lock)));
							lockables.remove(lockable.networkID);
						}
				}
			});
	}

	public static boolean canBreakLockable(PlayerEntity player, BlockPos pos)
	{
		return player.world.getCapability(LocksCapabilities.LOCKABLES)
			.map(lockables -> !LocksServerConfig.PROTECT_LOCKABLES.get() || player.isCreative() || !lockables.get().values().stream().anyMatch(lockable1 -> lockable1.lock.isLocked() && lockable1.box.intersects(pos)))
			.orElse(true);
	}

	@SubscribeEvent
	public static void onBlockBreaking(PlayerEvent.BreakSpeed event)
	{
		if(!canBreakLockable(event.getPlayer(), event.getPos()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if(!canBreakLockable(event.getPlayer(), event.getPos()))
			event.setCanceled(true);
	}
}