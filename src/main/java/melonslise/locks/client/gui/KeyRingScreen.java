package melonslise.locks.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import melonslise.locks.common.container.KeyRingContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyRingScreen extends ContainerScreen<KeyRingContainer>
{
	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	public KeyRingScreen(KeyRingContainer container, PlayerInventory inv, ITextComponent title)
	{
		super(container, inv, title);
		//int i = 222;
		//int j = 114;
		this.ySize = 114 + container.rows * 18;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTick)
	{
		this.renderBackground();
		super.render(mouseX, mouseY, partialTick);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.font.drawString(this.title.getFormattedText(), 8f, 6f, 4210752);
		this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8f, (float)(this.ySize - 96 + 2), 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		int rows = this.getContainer().rows;
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		this.minecraft.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(i, j, 0, 0, this.xSize, rows * 18 + 17);
		this.blit(i, j + rows * 18 + 17, 0, 126, this.xSize, 96);
	}
}