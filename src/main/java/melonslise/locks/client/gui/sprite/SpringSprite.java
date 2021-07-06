package melonslise.locks.client.gui.sprite;

import melonslise.locks.client.util.LocksClientUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpringSprite extends Sprite
{
	public final Texture[] texs;
	public Sprite target;

	public SpringSprite(Texture[] texs, Sprite target)
	{
		super(texs[0]);
		this.texs = texs;
		this.target = target;
	}

	@Override
	public void draw(float partialTick)
	{
		for(Texture tex : this.texs)
			if(LocksClientUtil.lerp(this.target.oldPosY, this.target.posY, partialTick) < this.posY + tex.height)
				this.tex = tex;
		super.draw(partialTick);
	}
}
