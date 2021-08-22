package melonslise.locks.client.gui;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import melonslise.locks.Locks;
import melonslise.locks.client.gui.sprite.SpringSprite;
import melonslise.locks.client.gui.sprite.Sprite;
import melonslise.locks.client.gui.sprite.Texture;
import melonslise.locks.client.gui.sprite.action.AccelerateAction;
import melonslise.locks.client.gui.sprite.action.FadeAction;
import melonslise.locks.client.gui.sprite.action.IAction;
import melonslise.locks.client.gui.sprite.action.MoveAction;
import melonslise.locks.client.gui.sprite.action.WaitAction;
import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.network.toserver.CheckPinPacket;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LockPickingGui extends GuiContainer
{
	public static final ITextComponent HINT = new TextComponentTranslation(Locks.ID + ".gui.lockpicking.open");

	public static final Texture
		FRONT_WALL_TEX = new Texture(6, 0, 4, 60, 48, 80),
		COLUMN_TEX = new Texture(10, 0, 8, 60, 48, 80),
		INNER_WALL_TEX = new Texture(18, 0, 4, 60, 48, 80),
		BACK_WALL_TEX = new Texture(22, 0, 4, 60, 48, 80),
		HANDLE_TEX = new Texture(26, 0, 19, 73, 48, 80),
		UPPER_PIN_TEX = new Texture(0, 0, 6, 8, 48, 80),
		LOCK_PICK_TEX = new Texture(0, 0, 160, 12, 160, 16);
	public static final Texture[] PIN_TUMBLER_TEX = new Texture[] {
		new Texture(0, 8, 6, 11, 48, 80),
		new Texture(0, 19, 6, 13, 48, 80),
		new Texture(0, 32, 6, 15, 48, 80) };
	public static final Texture[] SPRING_TEX = new Texture[] {
		new Texture(0, 57, 6, 23, 48, 80),
		new Texture(6, 60, 6, 20, 48, 80),
		new Texture(12, 64, 6, 16, 48, 80),
		new Texture(18, 69, 6, 11, 48, 80),
		new Texture(24, 73, 6, 7, 48, 80) };

	public final ResourceLocation lockTex;
	protected ResourceLocation pickTex;
	
	protected Collection<Sprite> sprites;
	protected Sprite lockPick, leftPickPart, rightPickPart;
	protected Sprite[] pinTumblers, upperPins, springs;
	
	public final BiConsumer<IAction<Sprite>, Sprite> unfreezeCb = (action, sprite) -> this.frozen = false;
	public final BiConsumer<IAction<Sprite>, Sprite> resetPickCb = (action, sprite) -> this.resetPick();
	
	public final int length;
	public final boolean pins[];
	public final EnumHand hand;

	protected int currPin;

	protected boolean frozen = true;
	
	protected boolean holdingLeft, holdingRight;
	
	
	public LockPickingGui(LockPickingContainer cont)
	{
		super(cont);
		this.length = cont.lockable.lock.getLength();
		this.pins = new boolean[this.length];
		this.hand = cont.hand;
		this.lockTex = getTextureFor(cont.lockable.stack);
		this.xSize = (FRONT_WALL_TEX.width + this.length * (COLUMN_TEX.width + INNER_WALL_TEX.width) + HANDLE_TEX.width);
		this.ySize = HANDLE_TEX.height;
		this.sprites = new ArrayDeque<>(this.length * 3 + 4);
		this.pinTumblers = new Sprite[this.length];
		this.upperPins = new Sprite[this.length];
		this.springs = new Sprite[this.length];
		for(int a = 0; a < this.pinTumblers.length; ++a)
		{
			int r = ThreadLocalRandom.current().nextInt(3);
			this.pinTumblers[a] = this.addSprite(new Sprite(PIN_TUMBLER_TEX[r]).position(FRONT_WALL_TEX.width + 1 + a * (COLUMN_TEX.width + INNER_WALL_TEX.width), 43 - PIN_TUMBLER_TEX[r].height));
			this.upperPins[a] = new Sprite(UPPER_PIN_TEX).position(FRONT_WALL_TEX.width + 1 + a * (COLUMN_TEX.width + INNER_WALL_TEX.width), 43 - PIN_TUMBLER_TEX[r].height - UPPER_PIN_TEX.height);
			this.springs[a] = this.addSprite(new SpringSprite(SPRING_TEX, this.upperPins[a]).position(FRONT_WALL_TEX.width + 1 + a * (COLUMN_TEX.width + INNER_WALL_TEX.width), 3));
			this.addSprite(this.upperPins[a]);
		}
		this.lockPick = this.addSprite(new Sprite(LOCK_PICK_TEX).position(0f, -4 + COLUMN_TEX.height - LOCK_PICK_TEX.height));
		this.resetPick();
		this.rightPickPart = this.addSprite(new Sprite(new Texture(0, 0, 0, 12, 160, 16)).position(-10f, this.lockPick.posY).alpha(0f));
		this.leftPickPart = this.addSprite(new Sprite(new Texture(0, 0, 0, 12, 160, 16)).position(0f, this.lockPick.posY).rotation(-30f, -10f, this.lockPick.posY + 13f).alpha(0f));
		this.holdingLeft = false;
		this.holdingRight = false;
		
		//Inflate sizes for UI scale
		this.xSize *= 2;
		this.ySize *= 2;
	}
	
	public static ResourceLocation getTextureFor(ItemStack stack)
	{
		return new ResourceLocation(Locks.ID, "textures/gui/" + stack.getItem().getRegistryName().getResourcePath() + ".png");
	}
	
	public Sprite addSprite(Sprite sprite)
	{
		this.sprites.add(sprite);
		return sprite;
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	// render(MatrixStack mtx, int mouseX, int mouseY, float partialTick)
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTick);
	}
	
	
	// protected void renderBg(MatrixStack mtx, float partialTick, int mouseX, int mouseY)
	@Override
	public void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) // TODO Final static constants for texture pos?
	{
		float pt = this.mc.getRenderPartialTicks(); // Don't know why, but partialTick param looks laggy af... Use getRenderPartialTicks instead.
		
		int cornerX = (this.width - xSize) / 2;
		int cornerY = (this.height - ySize) / 2;
		
		GlStateManager.color(1f, 1f, 1f, 1f); //TODO is this necessary?
		
		this.mc.getTextureManager().bindTexture(this.lockTex);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(cornerX, cornerY, 0f);
		GlStateManager.scale(2f, 2f, 2f);
		FRONT_WALL_TEX.draw(0f, 0f, 1f);
		
		for(int a = 0; a < this.length; ++a)
		{
			COLUMN_TEX.draw(FRONT_WALL_TEX.width + a * (COLUMN_TEX.width + INNER_WALL_TEX.width), 0f, 1f);
			if(a != this.length - 1)
				INNER_WALL_TEX.draw(FRONT_WALL_TEX.width + COLUMN_TEX.width + a * (COLUMN_TEX.width + INNER_WALL_TEX.width), 0f, 1f);
		}
		BACK_WALL_TEX.draw(this.length * (COLUMN_TEX.width + INNER_WALL_TEX.width), 0f, 1f);
		HANDLE_TEX.draw(BACK_WALL_TEX.width + this.length * (COLUMN_TEX.width + INNER_WALL_TEX.width), 2f, 1f);
		// FIXME right way??
		for(Sprite sprite : this.sprites)
		{
			if(sprite == this.lockPick)
			{
				this.mc.getTextureManager().bindTexture(this.pickTex); // FIXME fucking terrible
			}
			sprite.draw(pt);
		}
		GlStateManager.popMatrix();
	}

	//protected void renderLabels(MatrixStack mtx, int mouseX, int mouseY)
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		// Without shadow
		this.fontRenderer.drawString(LockPickingContainer.TITLE.getFormattedText(), 0, -this.fontRenderer.FONT_HEIGHT, 0xffffff);
		if(((LockPickingContainer) this.inventorySlots).isOpen())
			this.fontRenderer.drawString(HINT.getFormattedText(), this.xSize / 2 - this.fontRenderer.getStringWidth(HINT.getFormattedText()) / 2, this.ySize + 10, 0xffffff);
	}
	
	//public void tick()
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		updateLockPickSpeed();
		for(Sprite sprite : this.sprites)
			sprite.update();
		if(!this.frozen)
			this.boundLockPick();
		this.updatePickParts();
	}
	
	protected void updatePickParts()
	{
		this.rightPickPart.posY = this.lockPick.posY;
		this.rightPickPart.tex.width = 10 + (int) this.lockPick.posX + this.lockPick.tex.width;
		this.rightPickPart.tex.startX = this.rightPickPart.tex.canvasWidth - this.rightPickPart.tex.width;

		this.leftPickPart.posY = this.lockPick.posY;
		this.leftPickPart.tex.width = this.rightPickPart.tex.startX;
		this.leftPickPart.posX = -10 - this.leftPickPart.tex.width;
	}
	
	protected void boundLockPick()
	{
		this.lockPick.posX = 10 - LOCK_PICK_TEX.width + MathHelper.clamp(this.lockPick.posX - 10 + LOCK_PICK_TEX.width, 0, (this.length - 1) * (COLUMN_TEX.width + INNER_WALL_TEX.width));
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
		{
			this.holdingLeft = true;
			updateLockPickSpeed();
		}
		else if(code == this.mc.gameSettings.keyBindRight.getKeyCode())
		{
			this.holdingRight = true;
			updateLockPickSpeed();
		}
		else if(!this.frozen && code == this.mc.gameSettings.keyBindForward.getKeyCode() && !this.lockPick.isExecuting() && this.pullPin(this.getSelectedPin()))
		{
			this.lockPick.execute(MoveAction.at(0f, -2.5f).time(3), MoveAction.at(0f, 2.5f).time(3));
		}
		
	}

	public void keyReleased(int code)
	{
		if(code == this.mc.gameSettings.keyBindLeft.getKeyCode())
		{
			this.holdingLeft = false;
			updateLockPickSpeed();
		}
		else if(code == this.mc.gameSettings.keyBindRight.getKeyCode())
		{
			this.holdingRight = false;
			updateLockPickSpeed();
		}
	}
	
	protected void updateLockPickSpeed()
	{
		if(this.frozen)
			this.lockPick.speedX = 0;
		else if(this.holdingLeft && this.holdingRight)
			this.lockPick.speedX = 0;
		else if(this.holdingLeft)
			this.lockPick.speedX = -4;
		else if(this.holdingRight)
			this.lockPick.speedX = 4;
		else
			this.lockPick.speedX = 0;
	}

	protected int getSelectedPin()
	{
		return (int) ((this.lockPick.posX - 10 + LOCK_PICK_TEX.width) / (this.COLUMN_TEX.width + this.INNER_WALL_TEX.width) + 0.5f);
	}

	protected boolean pullPin(int pin)
	{
		if(this.pins[pin])
			return false;
		this.currPin = pin;
		LocksNetworks.MAIN.sendToServer(new CheckPinPacket((byte) pin));
		return true;
	}

	public void handlePin(boolean correct, boolean reset)
	{
		this.pinTumblers[this.currPin].execute(MoveAction.at(0f, -6f).time(2), MoveAction.at(0f, 6f).time(2));
		this.upperPins[this.currPin].execute(MoveAction.at(0f, -6f).time(2));
		if(correct)
		{
			this.pins[this.currPin] = true;
			this.upperPins[this.currPin].execute(MoveAction.to(this.upperPins[this.currPin], this.upperPins[this.currPin].posX, 29, 2));
		}
		else
			this.upperPins[this.currPin].execute(MoveAction.at(0f, 6f).time(2));
		if(reset)
			this.reset();
	}

	public void reset()
	{
		//this.lockPick.reset();
		for(int a = 0; a < this.pins.length; ++a)
			if(this.pins[a])
			{
				this.pins[a] = false;
				this.upperPins[a].execute(MoveAction.to(this.upperPins[a], this.upperPins[a].posX, this.pinTumblers[a].posY - UPPER_PIN_TEX.height, 2));
			}
		this.lockPick.alpha(0f);
		this.rightPickPart.alpha(1f).execute(WaitAction.ticks(10), FadeAction.to(this.rightPickPart, 0f, 4));
		this.leftPickPart.alpha(1f).execute(WaitAction.ticks(10), FadeAction.to(this.rightPickPart, 0f, 4).then(resetPickCb));
		this.frozen = true;
		updateLockPickSpeed();
	}

	public void resetPick()
	{
		this.pickTex = getTextureFor(Minecraft.getMinecraft().player.getHeldItem(this.hand));
		this.lockPick.position(-22 - LOCK_PICK_TEX.width, this.lockPick.posY).alpha(1f).execute(AccelerateAction.to(32f, 0f, 4, false).then(unfreezeCb));
	}
}