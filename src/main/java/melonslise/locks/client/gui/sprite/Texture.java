package melonslise.locks.client.gui.sprite;

import melonslise.locks.client.util.LocksClientUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO Save start pos and gui instead of passing them each time.
@SideOnly(Side.CLIENT)
public class Texture
{
	public int startX, startY, width, height, canvasWidth, canvasHeight;

	public Texture(int startX, int startY, int width, int height, int canvasWidth, int canvasHeight)
	{
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
	}

	public void draw(float x, float y)
	{
		LocksClientUtil.drawTexturedRectangle(x, y, this.startX, this.startY, this.width, this.height, this.canvasWidth, this.canvasHeight);
	}
}