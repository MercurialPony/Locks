package melonslise.locks.client.gui.sprite.action;

import melonslise.locks.client.gui.sprite.Sprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WaitAction<S extends Sprite> extends TimedAction<S>
{
	public static <Z extends Sprite> WaitAction<Z> ticks(int ticks)
	{
		return (WaitAction<Z>) new WaitAction().time(ticks);
	}
}