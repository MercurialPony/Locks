package melonslise.locks.client.gui.sprite;

import java.util.ArrayDeque;
import java.util.Queue;

import melonslise.locks.client.gui.sprite.action.IAction;
import melonslise.locks.client.util.LocksClientUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Sprite
{
	private Queue<IAction> actions = new ArrayDeque<>(4);
	public Texture tex;
	public float posX , posY, oldPosX, oldPosY, speedX, speedY, rot, oldRot, rotSpeed, originX, originY, alpha = 1f, oldAlpha = 1f;

	public Sprite(Texture tex)
	{
		this.tex = tex;
	}

	public Sprite position(float posX, float posY)
	{
		this.posX = this.oldPosX = posX;
		this.posY = this.oldPosY = posY;
		return this;
	}

	public Sprite rotation(float rot, float originX, float originY)
	{
		this.rot = this.oldRot = rot;
		this.originX = originX;
		this.originY = originY;
		return this;
	}

	public Sprite alpha(float alpha)
	{
		this.alpha = this.oldAlpha = alpha;
		return this;
	}

	public void execute(IAction... actions)
	{
		for(IAction action : actions)
			this.actions.offer(action);
	}

	public boolean isExecuting()
	{
		return !this.actions.isEmpty();
	}

	// FIXME new quat obj every frame? JFC
	public void draw(float partialTick)
	{
		if(this.alpha <= 0f)
			return;
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.originX, this.originY, 0f);
		GlStateManager.rotate(LocksClientUtil.lerp(this.oldRot, this.rot, partialTick), 0F, 0F, 1F);
		GlStateManager.translate(-this.originX, -this.originY, 0f);
		this.tex.draw(LocksClientUtil.lerp(this.oldPosX, this.posX, partialTick), LocksClientUtil.lerp(this.oldPosY, this.posY, partialTick), LocksClientUtil.lerp(this.oldAlpha, this.alpha, partialTick));
		GlStateManager.popMatrix();
	}

	public void update()
	{
		this.oldPosX = this.posX;
		this.oldPosY = this.posY;
		this.oldRot = this.rot;
		this.oldAlpha = this.alpha;
		this.posX += this.speedX;
		this.posY += this.speedY;
		this.rot += this.rotSpeed;
		IAction action = this.actions.peek();
		if(action == null)
			return;
		if(action.isFinished(this))
			this.actions.poll().finish(this);
		else
			action.update(this);
	}
}