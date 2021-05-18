package melonslise.locks.client.util;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LocksClientUtil
{
	private LocksClientUtil() {}

	public static ActiveRenderInfo getCamera()
	{
		return Minecraft.getInstance().gameRenderer.getMainCamera();
	}

	public static ClippingHelper getClippingHelper(MatrixStack mtx, Matrix4f proj)
	{
		ClippingHelper ch = Minecraft.getInstance().levelRenderer.capturedFrustum;
		if(ch != null)
			return ch;
		ch = new ClippingHelper(mtx.last().pose(), proj);
		Vector3d pos = getCamera().getPosition();
		ch.prepare(pos.x, pos.y, pos.z);
		return ch;
	}

	public static double distanceToLineSq(Vector3d p, Vector3d l1, Vector3d l2)
	{
		Vector3d l = l2.subtract(l1);
		return l.cross(p.subtract(l1)).lengthSqr() / l.lengthSqr();
	}

	/*
	public static Matrix4f getProjectionMatrix(float pt)
	{
		return Minecraft.getInstance().gameRenderer.getProjectionMatrix(getCamera(), pt, true);
	}

	public static Matrix4f getViewMatrix()
	{
		ActiveRenderInfo cam = getCamera();
		Vector3f x = cam.left;
		Vector3f y = cam.getUpVector();
		Vector3f z = cam.getLookVector();
		return new Matrix4f(new float[] {
			-x.x(), -x.y(), -x.z(), 0f,
			-y.x(), -y.y(), -y.z(), 0f,
			z.x(), z.y(), z.z(), 0f,
			0f, 0f, 0f, 1f
		});
	}
	*/

	// https://forums.minecraftforge.net/topic/88562-116solved-3d-to-2d-conversion/
	// And big thanks to JTK222 Lukas!!!
	public static Vector3f worldToScreen(Vector3d pos, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		ActiveRenderInfo cam = getCamera();
		Vector3d o = cam.getPosition();

		Vector3f pos1 = new Vector3f((float) (o.x - pos.x), (float) (o.y - pos.y), (float) (o.z - pos.z));
		Quaternion rot = cam.rotation().copy();
		rot.conj();
		pos1.transform(rot);

		// Account for view bobbing
		if (mc.options.bobView && mc.getCameraEntity() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) mc.getCameraEntity();
			float f = player.walkDist - player.walkDistO;
			float f1 = -(player.walkDist + f * partialTicks);
			float f2 = MathHelper.lerp(partialTicks, player.oBob, player.bob);

			Quaternion rot1 = Vector3f.XP.rotationDegrees(Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2f) * f2) * 5f);
			Quaternion rot2 = Vector3f.ZP.rotationDegrees(MathHelper.sin(f1 * (float) Math.PI) * f2 * 3f);
			rot1.conj();
			rot2.conj();
			pos1.transform(rot1);
			pos1.transform(rot2);
			pos1.add(MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5f, Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2), 0f);
		}

		MainWindow w = mc.getWindow();
		float sc = w.getGuiScaledHeight() / 2f / pos1.z() / (float) Math.tan(Math.toRadians(mc.gameRenderer.getFov(cam, partialTicks, true) / 2f));
		pos1.mul(-sc, -sc, 1f);
		pos1.add(w.getGuiScaledWidth() / 2f, w.getGuiScaledHeight() / 2f, 0f);

		return pos1;
	}

	/*
	public static Vector2f worldToScreen(Vector3d pos, Matrix4f proj)
	{
		Vector3d o = getCamera().getPosition();
		Vector4f pos1 = new Vector4f((float) (o.x - pos.x), (float) (o.y - pos.y), (float) (o.z - pos.z), 1f);
		pos1.transform(getViewMatrix());
		pos1.transform(proj);
		pos1.perspectiveDivide();
		MainWindow w = Minecraft.getInstance().getWindow();
		return new Vector2f((1f - pos1.x()) * w.getGuiScaledWidth() / 2f, (1f - pos1.y()) * w.getGuiScaledHeight() / 2f);
	}
	*/

	public static void texture(MatrixStack mtx, float x, float y, int u, int v, int width, int height, int texWidth, int texHeight, float alpha) // FIXME Cant batch like the others? Why? ;-;
	{
		Matrix4f last = mtx.last().pose();
		float f = 1f / texWidth;
		float f1 = 1f / texHeight;

		BufferBuilder buf = Tessellator.getInstance().getBuilder();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		buf.vertex(last, x, y + height, 0f).uv(u * f, (v + height) * f1).color(1f, 1f, 1f, alpha).endVertex();
		buf.vertex(last, x + width, y + height, 0f).uv((u + width) * f, (v + height) * f1).color(1f, 1f, 1f, alpha).endVertex();
		buf.vertex(last, x + width, y, 0f).uv((u + width) * f,  v * f1).color(1f, 1f, 1f, alpha).endVertex();
		buf.vertex(last, x, y, 0f).uv(u * f, v * f1).color(1f, 1f, 1f, alpha).endVertex();
		buf.end();
		RenderSystem.enableAlphaTest();
		WorldVertexBufferUploader.end(buf);
	}

	// https://stackoverflow.com/questions/7854043/drawing-rectangle-between-two-points-with-arbitrary-width
	public static void line(BufferBuilder buf, MatrixStack mtx, float x1, float y1, float x2, float y2, float width, float r, float g, float b, float a)
	{
		Matrix4f last = mtx.last().pose();
		// Construct perpendicular
		float pX = y2 - y1;
		float pY = x1 - x2;
		// Normalize and scale by half width
		float pL = MathHelper.sqrt(pX * pX + pY * pY);
		pX *= width / 2f / pL;
		pY *= width / 2f / pL;

		buf.vertex(last, x1 + pX, y1 + pY, 0f).color(r, g, b, a).endVertex();
		buf.vertex(last, x1 - pX, y1 - pY, 0f).color(r, g, b, a).endVertex();
		buf.vertex(last, x2 - pX, y2 - pY, 0f).color(r, g, b, a).endVertex();
		buf.vertex(last, x2 + pX, y2 + pY, 0f).color(r, g, b, a).endVertex();
	}

	public static void square(BufferBuilder buf, MatrixStack mtx, float x, float y, float length, float r, float g, float b, float a)
	{
		Matrix4f last = mtx.last().pose();
		length /= 2f;
		buf.vertex(last, x - length, y - length, 0f).color(r, g, b, a).endVertex();
		buf.vertex(last, x - length, y + length, 0f).color(r, g, b, a).endVertex();
		buf.vertex(last, x + length, y + length, 0f).color(r, g, b, a).endVertex();
		buf.vertex(last, x + length, y - length, 0f).color(r, g, b, a).endVertex();
	}

	public static void vGradient(BufferBuilder bld, MatrixStack mtx, int x1, int y1, int x2, int y2, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2)
	{
		Matrix4f last = mtx.last().pose();
		bld.vertex(last, x2, y1, 0f).color(r1, g1, b1, a1).endVertex();
		bld.vertex(last, x1, y1, 0f).color(r1, g1, b1, a1).endVertex();
		bld.vertex(last, x1, y2, 0f).color(r2, g2, b2, a2).endVertex();
		bld.vertex(last, x2, y2, 0f).color(r2, g2, b2, a2).endVertex();
	}

	public static float lerp(float start, float end, float progress)
	{
		return start + (end - start) * progress;
	}

	public static double lerp(double start, double end, double progress)
	{
		return start + (end - start) * progress;
	}

	/*
	 * Make 2d bezier??
	 * Implement 2d cubic bezier function
	 * https://stackoverflow.com/questions/11696736/recreating-css3-transitions-cubic-bezier-curve
	 * https://math.stackexchange.com/questions/26846/is-there-an-explicit-form-for-cubic-b%C3%A9zier-curves
	 * https://www.gamedev.net/forums/topic/572263-bezier-curve-for-animation/
	 * https://math.stackexchange.com/questions/2571471/understanding-of-cubic-b%C3%A9zier-curves-in-one-dimension
	 */
	public static float cubicBezier1d(float anchor1, float anchor2, float progress)
	{
		float omp = 1f - progress;
		return 3f * omp * omp * progress * anchor1 + 3f * omp * progress * progress * anchor2 + progress * progress * progress;
	}
}