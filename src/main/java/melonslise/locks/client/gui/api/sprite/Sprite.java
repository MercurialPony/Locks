package melonslise.locks.client.gui.api.sprite;

import net.minecraft.client.gui.Gui;

public class Sprite
{
	public Texture texture;
	public float shiftX , shiftY, oldShiftX, oldShiftY, motionX, motionY;
	public int ticksX, ticksY;

	public Sprite(Texture texture)
	{
		this.texture = texture;
	}

	public void draw(Gui gui, float positionX, float positionY, float partialTick)
	{
		this.texture.draw(gui, positionX + this.oldShiftX + (this.shiftX - this.oldShiftX) * partialTick, positionY + this.oldShiftY + (this.shiftY - this.oldShiftY) * partialTick);
	}

	public void update()
	{
		this.moveX();
		this.moveY();
	}

	protected void moveX()
	{
		this.oldShiftX = this.shiftX;
		if(this.ticksX > 0)
		{
			this.shiftX += this.motionX;
			--this.ticksX;
			if(this.ticksX <= 0) this.motionX = 0F;
		}
		else this.shiftX += this.motionX;
	}

	protected void moveY()
	{
		this.oldShiftY = this.shiftY;
		if(this.ticksY > 0)
		{
			this.shiftY += this.motionY;
			--this.ticksY;
			if(this.ticksY <= 0) this.motionY = 0F;
		}
		else this.shiftY += this.motionY;
	}

	public void moveX(float motion, int ticks)
	{
		this.motionX = motion;
		this.ticksX = ticks;
	}

	public void moveY(float motion, int ticks)
	{
		this.motionY = motion;
		this.ticksY = ticks;
	}

	public void reset()
	{
		this.shiftX = this.shiftY = this.oldShiftX = this.oldShiftY = this.motionX = this.motionY = 0F;
		this.ticksX = this.ticksY = 0;
	}
}