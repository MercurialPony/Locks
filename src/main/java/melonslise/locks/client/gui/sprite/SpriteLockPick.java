package melonslise.locks.client.gui.sprite;

public class SpriteLockPick extends SpriteRotatable
{
	public SpriteLockPick(Texture texture)
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
		if(this.rotation != 0F && this.ticksRotation == 0) this.rotation += 2F * - Math.signum(rotation);
	}
}