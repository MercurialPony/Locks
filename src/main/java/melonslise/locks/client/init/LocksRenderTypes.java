package melonslise.locks.client.init;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import melonslise.locks.Locks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

public final class LocksRenderTypes extends RenderType
{
	public LocksRenderTypes(String name, VertexFormat format, int mode, int bufferSize, boolean useDelegate, boolean sorting, Runnable setup, Runnable clear)
	{
		super(name, format, mode, bufferSize, useDelegate, sorting, setup, clear);
	}

	public static final RenderType OVERLAY_LINES = makeType(Locks.ID + ".overlay_lines", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256, State.getBuilder()
		.line(new LineState(OptionalDouble.empty()))
		.layer(field_239235_M_)
		.transparency(TRANSLUCENT_TRANSPARENCY)
		.depthTest(DEPTH_ALWAYS)
		.writeMask(COLOR_WRITE)
		.build(false));
}