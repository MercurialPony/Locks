package melonslise.locks.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import melonslise.locks.LocksCore;
import melonslise.locks.client.gui.api.sprite.Texture;
import melonslise.locks.client.gui.sprite.SpriteLockPick;
import melonslise.locks.client.gui.sprite.SpritePin;
import melonslise.locks.common.container.ContainerLockPicking;
import melonslise.locks.common.network.LocksNetworks;
import melonslise.locks.common.network.server.MessageCheckPin;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO Smaller tex size
// TODO cast primitives (int, float, etc)
// TODO Move pick with mouse too
@SideOnly(Side.CLIENT)
public class GuiLockPicking extends GuiContainer // TODO EVENTS
{
	protected int length;

	public static final ResourceLocation textures = new ResourceLocation(LocksCore.ID, "textures/gui/lockpicking.png");
	public static final String title = LocksUtilities.prefixLocks("gui.lockpicking.title");
	protected int textureWidth, textureHeight;

	protected Texture outerLock = new Texture(24, 0, 24, 120), innerLock = new Texture(0, 0, 24, 64), spring = new Texture(48, 48, 12, 24), edge = new Texture(0 ,0 , 12, 64), pin = new Texture(48, 0, 12, 48);
	protected SpriteLockPick lockPick = new SpriteLockPick(new Texture(60, 0, 192, 24));
	protected SpritePin[] pins;

	protected int currentPin;

	public GuiLockPicking(ContainerLockPicking container)
	{
		super(container);
		this.setLength(container.lockable.lock.getLength());
	}

	public void setLength(int length)
	{
		this.length = length;
		this.textureWidth = 12 + this.length * 24 + 12;
		this.textureHeight = 64;
		this.pins = new SpritePin[length];
		for(int a = 0; a < this.pins.length; ++a) this.pins[a] = new SpritePin(this.pin);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) // TODO Final static constants for texture pos?
	{
		this.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(textures);
		int cornerX = (this.width - this.textureWidth) / 2;
		int cornerY = (this.height - this.textureHeight) / 2;
		for(int a = 0; a < this.length + 1; ++a) this.outerLock.draw(this, cornerX + a * 24, cornerY - 48);
		for(int a = 0; a < this.length; ++a) this.innerLock.draw(this, cornerX + a * 24 + 12, cornerY);
		this.edge.draw(this, cornerX + this.length * 24 + 12, cornerY);
		this.lockPick.draw(this, cornerX - 156, cornerY + 24, partialTick);
		for(int a = 0; a < this.pins.length; ++a)
		{
			this.spring.draw(this, cornerX + a * 24 + 18, cornerY - 48);
			this.pins[a].draw(this, cornerX + a * 24 + 18, cornerY - 24, partialTick);
		}
		this.edge.draw(this, cornerX, cornerY);
		this.drawCenteredString(this.fontRenderer, I18n.format(title), this.width / 2, 10, 0xffffff);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
		this.lockPick.update();
		this.boundLockPick();
		for(int a = 0; a < this.length; ++a) this.pins[a].update();
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
		char key = Keyboard.getEventCharacter();
		int code = Keyboard.getEventKey();
		if(Keyboard.getEventKeyState()) this.keyPressed(key, code);
		else this.keyReleased(key, code);
	}

	protected void keyPressed(char key, int code)
	{
		if(code == mc.gameSettings.keyBindLeft.getKeyCode()) this.lockPick.motionX = -4;
		else if(code == mc.gameSettings.keyBindRight.getKeyCode()) this.lockPick.motionX = 4;
		else if(code == mc.gameSettings.keyBindForward.getKeyCode() && this.lockPick.rotation == 0F)
		{
			this.rotateLockPick(-6);
			this.pullPin(this.getSelectedPin());
		}
	}

	protected void keyReleased(char key, int code)
	{
		if(code == mc.gameSettings.keyBindLeft.getKeyCode()) this.lockPick.motionX = 0;
		if(code == mc.gameSettings.keyBindRight.getKeyCode()) this.lockPick.motionX = 0;
	}

	protected void boundLockPick()
	{
		this.lockPick.shiftX = MathHelper.clamp(this.lockPick.shiftX, 0, (this.length - 1) * 24);
	}

	protected void rotateLockPick(int degrees)
	{
		 // Uses a simple physics formula. Angle = (angular velocity) * time.
		int motion = 2 * Integer.signum(degrees);
		this.lockPick.rotate(motion, degrees/motion);
	}

	protected int getSelectedPin()
	{
		return (int) ((this.lockPick.shiftX + 12F) / 24F);
	}

	protected void pullPin(int pin)
	{
		if(this.pins[pin].isPulled) return;
		this.currentPin = pin;
		LocksNetworks.network.sendToServer(new MessageCheckPin(pin));
	}

	public void handlePin(boolean correct)
	{
		this.pins[this.currentPin].moveY(-8, 3);
		if(!correct) return;
		this.pins[this.currentPin].isPulled = true;
	}

	public void reset()
	{
		this.lockPick.reset();
		for(SpritePin pin : this.pins) pin.isPulled = false;
	}
}