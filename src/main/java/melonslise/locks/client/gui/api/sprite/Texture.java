package melonslise.locks.client.gui.api.sprite;

import net.minecraft.client.gui.Gui;

// TODO Custom texture size? Save start pos and gui instead of passing them each time.
public class Texture
{
	public int startX, startY, width, height;

	public Texture(int startX, int startY, int width, int height)
	{
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
	}

	public void draw(Gui gui, float positionX, float positionY)
	{
		gui.drawTexturedModalRect(positionX, positionY, this.startX, this.startY, this.width, this.height);
	}
}