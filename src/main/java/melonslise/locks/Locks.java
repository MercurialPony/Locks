package melonslise.locks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;

import melonslise.locks.client.init.LocksScreens;
import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksContainerTypes;
import melonslise.locks.common.init.LocksFeatures;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.init.LocksRecipeSerializers;
import melonslise.locks.common.init.LocksSounds;
import melonslise.locks.common.item.ItemLock;
import melonslise.locks.common.item.ItemLocking;
import melonslise.locks.common.network.toClient.PacketLockableAdd;
import melonslise.locks.utility.Box;
import melonslise.locks.utility.Lockable;
import melonslise.locks.utility.LocksUtilities;
import melonslise.locks.utility.predicate.LocksPredicates;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod(Locks.ID)
public class Locks
{
	public static final String ID = "locks";

	public Locks()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(SoundEvent.class, this::registerSounds);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(ContainerType.class, this::registerContainerTypes);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(IRecipeSerializer.class, this::registerRecipes);

		ModLoadingContext.get().registerConfig(Type.SERVER, LocksConfiguration.SPEC);
	}

	private void clientSetup(FMLClientSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.addListener(this::render);

		LocksScreens.register();
	}

	private void commonSetup(FMLCommonSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.addGenericListener(World.class, this::attachCapabilitiesToWorld);
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, this::attachCapabilitiesToEntity);

		MinecraftForge.EVENT_BUS.addListener(this::onRightClick);
		MinecraftForge.EVENT_BUS.addListener(this::onPlayerConnect);
		MinecraftForge.EVENT_BUS.addListener(this::onPlayerChangeDimension);
		MinecraftForge.EVENT_BUS.addListener(this::onPlayerRespawn);
		MinecraftForge.EVENT_BUS.addListener(this::onBlockBreaking);
		MinecraftForge.EVENT_BUS.addListener(this::onBlockBreak);
		MinecraftForge.EVENT_BUS.addListener(this::onPlayerTick);

		LocksCapabilities.register();
		LocksNetworks.register();
		LocksFeatures.register();
	}

	private void registerItems(RegistryEvent.Register<Item> event)
	{
		LocksItems.register(event);
	}

	private void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		LocksSounds.register(event);
	}

	private void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> event)
	{
		LocksContainerTypes.register(event);
	}

	private void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event)
	{
		LocksRecipeSerializers.register(event);
	}

	private void attachCapabilitiesToWorld(AttachCapabilitiesEvent<World> event)
	{
		LocksCapabilities.attachToWorld(event);
	}

	private void attachCapabilitiesToEntity(AttachCapabilitiesEvent<Entity> event)
	{
		LocksCapabilities.attachToEntity(event);
	}

	private void onPlayerConnect(PlayerLoggedInEvent event)
	{
		synchronizeLockables(event.getPlayer().world);
	}

	private void onPlayerChangeDimension(PlayerChangedDimensionEvent event)
	{
		synchronizeLockables(event.getPlayer().world, event.getTo());
	}

	// TODO Sync only if died in another dim
	private void onPlayerRespawn(PlayerRespawnEvent event)
	{
		synchronizeLockables(event.getPlayer().world);
	}

	public static void synchronizeLockables(World world, DimensionType dimension)
	{
		world.getCapability(LocksCapabilities.LOCKABLES).ifPresent(lockables ->
		{
			for(Lockable lockable : lockables.getLockables().values()) LocksNetworks.MAIN.send(PacketDistributor.DIMENSION.with(() -> dimension), new PacketLockableAdd(lockable));
		});
	}

	public static void synchronizeLockables(World world)
	{
		synchronizeLockables(world, world.dimension.getType());
	}

	// TODO FIX FOR CLIENT
	private void onRightClick(RightClickBlock event)
	{
		BlockPos position = event.getPos();
		World world = event.getWorld();
		PlayerEntity player = event.getEntityPlayer();

		world.getCapability(LocksCapabilities.LOCKABLES).ifPresent(lockables ->
		{
			List<Lockable> intersecting = lockables.getLockables().values().stream().filter(new PredicateIntersecting(position)).collect(Collectors.toList());
			if(intersecting.isEmpty()) return;
			if(event.getHand() != Hand.MAIN_HAND) // TODO Better way to prevent firing multiple times
			{
				event.setUseBlock(Result.DENY);
				return;
			}
			if(intersecting.stream().anyMatch(LocksPredicates.LOCKED))
			{
				event.setUseBlock(Result.DENY);
				world.playSound(null, position, LocksSounds.LOCK_RATTLE, SoundCategory.BLOCKS, 1f, 1f); // TODO Play sound only if item returns fail/pass
				//player.swingArm(Hand.MAIN_HAND);
				return;
			}
			if(LocksConfiguration.MAIN.removableLocks.get() && player.isSneaking() && event.getItemStack().isEmpty())
			{
				List<Lockable> matching = intersecting.stream().filter(LocksPredicates.NOT_LOCKED).collect(Collectors.toList());
				if(matching.isEmpty()) return;
				event.setUseBlock(Result.DENY);
				world.playSound(null, position, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 0.8f, 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
				if(world.isRemote) return;
				//player.swingArm(Hand.MAIN_HAND);
				for(Lockable lockable : matching)
				{
					world.func_217376_c(new ItemEntity(world, (double) position.getX() + 0.5d, (double) position.getY() + 0.5d, (double) position.getZ() + 0.5d, ItemLocking.assignID(new ItemStack(LocksItems.LOCK), lockable.lock.id)));
					lockables.remove(lockable.networkID);
				}
			}
		});
	}

	private void onBlockBreaking(BreakSpeed event)
	{
		if(!canBreakLockable(event.getEntityPlayer(), event.getPos())) event.setCanceled(true);
	}

	private void onBlockBreak(BreakEvent event)
	{
		if(!canBreakLockable(event.getPlayer(), event.getPos())) event.setCanceled(true);
	}

	public static boolean canBreakLockable(PlayerEntity player, BlockPos position)
	{
		return player.world.getCapability(LocksCapabilities.LOCKABLES).map(lockables ->
		{
			return !LocksConfiguration.MAIN.unbreakableLocks.get() || player.isCreative() || !lockables.getLockables().values().stream().anyMatch(LocksPredicates.LOCKED.and(new PredicateIntersecting(position)));
		}).orElse(true);
	}

	private void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END) return;
		event.player.getCapability(LocksCapabilities.LOCK_POSITION).ifPresent(lockPosition ->
		{
			if(lockPosition.get() == null) return;
			for(ItemStack stack : event.player.getHeldEquipment()) if(stack.getItem() instanceof ItemLock) return;
			lockPosition.set(null);
		});
	}

	// TODO Get this to work with voxel shapes instead
	// TODO Cull
	// TODO Fix lighting
	private void render(RenderWorldLastEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		GlStateManager.pushMatrix();
		GlStateManager.translated(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);
		mc.world.getCapability(LocksCapabilities.LOCKABLES).ifPresent(lockables ->
		{
			for(Lockable lockable : lockables.getLockables().values())
			{
				ArrayList<AxisAlignedBB> boxes = Lists.newArrayList();
				for(BlockPos position : BlockPos.getAllInBoxMutable(lockable.box.x1, lockable.box.y1, lockable.box.z1, lockable.box.x2 - 1, lockable.box.y2 - 1, lockable.box.z2 - 1))
				{
					VoxelShape shape = mc.world.getBlockState(position).getRenderShape(mc.world, position);
					if(shape.isEmpty()) continue;
					AxisAlignedBB box = shape.getBoundingBox();
					box = box.offset(position);
					AxisAlignedBB union = box;
					Iterator<AxisAlignedBB> iterator = boxes.iterator();
					while(iterator.hasNext())
					{
						AxisAlignedBB box1 = iterator.next();
						if(LocksUtilities.intersectsInclusive(union, box1))
						{
							union = union.union(box1);
							iterator.remove();
						}
					}
					boxes.add(union);
				}
				if(boxes.isEmpty()) continue;
				Vec3d center = LocksUtilities.getBoxSideCenter(lockable.box, lockable.side);
				Direction side = lockable.side;
				Vec3d point = center;
				double distanceMinimum = -1d;
				for(AxisAlignedBB box : boxes) for(Direction side1 : Direction.values())
				{
					Vec3d point1 = LocksUtilities.getBoxSideCenter(box, side1).add(new Vec3d(side1.getDirectionVec()).scale(0.05d));
					double distance = center.squareDistanceTo(point1);
					if(distanceMinimum != -1d && distance >= distanceMinimum) continue;
					point = point1;
					distanceMinimum = distance;
					side = side1;
				}
				GlStateManager.pushMatrix();
				GlStateManager.translated(point.x, point.y, point.z);
				GlStateManager.rotatef(lockable.side.getHorizontalAngle(), 0f, 1f, 0f);
				if(lockable.side == Direction.UP) GlStateManager.rotatef(90f, 1f, 0f, 0f);
				if(lockable.side == Direction.DOWN) GlStateManager.rotatef(-90f, 1f, 0f, 0f);
				GlStateManager.scalef(0.5f, 0.5f, 0.5f);
				mc.gameRenderer.enableLightmap();
				mc.getItemRenderer().renderItem(new ItemStack(LocksItems.LOCK), TransformType.FIXED);
				mc.gameRenderer.disableLightmap();
				GlStateManager.popMatrix();
			}

			mc.player.getCapability(LocksCapabilities.LOCK_POSITION).ifPresent(lockPosition ->
			{
				BlockPos position1 = lockPosition.get();
				if(position1 != null)
				{
					BlockPos position2 = mc.objectMouseOver instanceof BlockRayTraceResult ? ((BlockRayTraceResult) mc.objectMouseOver).getPos() : position1;
					Box box = new Box(position1, position2);
					GlStateManager.enableBlend();
					GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GlStateManager.lineWidth(1F);
					GlStateManager.disableTexture();
					GlStateManager.depthMask(false);
					GlStateManager.disableDepthTest();
					WorldRenderer.drawBoundingBox((double) box.x1, (double) box.y1, (double) box.z1, (double) box.x2, (double) box.y2, (double) box.z2, 0f, 1f, 0f, 0.5f);
					GlStateManager.enableDepthTest();
					GlStateManager.depthMask(true);
					GlStateManager.enableTexture();
					GlStateManager.disableBlend();
				}
			});
			GlStateManager.popMatrix();
		});
	}
}