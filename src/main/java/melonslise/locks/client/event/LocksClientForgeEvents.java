package melonslise.locks.client.event;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import melonslise.locks.Locks;
import melonslise.locks.client.init.LocksRenderTypes;
import melonslise.locks.client.proxy.ClientProxy;
import melonslise.locks.client.util.LocksClientUtil;
import melonslise.locks.common.capability.LockableStorage;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.Orientation;
import melonslise.locks.coremod.LocksDelegates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Locks.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LocksClientForgeEvents
{
	private LocksClientForgeEvents() {}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event)
	{
		if(event.getWorld().isRemote())
			ClientProxy.CLIENT_LOCKABLES = LazyOptional.of(() -> new LockableStorage(event.getWorld().getWorld()));
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if(event.phase == TickEvent.Phase.END || mc.world == null || mc.isGamePaused())
			return;
		Locks.PROXY.getLockables(mc.world).ifPresent(lockables -> lockables.get().values().forEach(lockable ->
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
		Vector3d origin = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
		IRenderTypeBuffer.Impl buf = mc.getRenderTypeBuffers().getBufferSource();
		MatrixStack mtx = event.getMatrixStack();
		BlockPos.Mutable mutPos = new BlockPos.Mutable();
		Locks.PROXY.getLockables(mc.world)
			.ifPresent(lockables ->
			{
				for(Lockable lockable : lockables.get().values())
				{
					if(!lockable.box.loaded(mc.world) || !lockable.inRange() || !lockable.box.inView(LocksDelegates.clippingHelper))
						continue;
					Pair<Vector3d, Orientation> pair = lockable.getLockState(mc.world);
					if(pair == null)
						continue;
					Vector3d lockPos = pair.getLeft();
					mtx.push();
					// For some reason translating by negative player position and then the point coords causes jittering in very big z and x coords. Why? Thus we use 1 translation instead
					mtx.translate(lockPos.x - origin.x, lockPos.y - origin.y, lockPos.z - origin.z);
					mtx.rotate(Vector3f.YP.rotationDegrees(-pair.getRight().dir.getHorizontalAngle() - 180f));
					if(pair.getRight().face != AttachFace.WALL)
						mtx.rotate(Vector3f.XP.rotationDegrees(90f));
					mtx.translate(0d, 0.1d, 0d);
					mtx.rotate(Vector3f.ZP.rotationDegrees(MathHelper.sin(LocksClientUtil.cubicBezier1d(1f, 1f, LocksClientUtil.lerp(lockable.maxShakeTicks - lockable.prevShakeTicks, lockable.maxShakeTicks - lockable.shakeTicks, event.getPartialTicks()) / (float) lockable.maxShakeTicks) * (float) lockable.maxShakeTicks / 5f * 3.14f) * 10f));
					mtx.translate(0d, -0.1d, 0d);
					mtx.scale(0.5f, 0.5f, 0.5f);
					int packedLight = WorldRenderer.getCombinedLight(mc.world, mutPos.setPos(lockPos.x, lockPos.y, lockPos.z));
					// GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) (lightValue % 65536), (float) (lightValue / 65536));
					// mc.gameRenderer.enableLightmap();
					mc.getItemRenderer().renderItem(LOCK_STACK, ItemCameraTransforms.TransformType.FIXED, packedLight, OverlayTexture.NO_OVERLAY, mtx, buf);
					RenderSystem.disableDepthTest();
					buf.finish();
					mtx.pop();
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
				// GlStateManager.enableBlend();
				// GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				// GlStateManager.lineWidth(1F);
				// GlStateManager.disableTexture();
				// GlStateManager.depthMask(false);
				// GlStateManager.disableDepthTest();
				// mc.gameRenderer.disableLightmap();
				// Ditto
				mtx.push();
				WorldRenderer.drawBoundingBox(mtx, buf.getBuffer(LocksRenderTypes.OVERLAY_LINES), box.x1 - origin.x, box.y1 - origin.y, box.z1 - origin.z, box.x2 - origin.x, box.y2 - origin.y, box.z2 - origin.z, r, g, 0f, 0.5f);
				RenderSystem.disableDepthTest();
				buf.finish();
				mtx.pop();
				// GlStateManager.enableDepthTest();
				// GlStateManager.depthMask(true);
				// GlStateManager.enableTexture();
				// GlStateManager.disableBlend();
			});
	}
}