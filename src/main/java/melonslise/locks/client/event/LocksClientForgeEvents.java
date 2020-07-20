package melonslise.locks.client.event;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import melonslise.locks.Locks;
import melonslise.locks.client.util.LocksClientUtil;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.Orientation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Locks.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LocksClientForgeEvents
{
	private LocksClientForgeEvents() {}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if(event.phase == TickEvent.Phase.END || mc.world == null || mc.isGamePaused())
			return;
		mc.world.getCapability(LocksCapabilities.LOCKABLES).ifPresent(lockables -> lockables.get().values().forEach(lockable ->
		{
			if(lockable.box.loaded(mc.world))
				lockable.tick();
		}));
	}

	public static final ItemStack LOCK_STACK = new ItemStack(LocksItems.LOCK);

	// TODO Use voxel shapes instead
	// TODO Move render to Lockable?
	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		mc.world.getCapability(LocksCapabilities.LOCKABLES)
			.ifPresent(lockables ->
			{
				for(Lockable lockable : lockables.get().values())
				{
					if(!lockable.box.loaded(mc.world) || !lockable.inRange() || !lockable.box.inView())
						continue;
					Pair<Vec3d, Orientation> pair = lockable.getLockState(mc.world);
					if(pair == null)
						continue;
					GlStateManager.pushMatrix();
					// For some reason translating by negative player position and then the point coords causes jittering in very big z and x coords. Why? Thus we use 1 translation instead
					GlStateManager.translated(pair.getLeft().x - TileEntityRendererDispatcher.staticPlayerX, pair.getLeft().y - TileEntityRendererDispatcher.staticPlayerY, pair.getLeft().z - TileEntityRendererDispatcher.staticPlayerZ);
					GlStateManager.rotatef(-pair.getRight().dir.getHorizontalAngle() - 180f, 0f, 1f, 0f);
					if(pair.getRight().face != AttachFace.WALL)
						GlStateManager.rotatef(90f, 1f, 0f, 0f);
					GlStateManager.translated(0d, 0.1d, 0d);
					GlStateManager.rotatef(MathHelper.sin(LocksClientUtil.cubicBezier1d(1f, 1f, LocksClientUtil.lerp(lockable.maxShakeTicks - lockable.prevShakeTicks, lockable.maxShakeTicks - lockable.shakeTicks, event.getPartialTicks()) / (float) lockable.maxShakeTicks) * (float) lockable.maxShakeTicks / 5f * 3.14f) * 10f, 0f, 0f, 1f);
					GlStateManager.translated(0d, -0.1d, 0d);
					GlStateManager.scalef(0.5f, 0.5f, 0.5f);
					int lightValue = mc.world.getCombinedLight(new BlockPos(pair.getLeft()), 0);
					GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) (lightValue % 65536), (float) (lightValue / 65536));
					mc.gameRenderer.enableLightmap();
					mc.getItemRenderer().renderItem(LOCK_STACK, TransformType.FIXED);
					GlStateManager.popMatrix();
				}
			});
		mc.player.getCapability(LocksCapabilities.LOCK_SELECTION)
			.ifPresent(select ->
			{
				BlockPos pos1 = select.get();
				if(pos1 == null)
					return;
				BlockPos pos2 = mc.objectMouseOver instanceof BlockRayTraceResult ? ((BlockRayTraceResult) mc.objectMouseOver).getPos() : pos1;
				Cuboid6i box = new Cuboid6i(pos1, pos2);
				float r = 0f, g = 0f;
				if(box.volume() > LocksServerConfig.MAX_LOCKABLE_VOLUME.get() || !LocksServerConfig.canLock(mc.world, pos2))
					r = 1f;
				else
					g = 1f;
				GlStateManager.enableBlend();
				GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.lineWidth(1F);
				GlStateManager.disableTexture();
				GlStateManager.depthMask(false);
				GlStateManager.disableDepthTest();
				mc.gameRenderer.disableLightmap();
				// Ditto
				WorldRenderer.drawBoundingBox((double) box.x1 - TileEntityRendererDispatcher.staticPlayerX, (double) box.y1 - TileEntityRendererDispatcher.staticPlayerY, (double) box.z1 - TileEntityRendererDispatcher.staticPlayerZ, (double) box.x2 - TileEntityRendererDispatcher.staticPlayerX, (double) box.y2 - TileEntityRendererDispatcher.staticPlayerY, (double) box.z2 - TileEntityRendererDispatcher.staticPlayerZ, r, g, 0f, 0.5f);
				GlStateManager.enableDepthTest();
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture();
				GlStateManager.disableBlend();
			});
	}
}