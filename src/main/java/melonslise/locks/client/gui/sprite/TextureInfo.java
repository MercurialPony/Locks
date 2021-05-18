package melonslise.locks.client.gui.sprite;

import com.mojang.blaze3d.matrix.MatrixStack;

import melonslise.locks.client.util.LocksClientUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureInfo
{
	public int startX, startY, width, height, canvasWidth, canvasHeight;

	public TextureInfo(int startX, int startY, int width, int height, int canvasWidth, int canvasHeight)
	{
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
	}

	public void draw(MatrixStack mtx, float x, float y, float alpha)
	{
		LocksClientUtil.texture(mtx, x, y, this.startX, this.startY, this.width, this.height, this.canvasWidth, this.canvasHeight, alpha);
	}
}