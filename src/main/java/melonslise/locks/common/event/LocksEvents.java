package melonslise.locks.common.event;

import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.capability.ISelection;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.ConfigSyncPacket;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.util.Orientation;
import melonslise.locks.common.world.LocksWorldEventListener;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@Mod.EventBusSubscriber(modid = Locks.ID)
public final class LocksEvents
{
	private LocksEvents() {}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		LocksItems.register(event);
	}

	@SubscribeEvent
	public static void registerSoundEvents(RegistryEvent.Register<SoundEvent> event)
	{
		LocksSoundEvents.register(event);
	}

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

	@SubscribeEvent
	public static void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(!event.getModID().equals(Locks.ID))
			return;
		ConfigManager.sync(Locks.ID, Config.Type.INSTANCE);
		LocksConfig.SERVER.init();
		LocksConfig.getServerClient().init();
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server == null)
			return;
		server.addScheduledTask(() -> LocksNetworks.MAIN.sendToAll(new ConfigSyncPacket(LocksConfig.SERVER)));
	}

	public static final LocksWorldEventListener LISTENER = new LocksWorldEventListener();

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event)
	{
		event.getWorld().addEventListener(LISTENER);
	}

	@SubscribeEvent
	public static void onChunkPopulate(PopulateChunkEvent.Post event)
	{
		World world = event.getWorld();
		Random rand = event.getRand();
		double ch = LocksConfig.COMMON.generationChance;
		if(ch == 0d || rand.nextDouble() > ch)
			return;
		ILockableStorage lockables = world.getCapability(LocksCapabilities.LOCKABLES, null);
		for(Entry<BlockPos, TileEntity> entry : world.getChunkFromChunkCoords(event.getChunkX(), event.getChunkZ()).getTileEntityMap().entrySet())
		{
			BlockPos pos = entry.getKey();
			if(!(entry.getValue() instanceof TileEntityChest) || lockables.get().values().stream().anyMatch(lockable1 -> lockable1.box.intersects(pos)))
				continue;
			BlockPos adjPos = LocksUtil.getAdjacentChest((TileEntityChest) entry.getValue());
			lockables.add(new Lockable(new Cuboid6i(pos, adjPos == null ? pos : adjPos), new Lock(rand.nextInt(), LocksConfig.COMMON.randLockLen(rand), true), Orientation.fromDirection(world.getBlockState(pos).getValue(BlockChest.FACING), EnumFacing.NORTH)));
		}
	}

	public static void syncLockables(World world, int dimId)
	{
		world.getCapability(LocksCapabilities.LOCKABLES, null).get().values().forEach(lockable -> LocksNetworks.MAIN.sendToDimension(new AddLockablePacket(lockable), dimId));
	}

	public static void syncLockables(World world)
	{
		syncLockables(world, world.provider.getDimension());
	}

	@SubscribeEvent
	public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		syncLockables(event.player.world);
		LocksNetworks.MAIN.sendTo(new ConfigSyncPacket(LocksConfig.SERVER), (EntityPlayerMP) event.player);
	}

	@SubscribeEvent
	public static void onPlayerChangeDim(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		syncLockables(event.player.world, event.toDim);
	}

	// TODO Sync only if died in another dim
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		syncLockables(event.player.world);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
			return;
		ISelection select = event.player.getCapability(LocksCapabilities.LOCK_SELECTION, null);
		if(select.get() == null)
			return;
		for(ItemStack stack : event.player.getHeldEquipment())
			if(stack.getItem() instanceof LockItem)
				return;
		select.set(null);
	}

	public static final ITextComponent LOCKED_MESSAGE = new TextComponentTranslation(Locks.ID + ".status.locked");

	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		BlockPos pos = event.getPos();
		World world = event.getWorld();
		EntityPlayer player = event.getEntityPlayer();
		ILockableStorage lockables = world.getCapability(LocksCapabilities.LOCKABLES, null);
		List<Lockable> intersecting = lockables.get().values().stream().filter(lockable1 -> lockable1.box.intersects(pos)).collect(Collectors.toList());
		if(intersecting.isEmpty())
			return;
		if(event.getHand() != EnumHand.MAIN_HAND) // TODO Better way to prevent firing multiple times
		{
			event.setUseBlock(Result.DENY);
			return;
		}
		if(intersecting.stream().anyMatch(LocksPredicates.LOCKED))
		{
			intersecting.stream().filter(LocksPredicates.LOCKED).forEach(lockable -> lockable.shake(20));
			event.setUseBlock(Result.DENY);
			world.playSound(null, pos, LocksSoundEvents.LOCK_RATTLE, SoundCategory.BLOCKS, 1f, 1f); // TODO Play sound only if item returns fail/pass
			player.swingArm(EnumHand.MAIN_HAND);
			if(world.isRemote && LocksConfig.CLIENT.deafMode)
				player.sendStatusMessage(LOCKED_MESSAGE, true);
			return;
		}
		if(LocksConfig.getServer(world).allowRemovingLocks && player.isSneaking() && event.getItemStack().isEmpty())
		{
			List<Lockable> matching = intersecting.stream().filter(LocksPredicates.NOT_LOCKED).collect(Collectors.toList());
			if(matching.isEmpty())
				return;
			event.setUseBlock(Result.DENY);
			world.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 0.8f, 0.8f + world.rand.nextFloat() * 0.4f);
			player.swingArm(EnumHand.MAIN_HAND);
			if(world.isRemote)
				return;
			for(Lockable lockable : matching)
			{
				world.spawnEntity(new EntityItem(world, (double) pos.getX() + 0.5d, (double) pos.getY() + 0.5d, (double) pos.getZ() + 0.5d, LockItem.from(lockable.lock)));
				lockables.remove(lockable.networkID);
			}
		}
	}

	public static boolean canBreakLockable(EntityPlayer player, BlockPos pos)
	{
		ILockableStorage lockables = player.world.getCapability(LocksCapabilities.LOCKABLES, null);
		return !LocksConfig.getServer(player.world).protectLockables || player.isCreative() || !lockables.get().values().stream().anyMatch(lockable1 -> lockable1.lock.isLocked() && lockable1.box.intersects(pos));
	}

	@SubscribeEvent
	public static void onBlockBreaking(BreakSpeed event)
	{
		if(!canBreakLockable(event.getEntityPlayer(), event.getPos()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if(!canBreakLockable(event.getPlayer(), event.getPos()))
			event.setCanceled(true);
	}
}