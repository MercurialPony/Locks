package melonslise.locks.client.gui.sprite.action;

import java.util.function.BiConsumer;

import melonslise.locks.client.gui.sprite.Sprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IAction<S extends Sprite>
{
	boolean isFinished(S sprite);

	void update(S sprite);

	void finish(S sprite);

	IAction<S> then(BiConsumer<IAction<S>, S> cb);
}