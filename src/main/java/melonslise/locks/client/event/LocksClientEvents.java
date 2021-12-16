package melonslise.locks.client.event;

import melonslise.locks.Locks;
import melonslise.locks.client.util.LocksClientUtil;
import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.capability.ISelection;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.util.AttachFace;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lockable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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
		mc.world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null).getLoaded().values().forEach(lockable ->
		{
			if(lockable.box.loaded(mc.world))
				lockable.tick();
		});
	}
	
	// TODO Use voxel shapes instead
	// TODO Move render to Lockable?
	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Vec3d origin = new Vec3d(TileEntityRendererDispatcher.staticPlayerX, TileEntityRendererDispatcher.staticPlayerY, TileEntityRendererDispatcher.staticPlayerZ);
		BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
		ILockableHandler lockables = mc.world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
		
		GlStateManager.enableRescaleNormal();
		
		for(Lockable lockable : lockables.getLoaded().values())
		{
			Lockable.State state = lockable.getLockState(mc.world);
			if(state == null || !state.inRange(origin) || !state.inView(ClippingHelperImpl.getInstance(), origin))
				continue;
			GlStateManager.pushMatrix();
			// For some reason translating by negative player position and then the point coords causes jittering in very big z and x coords. Why? Thus we use 1 translation instead
			GlStateManager.translate(state.pos.x - origin.x, state.pos.y - origin.y, state.pos.z - origin.z);
			GlStateManager.rotate(-state.orient.dir.getHorizontalAngle() - 180f, 0f, 1f, 0f);
			if(state.orient.face != AttachFace.WALL)
				GlStateManager.rotate(90f, 1f, 0f, 0f);
			GlStateManager.translate(0d, 0.1d, 0d);
			GlStateManager.rotate(MathHelper.sin(LocksClientUtil.cubicBezier1d(1f, 1f, LocksClientUtil.lerp(lockable.maxShakeTicks - lockable.prevShakeTicks, lockable.maxShakeTicks - lockable.shakeTicks, event.getPartialTicks()) / (float) lockable.maxShakeTicks) * (float) lockable.maxShakeTicks / 5f * 3.14f) * 10f, 0f, 0f, 1f);
			GlStateManager.translate(0d, -0.1d, 0d);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			int light = mc.world.getCombinedLight(mutPos.setPos(state.pos.x, state.pos.y, state.pos.z), 0);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			mc.entityRenderer.enableLightmap();
            RenderHelper.enableStandardItemLighting();
			mc.getRenderItem().renderItem(lockable.stack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
			mc.entityRenderer.disableLightmap();
			GlStateManager.popMatrix();
		}

		GlStateManager.disableRescaleNormal();

		ISelection select = mc.player.getCapability(LocksCapabilities.SELECTION, null);
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
		mc.entityRenderer.disableLightmap();
		RenderGlobal.drawBoundingBox((double) box.x1 - origin.x, (double) box.y1 - origin.y, (double) box.z1 - origin.z, (double) box.x2 - origin.x, (double) box.y2 - origin.y, (double) box.z2 - origin.z, r, g, 0f, 0.5f);
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		GlStateManager.enableLighting();
	}
}