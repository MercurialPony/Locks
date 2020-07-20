package melonslise.locks.client.gui.sprite;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LockPickSprite extends RotatableSprite
{
	public LockPickSprite(Texture texture)
	{
		super(texture);
	}

	@Override
	public void update()
	{
		super.update();
		this.rotateBack();
	}

	protected void rotateBack()
	{
		if(this.rotation != 0F && this.ticksRotation == 0)
			this.rotation += 2F * - Math.signum(rotation);
	}
}