package melonslise.locks.client.gui.sprite;

import com.mojang.blaze3d.matrix.MatrixStack;

import melonslise.locks.client.util.LocksClientUtil;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RotatableSprite extends Sprite
{
	public float rotation, oldRotation, motionRotation, originX = this.texture.width / 2, originY = this.texture.height / 2;;
	public int ticksRotation;

	public RotatableSprite(Texture tex)
	{
		super(tex);
	}

	@Override
	public void draw(MatrixStack mtx, float x, float y, float partialTick)
	{
		mtx.push();
		mtx.translate(this.originX + this.shiftX, this.originY + this.shiftY, 0d);
		mtx.rotate(Vector3f.ZP.rotationDegrees(LocksClientUtil.lerp(this.oldRotation, this.rotation, partialTick)));
		mtx.translate(-this.originX - this.shiftX, -this.originY - this.shiftY, 0d);
		super.draw(mtx, x, y, partialTick);
		mtx.pop();
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