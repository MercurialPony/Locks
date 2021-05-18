package melonslise.locks.client.gui;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import melonslise.locks.Locks;
import melonslise.locks.client.gui.sprite.SpringSprite;
import melonslise.locks.client.gui.sprite.Sprite;
import melonslise.locks.client.gui.sprite.TextureInfo;
import melonslise.locks.client.gui.sprite.action.AccelerateAction;
import melonslise.locks.client.gui.sprite.action.FadeAction;
import melonslise.locks.client.gui.sprite.action.IAction;
import melonslise.locks.client.gui.sprite.action.MoveAction;
import melonslise.locks.client.gui.sprite.action.WaitAction;
import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksNetwork;
import melonslise.locks.common.network.toserver.TryPinPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LockPickingScreen extends ContainerScreen<LockPickingContainer>
{
	public static final ITextComponent HINT = new TranslationTextComponent(Locks.ID + ".gui.lockpicking.open");

	public static final TextureInfo
		FRONT_WALL_TEX = new TextureInfo(6, 0, 4, 60, 48, 80),
		COLUMN_TEX = new TextureInfo(10, 0, 8, 60, 48, 80),
		INNER_WALL_TEX = new TextureInfo(18, 0, 4, 60, 48, 80),
		BACK_WALL_TEX = new TextureInfo(22, 0, 4, 60, 48, 80),
		HANDLE_TEX = new TextureInfo(26, 0, 19, 73, 48, 80),
		UPPER_PIN_TEX = new TextureInfo(0, 0, 6, 8, 48, 80),
		LOCK_PICK_TEX = new TextureInfo(0, 0, 160, 12, 160, 16);
	public static final TextureInfo[] PIN_TUMBLER_TEX = new TextureInfo[] {
		new TextureInfo(0, 8, 6, 11, 48, 80),
		new TextureInfo(0, 19, 6, 13, 48, 80),
		new TextureInfo(0, 32, 6, 15, 48, 80) };
	public static final TextureInfo[] SPRING_TEX = new TextureInfo[] {
		new TextureInfo(0, 57, 6, 23, 48, 80),
		new TextureInfo(6, 60, 6, 20, 48, 80),
		new TextureInfo(12, 64, 6, 16, 48, 80),
		new TextureInfo(18, 69, 6, 11, 48, 80),
		new TextureInfo(24, 73, 6, 7, 48, 80) };

	public final ResourceLocation lockTex;
	protected ResourceLocation pickTex;

	protected Collection<Sprite> sprites;
	protected Sprite lockPick, leftPickPart, rightPickPart;
	protected Sprite[] pinTumblers, upperPins, springs;

	public final BiConsumer<IAction<Sprite>, Sprite> unfreezeCb = (action, sprite) -> this.frozen = false;
	public final BiConsumer<IAction<Sprite>, Sprite> resetPickCb = (action, sprite) -> this.resetPick();

	public final int length;
	public final boolean pins[];
	public final Hand hand;

	protected int currPin;

	protected boolean frozen = true;

	public LockPickingScreen(LockPickingContainer cont, PlayerInventory inv, ITextComponent title)
	{
		super(cont, inv, title);
		this.length = cont.lockable.lock.getLength();
		this.pins = new boolean[this.length];
		this.hand = cont.hand;
		this.lockTex = getTextureFor(cont.lockable.stack);
		this.imageWidth = (FRONT_WALL_TEX.width + this.length * (COLUMN_TEX.width + INNER_WALL_TEX.width)) * 2;
		this.imageHeight = HANDLE_TEX.height * 2;
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
		this.rightPickPart = this.addSprite(new Sprite(new TextureInfo(0, 0, 0, 12, 160, 16)).position(-10f, this.lockPick.posY).alpha(0f));
		this.leftPickPart = this.addSprite(new Sprite(new TextureInfo(0, 0, 0, 12, 160, 16)).position(0f, this.lockPick.posY).rotation(-30f, -10f, this.lockPick.posY + 13f).alpha(0f));
	}

	public static ResourceLocation getTextureFor(ItemStack stack)
	{
		return new ResourceLocation(Locks.ID, "textures/gui/" + stack.getItem().getRegistryName().getPath() + ".png");
	}

	public Sprite addSprite(Sprite sprite)
	{
		this.sprites.add(sprite);
		return sprite;
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	@Override
	public void render(MatrixStack mtx, int mouseX, int mouseY, float partialTick)
	{
		this.renderBackground(mtx);
		super.render(mtx, mouseX, mouseY, partialTick);
	}

	@Override
	protected void renderBg(MatrixStack mtx, float partialTick, int mouseX, int mouseY)
	{
		float pt = this.minecraft.getFrameTime(); // idk why, but partialTick looks laggy AF... Use getFrameTime instead!
		int cornerX = (this.width - this.imageWidth) / 2;
		int cornerY = (this.height - this.imageHeight) / 2;

		this.minecraft.getTextureManager().bind(this.lockTex);

		mtx.pushPose();
		mtx.translate(cornerX, cornerY, 0f);
		mtx.scale(2f, 2f, 2f);
		FRONT_WALL_TEX.draw(mtx, 0f, 0f, 1f);

		for(int a = 0; a < this.length; ++a)
		{
			COLUMN_TEX.draw(mtx, FRONT_WALL_TEX.width + a * (COLUMN_TEX.width + INNER_WALL_TEX.width), 0f, 1f);
			if(a != this.length - 1)
				INNER_WALL_TEX.draw(mtx, FRONT_WALL_TEX.width + COLUMN_TEX.width + a * (COLUMN_TEX.width + INNER_WALL_TEX.width), 0f, 1f);
		}
		BACK_WALL_TEX.draw(mtx, this.length * (COLUMN_TEX.width + INNER_WALL_TEX.width), 0f, 1f);
		HANDLE_TEX.draw(mtx, BACK_WALL_TEX.width + this.length * (COLUMN_TEX.width + INNER_WALL_TEX.width), 2f, 1f);
		// FIXME right way??
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		for(Sprite sprite : this.sprites)
		{
			if(sprite == this.lockPick)
				this.minecraft.getTextureManager().bind(this.pickTex); // FIXME fucking terrible
			sprite.draw(mtx, pt);
		}
		mtx.popPose();
	}

	@Override
	protected void renderLabels(MatrixStack mtx, int mouseX, int mouseY)
	{
		// Without shadow
		this.font.draw(mtx, this.title, 0f, -this.font.lineHeight, 0xffffff);
		if(this.getMenu().isOpen())
			this.font.draw(mtx, HINT, (this.imageWidth - this.font.width(HINT)) / 2f, this.imageHeight + 10f, 0xffffff);
	}

	@Override
	public void tick()
	{
		super.tick();
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
	public boolean keyPressed(int key, int scan, int modifier)
	{
		if(this.frozen)
			return super.keyPressed(key, scan, modifier);;
		if(key == this.minecraft.options.keyLeft.getKey().getValue())
			this.lockPick.speedX = -4;
		else if(key == this.minecraft.options.keyRight.getKey().getValue())
			this.lockPick.speedX = 4;
		else if(key == this.minecraft.options.keyUp.getKey().getValue() && !this.lockPick.isExecuting() && this.pullPin(this.getSelectedPin()))
			this.lockPick.execute(MoveAction.at(0f, -2.5f).time(3), MoveAction.at(0f, 2.5f).time(3));
		return super.keyPressed(key, scan, modifier);
	}

	@Override
	public boolean keyReleased(int key, int scan, int modifier)
	{
		if(this.frozen)
			return super.keyReleased(key, scan, modifier);
		if(key == this.minecraft.options.keyLeft.getKey().getValue() || key == this.minecraft.options.keyRight.getKey().getValue())
			this.lockPick.speedX = 0;
		return super.keyReleased(key, scan, modifier);
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
		LocksNetwork.MAIN.sendToServer(new TryPinPacket((byte) pin));
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
	}

	public void resetPick()
	{
		this.pickTex = getTextureFor(Minecraft.getInstance().player.getItemInHand(this.hand));
		this.lockPick.position(-22 - LOCK_PICK_TEX.width, this.lockPick.posY).alpha(1f).execute(AccelerateAction.to(32f, 0f, 4, false).then(unfreezeCb));
	}
}