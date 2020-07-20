package melonslise.locks.client.gui.sprite;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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