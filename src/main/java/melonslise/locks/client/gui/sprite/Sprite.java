package melonslise.locks.client.gui.sprite;

import com.mojang.blaze3d.matrix.MatrixStack;

import melonslise.locks.client.util.LocksClientUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Sprite
{
	public Texture texture;
	public float shiftX , shiftY, oldShiftX, oldShiftY, motionX, motionY;
	public int ticksX, ticksY;

	public Sprite(Texture tex)
	{
		this.texture = tex;
	}

	public void draw(MatrixStack mtx, float x, float y, float partialTick)
	{
		this.texture.draw(mtx, x + LocksClientUtil.lerp(this.oldShiftX, this.shiftX, partialTick), y + LocksClientUtil.lerp(this.oldShiftY, this.shiftY, partialTick));
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
			if(this.ticksX <= 0)
				this.motionX = 0f;
		}
		else
			this.shiftX += this.motionX;
	}

	protected void moveY()
	{
		this.oldShiftY = this.shiftY;
		if(this.ticksY > 0)
		{
			this.shiftY += this.motionY;
			--this.ticksY;
			if(this.ticksY <= 0)
				this.motionY = 0f;
		}
		else
			this.shiftY += this.motionY;
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
		this.shiftX = this.shiftY = this.oldShiftX = this.oldShiftY = this.motionX = this.motionY = 0f;
		this.ticksX = this.ticksY = 0;
	}
}