package melonslise.locks.client.gui.sprite.action;

import melonslise.locks.client.gui.sprite.Sprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FadeAction<S extends Sprite> extends TimedAction<S>
{
	public float fadeSpeed;

	private FadeAction(float fadeSpeed)
	{
		this.fadeSpeed = fadeSpeed;
	}

	public static <Z extends Sprite> FadeAction<Z> at(float fadeSpeed)
	{
		return new FadeAction(fadeSpeed);
	}

	public static <Z extends Sprite> FadeAction<Z> to(float delta, int ticks)
	{
		return (FadeAction<Z>) at(delta / ticks).time(ticks);
	}

	public static <Z extends Sprite> FadeAction<Z> to(Sprite sprite, float alpha, int ticks)
	{
		return to(alpha - sprite.alpha, ticks);
	}

	@Override
	public void update(S sprite)
	{
		super.update(sprite);
		sprite.alpha += this.fadeSpeed;
	}
}