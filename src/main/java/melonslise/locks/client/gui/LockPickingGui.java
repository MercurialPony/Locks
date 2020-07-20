package melonslise.locks.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import melonslise.locks.Locks;
import melonslise.locks.client.gui.sprite.LockPickSprite;
import melonslise.locks.client.gui.sprite.PinSprite;
import melonslise.locks.client.gui.sprite.Texture;
import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.network.toserver.CheckPinPacket;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LockPickingGui extends GuiContainer
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(Locks.ID, "textures/gui/lockpicking.png");
	public static final ITextComponent HINT = new TextComponentTranslation(Locks.ID + ".gui.lockpicking.open");

	protected Texture
		outerLock = new Texture(24, 0, 24, 120, 256, 256),
		innerLock = new Texture(0, 0, 24, 64, 256, 256),
		spring = new Texture(48, 48, 12, 24, 256, 256),
		edge = new Texture(0 ,0 , 12, 64, 256, 256),
		pin = new Texture(48, 0, 12, 48, 256, 256);
	protected LockPickSprite lockPick = new LockPickSprite(new Texture(60, 0, 192, 24, 256, 256));
	protected PinSprite[] pins;

	protected int length;

	protected int currentPin;

	public LockPickingGui(LockPickingContainer container)
	{
		super(container);
		this.setLength(container.lockable.lock.getLength());
	}

	public void setLength(int length)
	{
		this.length = length;
		this.xSize = 12 + this.length * 24 + 12;
		this.ySize = 120;
		this.pins = new PinSprite[length];
		for(int a = 0; a < this.pins.length; ++a)
			this.pins[a] = new PinSprite(this.pin);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTick);
	}

	// Don't know why, but partialTick param looks laggy af... Use getRenderPartialTicks instead.
	@Override
	public void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) // TODO Final static constants for texture pos?
	{
		float pt = this.mc.getRenderPartialTicks();
		GlStateManager.color(1f, 1f, 1f, 1f);
		this.mc.getTextureManager().bindTexture(TEXTURE);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		for(int a = 0; a < this.length + 1; ++a)
			this.outerLock.draw(cornerX + a * 24, cornerY);
		for(int a = 0; a < this.length; ++a)
			this.innerLock.draw(cornerX + a * 24 + 12, cornerY + 48);
		this.edge.draw(cornerX + this.length * 24 + 12, cornerY + 48);
		this.lockPick.draw(cornerX - 156, cornerY + 72, pt);
		for(int a = 0; a < this.pins.length; ++a)
		{
			this.spring.draw(cornerX + a * 24 + 18, cornerY);
			this.pins[a].draw(cornerX + a * 24 + 18, cornerY + 24, pt);
		}
		this.edge.draw(cornerX, cornerY + 48);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		// Without shadow
		this.fontRenderer.drawString(LockPickingContainer.TITLE.getFormattedText(), 0, -this.fontRenderer.FONT_HEIGHT, 0xffffff);
		if(((LockPickingContainer) this.inventorySlots).isOpen())
			this.fontRenderer.drawString(HINT.getFormattedText(), this.xSize / 2 - this.fontRenderer.getStringWidth(HINT.getFormattedText()) / 2, this.ySize + 10, 0xffffff);
		//this.drawCenteredString(this.font, I18n.format(title), this.width / 2, 10, 0xffffff);
		//if(this.getContainer().isOpen()) this.drawCenteredString(this.fontRenderer, I18n.format(hint), this.width / 2, cornerY + 96, 0xffffff);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
		this.lockPick.update();
		this.boundLockPick();
		for(int a = 0; a < this.length; ++a)
			this.pins[a].update();
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	public void handleKeyboardInput() throws IOException
	{
		super.handleKeyboardInput();
		int code = Keyboard.getEventKey();
		if(Keyboard.getEventKeyState())
			this.keyPressed(code);
		else
			this.keyReleased(code);
	}

	public void keyPressed(int code)
	{
		if(code == this.mc.gameSettings.keyBindLeft.getKeyCode())
			this.lockPick.motionX = -6;
		else if(code == this.mc.gameSettings.keyBindRight.getKeyCode())
			this.lockPick.motionX = 6;
		else if(code == this.mc.gameSettings.keyBindForward.getKeyCode() && this.lockPick.rotation == 0f)
		{
			this.rotateLockPick(-6);
			this.pullPin(this.getSelectedPin());
		}
	}

	public void keyReleased(int code)
	{
		if(code == this.mc.gameSettings.keyBindLeft.getKeyCode() || code == this.mc.gameSettings.keyBindRight.getKeyCode())
			this.lockPick.motionX = 0;
	}

	protected void boundLockPick()
	{
		this.lockPick.shiftX = MathHelper.clamp(this.lockPick.shiftX, 0, (this.length - 1) * 24);
	}

	protected void rotateLockPick(int deg)
	{
		// Angle = (angular velocity) * time.
		int motion = 2 * Integer.signum(deg);
		this.lockPick.rotate(motion, deg / motion);
	}

	protected int getSelectedPin()
	{
		return (int) ((this.lockPick.shiftX + 12F) / 24F);
	}

	protected void pullPin(int pin)
	{
		if(this.pins[pin].isPulled)
			return;
		this.currentPin = pin;
		LocksNetworks.MAIN.sendToServer(new CheckPinPacket((byte) pin));
	}

	public void handlePin(boolean correct, boolean reset)
	{
		this.pins[this.currentPin].moveY(-8, 3);
		if(correct)
			this.pins[this.currentPin].isPulled = true;
		if(reset)
			this.reset();
	}

	public void reset()
	{
		this.lockPick.reset();
		for(PinSprite pin : this.pins)
			pin.isPulled = false;
	}
}