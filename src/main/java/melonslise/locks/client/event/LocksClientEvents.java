package melonslise.locks.client.event;

import org.apache.commons.lang3.tuple.Pair;

import melonslise.locks.Locks;
import melonslise.locks.client.util.LocksClientUtil;
import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.capability.ISelection;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.util.AttachFace;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.Orientation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Locks.ID, value = Side.CLIENT)
public final class LocksClientEvents
{
	private LocksClientEvents() {}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		for(Item item : LocksItems.ITEMS)
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		if(event.phase == TickEvent.Phase.END || mc.world == null || mc.isGamePaused())
			return;
		mc.world.getCapability(LocksCapabilities.LOCKABLES, null).get().values().forEach(lockable ->
		{
			if(lockable.box.loaded(mc.world))
				lockable.tick();
		});
	}

	public static final ItemStack LOCK_STACK = new ItemStack(LocksItems.LOCK);

	// TODO Use voxel shapes instead
	// TODO Move render to Lockable?
	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		ILockableStorage lockables = mc.world.getCapability(LocksCapabilities.LOCKABLES, null);
		for(Lockable lockable : lockables.get().values())
		{
			if(!lockable.box.loaded(mc.world) || !lockable.inRange() || !lockable.box.inView())
				continue;
			Pair<Vec3d, Orientation> state = lockable.getLockState(mc.world);
			if(state == null)
				continue;
			GlStateManager.pushMatrix();
			// For some reason translating by negative player position and then the point coords causes jittering in very big z and x coords. Why? Thus we use 1 translation instead
			GlStateManager.translate(state.getLeft().x - TileEntityRendererDispatcher.staticPlayerX, state.getLeft().y - TileEntityRendererDispatcher.staticPlayerY, state.getLeft().z - TileEntityRendererDispatcher.staticPlayerZ);
			GlStateManager.rotate(-state.getRight().dir.getHorizontalAngle() - 180f, 0f, 1f, 0f);
			if(state.getRight().face != AttachFace.WALL)
				GlStateManager.rotate(90f, 1f, 0f, 0f);
			GlStateManager.translate(0d, 0.1d, 0d);
			GlStateManager.rotate(MathHelper.sin(LocksClientUtil.cubicBezier1d(1f, 1f, LocksClientUtil.lerp(lockable.maxShakeTicks - lockable.prevShakeTicks, lockable.maxShakeTicks - lockable.shakeTicks, event.getPartialTicks()) / (float) lockable.maxShakeTicks) * (float) lockable.maxShakeTicks / 5f * 3.14f) * 10f, 0f, 0f, 1f);
			GlStateManager.translate(0d, -0.1d, 0d);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			//int lightValue = mc.world.getCombinedLight(new BlockPos(pair.getLeft()), 0);
			//GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) (lightValue % 65536), (float) (lightValue / 65536));
			//mc.gameRenderer.enableLightmap();
			GlStateManager.disableLighting();
			GlStateManager.pushAttrib();
			RenderHelper.enableStandardItemLighting();
			mc.entityRenderer.enableLightmap();
			mc.getRenderItem().renderItem(LOCK_STACK, ItemCameraTransforms.TransformType.FIXED);
			mc.entityRenderer.disableLightmap();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		}

		ISelection select = mc.player.getCapability(LocksCapabilities.LOCK_SELECTION, null);
		BlockPos pos1 = select.get();
		if(pos1 == null)
			return;
		BlockPos pos2 = mc.objectMouseOver.getBlockPos() != null ? mc.objectMouseOver.getBlockPos() : pos1;
		Cuboid6i box = new Cuboid6i(pos1, pos2);
		float r = 0f, g = 0f;
		LocksConfig.Server cfg = LocksConfig.getServer(mc.world);
		if(box.volume() > cfg.maxLockableVolume || !cfg.canLock(mc.world, pos2))
			r = 1f;
		else
			g = 1f;
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(1F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.disableDepth();
		// Ditto
		RenderGlobal.drawBoundingBox((double) box.x1 - TileEntityRendererDispatcher.staticPlayerX, (double) box.y1 - TileEntityRendererDispatcher.staticPlayerY, (double) box.z1 - TileEntityRendererDispatcher.staticPlayerZ, (double) box.x2 - TileEntityRendererDispatcher.staticPlayerX, (double) box.y2 - TileEntityRendererDispatcher.staticPlayerY, (double) box.z2 - TileEntityRendererDispatcher.staticPlayerZ, r, g, 0f, 0.5f);
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		GlStateManager.enableLighting();
	}
}