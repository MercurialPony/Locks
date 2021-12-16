package melonslise.locks.common.event;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.capability.ISelection;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksEnchantments;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.KeyItem;
import melonslise.locks.common.item.KeyRingItem;
import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.item.LockPickItem;
import melonslise.locks.common.item.LockingItem;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.ConfigSyncPacket;
import melonslise.locks.common.network.toclient.RemoveLockablePacket;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.world.LocksWorldEventListener;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
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
	public static void registerEnchantments(RegistryEvent.Register<Enchantment> event)
	{
		LocksEnchantments.register(event);
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
	public static void attachCapabilitiesToChunk(AttachCapabilitiesEvent<Chunk> event)
	{
		LocksCapabilities.attachToChunk(event);
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
		//The chunk already exists and is loaded
		//Grab it from the provider
		Chunk chunk = event.getWorld().getChunkProvider().getLoadedChunk(event.getChunkX(), event.getChunkZ());
		
		if(chunk == null)
		{
			//DebugUL.messageAll("Chunk was null at "+event.getChunkX()+" "+event.getChunkZ());
			return;
		}
		
		chunk.getCapability(LocksCapabilities.LOCKABLE_WORLDGEN_HANDLER, null).setChunkShouldGenerateChests();
		
		
		/*
		World world = event.getWorld();
		Random rand = event.getRand();
		double ch = LocksConfig.COMMON.generationChance;
		if(ch == 0d || rand.nextDouble() > ch)
			return;
		ILockableHandler lockables = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
		for(Entry<BlockPos, TileEntity> entry : world.getChunkFromChunkCoords(event.getChunkX(), event.getChunkZ()).getTileEntityMap().entrySet())
		{
			BlockPos pos = entry.getKey();
			if(!(entry.getValue() instanceof TileEntityChest) || lockables.getInChunk(pos).values().stream().anyMatch(lockable1 -> lockable1.box.intersects(pos)))
				continue;
			BlockPos adjPos = LocksUtil.getAdjacentChest((TileEntityChest) entry.getValue());
			ItemStack stack = LocksConfig.getRandomLock(rand);
			lockables.add(new Lockable(
					new Cuboid6i(pos, adjPos == null ? pos : adjPos), 
					new Lock(rand.nextInt(), LocksConfig.COMMON.randLockLen(rand), true), 
					Orientation.fromDirection(world.getBlockState(pos).getValue(BlockChest.FACING), EnumFacing.NORTH),
					stack)
					);
		}
		*/
	}

	/*
	public static void syncLockables(World world, int dimId)
	{
		world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null).getLoaded().values().forEach(lockable -> LocksNetworks.MAIN.sendToDimension(new AddLockablePacket(lockable), dimId));
	}

	public static void syncLockables(World world)
	{
		syncLockables(world, world.provider.getDimension());
	}
	*/

	@SubscribeEvent
	public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		//syncLockables(event.player.world);
		LocksNetworks.MAIN.sendTo(new ConfigSyncPacket(LocksConfig.SERVER), (EntityPlayerMP) event.player);
	}

	@SubscribeEvent
	public static void onPlayerChangeDim(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		//syncLockables(event.player.world, event.toDim);
	}

	// TODO Sync only if died in another dim
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		//syncLockables(event.player.world);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
			return;
		ISelection select = event.player.getCapability(LocksCapabilities.SELECTION, null);
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
		ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
		List<Lockable> intersecting = handler.getInChunk(pos).values().stream().filter(lockable1 -> lockable1.box.intersects(pos)).collect(Collectors.toList());
		if(intersecting.isEmpty())
			return;
		if(event.getHand() != EnumHand.MAIN_HAND) // TODO Better way to prevent firing multiple times
		{
			event.setUseBlock(Result.DENY);
			return;
		}
		Optional<Lockable> locked = intersecting.stream().filter(LocksPredicates.LOCKED).findFirst();
		if(locked.isPresent())
		{
			Lockable lkb = locked.get();
			event.setUseBlock(Result.DENY);
			ItemStack stack = event.getItemStack();
			Item item = stack.getItem();
			//TODO remove hardcoding and such
			//AKA use oredict or something
			if(!(item instanceof LockPickItem)
				&& item != LocksItems.MASTER_KEY
				&& (!(item instanceof KeyItem) || LockingItem.getOrSetId(stack) != lkb.lock.id)
				&& (item != LocksItems.KEY_RING || !KeyRingItem.containsId(stack, lkb.lock.id))
			)
			{
				lkb.shake(20);
				world.playSound(null, pos, LocksSoundEvents.LOCK_RATTLE, SoundCategory.BLOCKS, 1f, 1f);
			
				if(world.isRemote && LocksConfig.CLIENT.deafMode)
					player.sendStatusMessage(LOCKED_MESSAGE, true);
			}
			
			player.swingArm(EnumHand.MAIN_HAND);
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
				world.spawnEntity(new EntityItem(world, (double) pos.getX() + 0.5d, (double) pos.getY() + 0.5d, (double) pos.getZ() + 0.5d, lockable.stack));
				handler.remove(lockable.networkID);
			}
		}
	}

	public static boolean canBreakLockable(EntityPlayer player, BlockPos pos)
	{
		return !LocksConfig.getServer(player.world).protectLockables || player.isCreative() || !LocksUtil.locked(player.world, pos);
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
	
	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch event)
	{
		//Try chest population (non-delayed version)
		//event.getChunkInstance().getCapability(LocksCapabilities.LOCKABLE_WORLDGEN_HANDLER, null).tryGeneratingLocks();
		
		EntityPlayerMP player = event.getPlayer();
		event.getChunkInstance().getCapability(LocksCapabilities.LOCKABLE_STORAGE, null).get().values().stream().forEach(lkb -> 
		{
			LocksNetworks.MAIN.sendTo(new AddLockablePacket(lkb), player);
			//DebugUL.messageAll("Sending add packet for watch event at "+lkb.box.center());
		});
	}
	
	@SubscribeEvent
	public static void onChunkUnWatch(ChunkWatchEvent.UnWatch event)
	{
		EntityPlayerMP player = event.getPlayer();
		event.getChunkInstance().getCapability(LocksCapabilities.LOCKABLE_STORAGE, null).get().keySet().stream().forEach(id -> 
		{
			LocksNetworks.MAIN.sendTo(new RemoveLockablePacket(id), player);
			//DebugUL.messageAll("Sending remove packet for watch event");
		});
	}
	
	/*
	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event)
	{
		//FIXME handler is forgetting loaded locks, and this could be the culprit
		
		Chunk ch = (Chunk) event.getChunk();
		ILockableHandler handler = ch.getWorld().getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
		ch.getCapability(LocksCapabilities.LOCKABLE_STORAGE, null).get().values().forEach(lkb ->
		{
			handler.getLoaded().remove(lkb.networkID);
			lkb.deleteObserver(handler); //Redundant?
			if(Locks.debug)
				Locks.logger.debug("Removing lockable from loaded with id: "+lkb.networkID+" ::: "+lkb.toString());
		});
	}
	*/

	//ChunkEvent.Unload is unreliable?
	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event)
	{
		//FIXME handler is forgetting loaded locks, and this could be the culprit
		
		Chunk ch = (Chunk) event.getChunk();
		World world = ch.getWorld();
		
		if(world.isRemote)
			return;
		
		ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
		ch.getCapability(LocksCapabilities.LOCKABLE_STORAGE, null).get().values().forEach(lkb ->
		{
			ChunkPos unloadPos = ch.getPos();
			List<ChunkPos> chunkList = lkb.box.containedChunkPosList();
			for(ChunkPos chunkPos : chunkList)
			{
				//Skip this chunk as it's unloading
				if(unloadPos.equals(chunkPos))
					continue;
				
				//Don't unload if any of the chunks are loaded
				if(LocksUtil.hasChunk(world, chunkPos.x, chunkPos.z))
					continue;
			}
			handler.getLoaded().remove(lkb.networkID);
			lkb.deleteObserver(handler); //Redundant?
			if(Locks.debug)
				Locks.logger.debug("Removing lockable from loaded with id: "+lkb.networkID+" ::: "+lkb.toString());
		});
	}
	
	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load event)
	{
		Chunk ch = (Chunk) event.getChunk();
		World world = ch.getWorld();
		
		if(world.isRemote)
			return;
		
		ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
		Int2ObjectMap<Lockable> loadedLockables = handler.getLoaded();
		ch.getCapability(LocksCapabilities.LOCKABLE_STORAGE, null).get().values().forEach(lkb ->
		{
			if(!loadedLockables.containsKey(lkb.networkID))
			{
				lkb.addObserver(handler);
				loadedLockables.put(lkb.networkID, lkb);
				if(Locks.debug)
					Locks.logger.debug("Placing lockable into loaded with id: "+lkb.networkID+" ::: "+lkb.toString());
			}
		});
	}

	/*
	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event)
	{
		//FIXME handler is forgetting loaded locks, and this could be the culprit
		
		Chunk ch = (Chunk) event.getChunk();
		World world = ch.getWorld();
		
		//Client relies on chunk unwatch and communication with the server
		if(world.isRemote)
			return;
		
		ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
		
		//Iterate through each lockable of the specified chunk
		ch.getCapability(LocksCapabilities.LOCKABLE_STORAGE, null).get().values().forEach(lkb ->
		{
			//Schedule a lockable checking task for later
			//Unload event is unreliable, so we have to do this ourselves later, which is really fun...
			world.getMinecraftServer().addScheduledTask(new Runnable()
			{
				@Override
				public void run()
				{
					ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
					if(handler == null)
						return;
					
					//Add new lock to regularly check for unloading
					handler.getServerUnloadSet().add(lkb.networkID);
				}
			});
		});
		
		//Also schedule a task to check all locks in the unload set
		world.getMinecraftServer().addScheduledTask(new Runnable()
		{
			@Override
			public void run()
			{
				ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
				
				if(handler == null)
					return;
				
				Int2ObjectMap<Lockable> loadedLockables = handler.getLoaded();
				
				Iterator<Integer> it = handler.getServerUnloadSet().iterator();
				while(it.hasNext())
				{
					Integer networkId = it.next();
					Lockable lkb = loadedLockables.get(networkId);
					
					//Check if id is missing already for some reason
					if(lkb == loadedLockables.defaultReturnValue())
					{
						//Lock is already missing, remove network id from iteration
						it.remove();
						continue;
					}
					
					List<ChunkPos> chunkList = lkb.box.containedChunkPosList();
					//Go through every chunk the lockable is supposed to be in, and make sure all the chunks are already unloaded
					if(Locks.debug)
						Locks.logger.debug("ChunkList Size: "+chunkList.size());
					
					for(ChunkPos chunkPos : chunkList)
					{
						//Don't unload if any of the chunks are loaded
						if(LocksUtil.hasChunk(world, chunkPos.x, chunkPos.z))
							continue;
					}
					
					//No more chunks are loaded, for real this time. Remove the lock.
					loadedLockables.remove(lkb.networkID);
					lkb.deleteObserver(handler); //Redundant?
					if(Locks.debug)
						Locks.logger.debug("Removing lockable from loaded with id: "+lkb.networkID+" ::: "+lkb.toString());
					
					//Now that the lock is removed, remove the network id from iteration
					it.remove();
				}
			}
		});
	}
	*/
}