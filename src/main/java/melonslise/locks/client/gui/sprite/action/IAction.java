package melonslise.locks.client.gui.sprite.action;

import java.util.function.BiConsumer;

import melonslise.locks.client.gui.sprite.Sprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IAction<S extends Sprite>
{
	boolean isFinished(S sprite);

	void update(S sprite);

	void finish(S sprite);

	IAction<S> then(BiConsumer<IAction<S>, S> cb);
}
