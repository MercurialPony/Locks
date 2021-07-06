package melonslise.locks.client.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class LocksClientUtil
{
	private LocksClientUtil() {}

	/*
	 * 
	 * Rendering
	 * 
	 */

	public static void drawTexturedRectangle(float x, float y, int u, int v, int width, int height, int textureWidth, int textureHeight, float alpha)
	{
		float f = 1f / (float) textureWidth;
		float f1 = 1f / (float) textureHeight;
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder bld = tes.getBuffer();
		bld.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		//FIXME Alpha is failing to render even with a simple .color(1f, 1f, 1f, alpha) in the builder
		//and even with GlStateManager.enableAlpha();

		/*
		bld.pos((double) x, (double) (y + (float) height), 0d).tex((double) ((float) u * f), (double) ((float) (v + height) * f1)).color(1f, 1f, 1f, alpha).endVertex();
		bld.pos((double) (x + (float) width), (double) (y + (float) height), 0d).tex((double) ((float) (u + width) * f), (double) ((float) (v + height) * f1)).color(1f, 1f, 1f, alpha).endVertex();
		bld.pos((double) (x + (float) width), (double) y, 0d).tex((double) ((float) (u + width) * f), (double) ((float) v * f1)).color(1f, 1f, 1f, alpha).endVertex();
		bld.pos((double) x, (double) y, 0d).tex((double) ((float) u * f), (double) ((float) v * f1)).color(1f, 1f, 1f, alpha).endVertex();
		*/
		
		//Using legacy rendering instead...
		bld.pos((double) x, (double) (y + (float) height), 0d).tex((double) ((float) u * f), (double) ((float) (v + height) * f1)).endVertex();
		bld.pos((double) (x + (float) width), (double) (y + (float) height), 0d).tex((double) ((float) (u + width) * f), (double) ((float) (v + height) * f1)).endVertex();
		bld.pos((double) (x + (float) width), (double) y, 0d).tex((double) ((float) (u + width) * f), (double) ((float) v * f1)).endVertex();
		bld.pos((double) x, (double) y, 0d).tex((double) ((float) u * f), (double) ((float) v * f1)).endVertex();
		
		
		
		tes.draw();
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
	 * Make 2d bezier??
	 * Implement 2d cubic bezier function
	 * https://stackoverflow.com/questions/11696736/recreating-css3-transitions-cubic-bezier-curve
	 * https://math.stackexchange.com/questions/26846/is-there-an-explicit-form-for-cubic-b%C3%A9zier-curves
	 * https://www.gamedev.net/forums/topic/572263-bezier-curve-for-animation/
	 * https://math.stackexchange.com/questions/2571471/understanding-of-cubic-b%C3%A9zier-curves-in-one-dimension
	 */
	public static float cubicBezier1d(float anchor1, float anchor2, float progress)
	{
		float oneMinusP = 1f - progress;
		return 3f * oneMinusP * oneMinusP * progress * anchor1 + 3f * oneMinusP * progress * progress * anchor2 + progress * progress * progress;
	}
}