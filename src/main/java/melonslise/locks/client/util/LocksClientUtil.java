package melonslise.locks.client.util;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LocksClientUtil
{
	private LocksClientUtil() {}

	/*
	 * 
	 * Rendering
	 * 
	 */

	public static void drawTexturedRectangle(float x, float y, int u, int v, int width, int height, int texWidth, int texHeight)
	{
		float f = 1f / (float) texWidth;
		float f1 = 1f / (float) texHeight;
		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		buf.begin(7, DefaultVertexFormats.POSITION_TEX);
		buf.pos((double) x, (double) (y + (float) height), 0d).tex(((float) u * f), ((float) (v + height) * f1)).endVertex();
		buf.pos((double) (x + (float) width), (double) (y + (float) height), 0d).tex(((float) (u + width) * f), ((float) (v + height) * f1)).endVertex();
		buf.pos((double) (x + (float) width), (double) y, 0d).tex(((float) (u + width) * f),  ((float) v * f1)).endVertex();
		buf.pos((double) x, (double) y, 0d).tex(((float) u * f), ((float) v * f1)).endVertex();
		buf.finishDrawing();
		RenderSystem.enableAlphaTest();
		WorldVertexBufferUploader.draw(buf);
	}

	/*
	 * 
	 * Animation
	 * 
	 */

	public static float lerp(float start, float end, float progress)
	{
		return start + (end - start) * progress;
	}

	public static double lerp(double start, double end, double progress)
	{
		return start + (end - start) * progress;
	}

	/*
	 * FIXME 2d bezier
	 * Implement 2d cubic bezier function
	 * https://stackoverflow.com/questions/11696736/recreating-css3-transitions-cubic-bezier-curve
	 * https://math.stackexchange.com/questions/26846/is-there-an-explicit-form-for-cubic-b%C3%A9zier-curves
	 * https://www.gamedev.net/forums/topic/572263-bezier-curve-for-animation/
	 * https://math.stackexchange.com/questions/2571471/understanding-of-cubic-b%C3%A9zier-curves-in-one-dimension
	 */
	public static float cubicBezier1d(float anchor1, float anchor2, float progress)
	{
		float oneMinusP = 1f - progress;
		return 3 * oneMinusP * oneMinusP * progress * anchor1 + 3 * oneMinusP * progress * progress * anchor2 + progress * progress * progress;
	}
}