package melonslise.locks.client.init;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import melonslise.locks.Locks;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LocksRenderTypes extends RenderType
{
	// FIXME this still has depth for some reason. As suggested we could try to create a custom DepthTestState which clears GL_DEPTH_BUFFER_BIT on setup, but thats not possible without AT or reflect...
	public static final RenderType OVERLAY_LINES = RenderType.create(Locks.ID + ".overlay_lines", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256, RenderType.State.builder()
		.setLineState(new LineState(OptionalDouble.empty()))
		.setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
		.setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
		.setDepthTestState(RenderState.NO_DEPTH_TEST)
		.setWriteMaskState(RenderState.COLOR_WRITE)
		.createCompositeState(false));

	private LocksRenderTypes(String name, VertexFormat format, int mode, int bufSize, boolean useDelegate, boolean sorting, Runnable setup, Runnable clear)
	{
		super(name, format, mode, bufSize, useDelegate, sorting, setup, clear);
	}
}