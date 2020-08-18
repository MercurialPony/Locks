package melonslise.locks.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import melonslise.locks.Locks;
import melonslise.locks.client.init.LocksRenderTypes;
import melonslise.locks.client.util.LocksClientUtil;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lockable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
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

	// Initialized in client setup to avoid null crash
	public static ItemStack LOCK_MODEL_STACK = null;

	// TODO Use voxel shapes instead
	// TODO Move render to Lockable?
	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		ActiveRenderInfo info = mc.gameRenderer.getActiveRenderInfo();
		Matrix4f proj = event.getProjectionMatrix();
		Vector3d origin = info.getProjectedView();
		IRenderTypeBuffer.Impl buf = mc.getRenderTypeBuffers().getBufferSource();
		MatrixStack mtx = event.getMatrixStack();
		BlockPos.Mutable mutPos = new BlockPos.Mutable();
		mc.world.getCapability(LocksCapabilities.LOCKABLES)
			.ifPresent(lockables ->
			{
				for(Lockable lockable : lockables.get().values())
				{
					Lockable.State state = lockable.getLockState(mc.world);
					if(state == null || !state.inRange(origin) || !state.inView(LocksClientUtil.getClippingHelper(mtx, proj, info)))
						continue;
					mtx.push();
					// For some reason translating by negative player position and then the point coords causes jittering in very big z and x coords. Why? Thus we use 1 translation instead
					mtx.translate(state.pos.x - origin.x, state.pos.y - origin.y, state.pos.z - origin.z);
					mtx.rotate(Vector3f.YP.rotationDegrees(-state.orient.dir.getHorizontalAngle() - 180f));
					if(state.orient.face != AttachFace.WALL)
						mtx.rotate(Vector3f.XP.rotationDegrees(90f));
					mtx.translate(0d, 0.1d, 0d);
					mtx.rotate(Vector3f.ZP.rotationDegrees(MathHelper.sin(LocksClientUtil.cubicBezier1d(1f, 1f, LocksClientUtil.lerp(lockable.maxShakeTicks - lockable.prevShakeTicks, lockable.maxShakeTicks - lockable.shakeTicks, event.getPartialTicks()) / (float) lockable.maxShakeTicks) * (float) lockable.maxShakeTicks / 5f * 3.14f) * 10f));
					mtx.translate(0d, -0.1d, 0d);
					mtx.scale(0.5f, 0.5f, 0.5f);
					int packedLight = WorldRenderer.getCombinedLight(mc.world, mutPos.setPos(state.pos.x, state.pos.y, state.pos.z));
					mc.getItemRenderer().renderItem(LOCK_MODEL_STACK, ItemCameraTransforms.TransformType.FIXED, packedLight, OverlayTexture.NO_OVERLAY, mtx, buf);
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
				// Ditto
				mtx.push();
				WorldRenderer.drawBoundingBox(mtx, buf.getBuffer(LocksRenderTypes.OVERLAY_LINES), box.x1 - origin.x, box.y1 - origin.y, box.z1 - origin.z, box.x2 - origin.x, box.y2 - origin.y, box.z2 - origin.z, r, g, 0f, 0.5f);
				RenderSystem.disableDepthTest();
				buf.finish();
				mtx.pop();
			});
	}
}