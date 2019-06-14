package melonslise.locks.client.gui.sprite;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.AbstractGui;

public class SpriteRotatable extends Sprite
{
	public float rotation, oldRotation, motionRotation, originX = this.texture.width / 2, originY = this.texture.height / 2;;
	public int ticksRotation;

	public SpriteRotatable(Texture texture)
	{
		super(texture);
	}

	@Override
	public void draw(AbstractGui gui, float positionX, float positionY, float partialTick)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translatef(this.originX + this.shiftX, this.originY + this.shiftY, 0F);
		GlStateManager.rotatef(this.oldRotation + (this.rotation - this.oldRotation) * partialTick, 0F, 0F, 1F);
		GlStateManager.translatef(-this.originX - this.shiftX, -this.originY - this.shiftY, 0F);
		super.draw(gui, positionX, positionY, partialTick);
		GlStateManager.popMatrix();
	}

	@Override
	public void update()
	{
		super.update();
		this.rotate();
	}

	protected void rotate()
	{
		this.oldRotation = this.rotation;
		if(this.ticksRotation > 0)
		{
			this.rotation += this.motionRotation;
			--this.ticksRotation;
			if(this.ticksRotation <= 0) this.motionRotation = 0F;
		}
		else this.rotation += this.motionRotation;
	}

	public void rotate(float motion, int ticks)
	{
		this.motionRotation = motion;
		this.ticksRotation = ticks;
	}

	@Override
	public void reset()
	{
		super.reset();
		this.rotation = this.oldRotation = this.motionRotation = 0F;
		this.ticksRotation = 0;
	}
}