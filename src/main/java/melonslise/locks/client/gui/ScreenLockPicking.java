package melonslise.locks.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import melonslise.locks.Locks;
import melonslise.locks.client.gui.sprite.SpriteLockPick;
import melonslise.locks.client.gui.sprite.SpritePin;
import melonslise.locks.client.gui.sprite.Texture;
import melonslise.locks.common.container.ContainerLockPicking;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.network.toServer.PacketCheckPin;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenLockPicking extends ContainerScreen<ContainerLockPicking>
{
	protected int length;

	public static final ResourceLocation textures = new ResourceLocation(Locks.ID, "textures/gui/lockpicking.png");
	public static final ITextComponent HINT = new TranslationTextComponent(Locks.ID + ".gui.lockpicking.open");

	protected Texture outerLock = new Texture(24, 0, 24, 120, 256, 256), innerLock = new Texture(0, 0, 24, 64, 256, 256), spring = new Texture(48, 48, 12, 24, 256, 256), edge = new Texture(0 ,0 , 12, 64, 256, 256), pin = new Texture(48, 0, 12, 48, 256, 256);
	protected SpriteLockPick lockPick = new SpriteLockPick(new Texture(60, 0, 192, 24, 256, 256));
	protected SpritePin[] pins;

	protected int currentPin;

	public ScreenLockPicking(ContainerLockPicking container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		this.setLength(container.lockable.lock.getLength());
	}

	public void setLength(int length)
	{
		this.length = length;
		this.xSize = 12 + this.length * 24 + 12;
		this.ySize = 120;
		this.pins = new SpritePin[length];
		for(int a = 0; a < this.pins.length; ++a) this.pins[a] = new SpritePin(this.pin);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) // TODO Final static constants for texture pos?
	{
		this.renderBackground();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		this.minecraft.getTextureManager().bindTexture(textures);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		for(int a = 0; a < this.length + 1; ++a) this.outerLock.draw(this, cornerX + a * 24, cornerY);
		for(int a = 0; a < this.length; ++a) this.innerLock.draw(this, cornerX + a * 24 + 12, cornerY + 48);
		this.edge.draw(this, cornerX + this.length * 24 + 12, cornerY + 48);
		this.lockPick.draw(this, cornerX - 156, cornerY + 72, partialTick);
		for(int a = 0; a < this.pins.length; ++a)
		{
			this.spring.draw(this, cornerX + a * 24 + 18, cornerY);
			this.pins[a].draw(this, cornerX + a * 24 + 18, cornerY + 24, partialTick);
		}
		this.edge.draw(this, cornerX, cornerY + 48);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.font.drawString(this.title.getFormattedText(), 0f, (float) -this.font.FONT_HEIGHT, 0xffffff);
		if(this.getContainer().isOpen()) this.font.drawString(HINT.getFormattedText(), (float) (this.xSize - this.font.getStringWidth(HINT.getFormattedText())) / 2f, (float) (this.ySize + 10), 0xffffff);
		//this.drawCenteredString(this.font, I18n.format(title), this.width / 2, 10, 0xffffff);
		//if(this.getContainer().isOpen()) this.drawCenteredString(this.fontRenderer, I18n.format(hint), this.width / 2, cornerY + 96, 0xffffff);
	}

	@Override
	public void tick()
	{
		super.tick();
		this.lockPick.update();
		this.boundLockPick();
		for(int a = 0; a < this.length; ++a) this.pins[a].update();
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	@Override
	public boolean keyPressed(int key, int scan, int modifier)
	{
		if(key == this.minecraft.gameSettings.keyBindLeft.getKey().getKeyCode())
		{
			this.lockPick.motionX = -4;
			return true;
		}
		else if(key == this.minecraft.gameSettings.keyBindRight.getKey().getKeyCode())
		{
			this.lockPick.motionX = 4;
			return true;
		}
		else if(key == this.minecraft.gameSettings.keyBindForward.getKey().getKeyCode() && this.lockPick.rotation == 0F)
		{
			this.rotateLockPick(-6);
			this.pullPin(this.getSelectedPin());
			return true;
		}
		return super.keyPressed(key, scan, modifier);
	}

	@Override
	public boolean keyReleased(int key, int scan, int modifier)
	{
		if(key == this.minecraft.gameSettings.keyBindLeft.getKey().getKeyCode())
		{
			this.lockPick.motionX = 0;
			return true;
		}
		else if(key == this.minecraft.gameSettings.keyBindRight.getKey().getKeyCode())
		{
			this.lockPick.motionX = 0;
			return true;
		}
		return super.keyReleased(key, scan, modifier);
	}

	protected void boundLockPick()
	{
		this.lockPick.shiftX = MathHelper.clamp(this.lockPick.shiftX, 0, (this.length - 1) * 24);
	}

	protected void rotateLockPick(int degrees)
	{
		// Angle = (angular velocity) * time.
		int motion = 2 * Integer.signum(degrees);
		this.lockPick.rotate(motion, degrees / motion);
	}

	protected int getSelectedPin()
	{
		return (int) ((this.lockPick.shiftX + 12F) / 24F);
	}

	protected void pullPin(int pin)
	{
		if(this.pins[pin].isPulled) return;
		this.currentPin = pin;
		LocksNetworks.MAIN.sendToServer(new PacketCheckPin((byte) pin));
	}

	public void handlePin(boolean correct, boolean reset)
	{
		this.pins[this.currentPin].moveY(-8, 3);
		if(correct) this.pins[this.currentPin].isPulled = true;
		if(reset) this.reset();
	}

	public void reset()
	{
		this.lockPick.reset();
		for(SpritePin pin : this.pins) pin.isPulled = false;
	}
}