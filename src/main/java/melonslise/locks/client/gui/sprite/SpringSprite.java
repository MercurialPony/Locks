package melonslise.locks.client.gui.sprite;

import com.mojang.blaze3d.matrix.MatrixStack;

import melonslise.locks.client.util.LocksClientUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpringSprite extends Sprite
{
	public final TextureInfo[] texs;
	public Sprite target;

	public SpringSprite(TextureInfo[] texs, Sprite target)
	{
		super(texs[0]);
		this.texs = texs;
		this.target = target;
	}

	@Override
	public void draw(MatrixStack mtx, float partialTick)
	{
		for(TextureInfo tex : this.texs)
			if(LocksClientUtil.lerp(this.target.oldPosY, this.target.posY, partialTick) < this.posY + tex.height)
				this.tex = tex;
		super.draw(mtx, partialTick);
	}
}