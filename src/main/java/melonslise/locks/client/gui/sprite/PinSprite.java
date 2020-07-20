package melonslise.locks.client.gui.sprite;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PinSprite extends Sprite
{
	public boolean isPulled = false;

	public PinSprite(Texture texture)
	{
		super(texture);
	}

	@Override
	public void update()
	{
		super.update();
		this.moveBack();
	}

	// FIXME not constant move back speed
	protected void moveBack()
	{
		if(!this.isPulled && this.shiftY != 0 && this.ticksY == 0)
			this.shiftY += 8f * -Math.signum(this.shiftY);
	}

	@Override
	public void reset()
	{
		super.reset();
		this.isPulled = false;
	}
}