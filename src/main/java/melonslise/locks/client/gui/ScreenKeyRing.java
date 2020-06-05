package melonslise.locks.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import melonslise.locks.common.container.ContainerKeyRing;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenKeyRing extends ContainerScreen<ContainerKeyRing>
{
	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	public ScreenKeyRing(ContainerKeyRing container, PlayerInventory inventory, ITextComponent title)
	{
		super(container, inventory, title);
		int i = 222;
		int j = 114;
		this.ySize = 114 + container.rows * 18;
	}

	@Override
	public void render(int p_render_1_, int p_render_2_, float p_render_3_)
	{
		this.renderBackground();
		super.render(p_render_1_, p_render_2_, p_render_3_);
		this.renderHoveredToolTip(p_render_1_, p_render_2_);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.font.drawString(this.title.getFormattedText(), 8.0F, 6.0F, 4210752);
		this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F, (float)(this.ySize - 96 + 2), 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		int rows = this.getContainer().rows;
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(i, j, 0, 0, this.xSize, rows * 18 + 17);
		this.blit(i, j + rows * 18 + 17, 0, 126, this.xSize, 96);
	}
}