package melonslise.locks.client.gui.sprite;

import melonslise.locks.client.gui.api.sprite.Sprite;
import melonslise.locks.client.gui.api.sprite.Texture;

public class SpritePin extends Sprite
{
	public boolean isPulled = false;

	public SpritePin(Texture texture)
	{
		super(texture);
	}

	@Override
	public void update()
	{
		super.update();
		this.moveBack();
	}

	protected void moveBack()
	{
		if(!this.isPulled && this.shiftY != 0 && this.ticksY == 0) this.shiftY += 6F * -Math.signum(this.shiftY);
	}

	@Override
	public void reset()
	{
		super.reset();
		this.isPulled = false;
	}
}