package melonslise.locks.common.event;

import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.base.Predicates;

import io.netty.util.internal.ThreadLocalRandom;
import melonslise.locks.LocksCore;
import melonslise.locks.common.capability.LocksCapabilities;
import melonslise.locks.common.capability.entity.ICapabilityLockBounds;
import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.item.ItemLock;
import melonslise.locks.common.item.LocksItems;
import melonslise.locks.common.item.api.lockable.ItemLockable;
import melonslise.locks.common.network.LocksNetworks;
import melonslise.locks.common.network.client.MessageConfiguration;
import melonslise.locks.common.sound.LocksSounds;
import melonslise.locks.common.world.ListenerLockables;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.Lock;
import melonslise.locks.common.world.storage.Lockable;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.LocksUtilities;
import melonslise.locks.utility.predicate.LocksSelectors;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

@Mod.EventBusSubscriber(modid = LocksCore.ID)
public class LocksEvents
{
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		LocksItems.register(event);
	}

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		LocksSounds.register(event);
	}

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent event)
	{
		LocksCapabilities.attach(event);
	}

	@SubscribeEvent
	public static void onPlayerConnect(PlayerLoggedInEvent event)
	{
		StorageLockables.get(event.player.world).synchronize();
		LocksConfiguration.synchronize((EntityPlayerMP) event.player);
	}

	@SubscribeEvent
	public static void onPlayerChangeDimension(PlayerChangedDimensionEvent event)
	{
		StorageLockables.get(event.player.world).synchronize();
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerRespawnEvent event)
	{
		StorageLockables.get(event.player.world).synchronize();
	}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event)
	{
		event.getWorld().addEventListener(new ListenerLockables());
	}

	@SubscribeEvent
	public static void onChunkPopulate(PopulateChunkEvent.Post event)
	{
		World world = event.getWorld();
		if(!LocksConfiguration.getMain(world).generate_locks) return;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for(Entry<BlockPos, TileEntity> entry : world.getChunkFromChunkCoords(event.getChunkX(), event.getChunkZ()).getTileEntityMap().entrySet())
		{
			if(!(entry.getValue() instanceof TileEntityChest)) continue;
			BlockPos position1 = entry.getKey();
			BlockPos position2 = LocksUtilities.getAdjacentChest((TileEntityChest) entry.getValue());
			StorageLockables.get(world).add(new Lockable(new Box(position1, position2 == null ? position1 : position2), new Lock(UUID.randomUUID(), random.nextInt(5, 9), true), world.getBlockState(position1).getValue(BlockChest.FACING)));
		}
	}

	// TODO Hoppers and stuff too
	// TODO also increase blast and break resistance
	// TODO Deny item too?
	// TODO Helper for item drop
	// TODO Fix removing (potentially) overlapping locked and unlocked lockables
	@SubscribeEvent
	public static void onPlayerInteract(RightClickBlock event)
	{
		if(event.getHand() != EnumHand.MAIN_HAND) // TODO Better way to prevent firing multiple times
		{
			event.setUseBlock(Result.DENY);
			return;
		}
		EntityPlayer player = event.getEntityPlayer();
		World world = event.getWorld();
		StorageLockables lockables = StorageLockables.get(world);
		BlockPos position = event.getPos();
		Box box = new Box(position);
		if(lockables.contains(Predicates.and(new PredicateIntersecting(box), LocksSelectors.LOCKED)))
		{
			event.setUseBlock(Result.DENY);
			world.playSound(null, position, LocksSounds.lock_rattle, SoundCategory.BLOCKS, 1F, 1F); // TODO Play sound only if item returns fail/pass
			player.swingArm(EnumHand.MAIN_HAND);
			return;
		}
		if(!LocksConfiguration.getMain(world).remove_locks || !player.isSneaking() || !event.getItemStack().isEmpty() || !lockables.contains(Predicates.and(new PredicateIntersecting(box), LocksSelectors.NOT_LOCKED))) return;
		event.setUseBlock(Result.DENY);
		player.playSound(SoundEvents.ENTITY_ITEM_BREAK, 0.8F, 0.8F + ThreadLocalRandom.current().nextFloat() * 0.4F);
		player.swingArm(EnumHand.MAIN_HAND);
		if(world.isRemote) return;
		for(Lockable lockable : lockables.remove(box)) world.spawnEntity(new EntityItem(world, (double) position.getX() + 0.5D, (double) position.getY() + 0.5D, (double) position.getZ() + 0.5D, ItemLockable.assignID(new ItemStack(LocksItems.lock), lockable.lock.id)));
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event)
	{
		ICapabilityLockBounds bounds = LocksCapabilities.getLockBounds(event.player);
		if(event.phase != Phase.START || bounds.get() == null) return;
		for(ItemStack stack : event.player.getHeldEquipment()) if(stack.getItem() instanceof ItemLock) return;
		bounds.set(null);
	}

	@SubscribeEvent
	public static void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(!event.getModID().equals(LocksCore.ID)) return;
		ConfigManager.sync(LocksCore.ID, Config.Type.INSTANCE);
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server == null) return;
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				LocksNetworks.network.sendToAll(new MessageConfiguration(LocksConfiguration.main));
			}
		};
		server.addScheduledTask(runnable);
	}
}