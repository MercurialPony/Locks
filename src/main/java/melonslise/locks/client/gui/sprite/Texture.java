package melonslise.locks.client.gui.sprite;

import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.gui.AbstractGui;

// TODO Save start pos and gui instead of passing them each time.
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

	public void draw(AbstractGui gui, float positionX, float positionY)
	{
		LocksUtilities.drawTexturedRectangle(positionX, positionY, this.startX, this.startY, this.width, this.height, canvasWidth, canvasHeight);
	}
}