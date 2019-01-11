package melonslise.locks.client.gui;

import melonslise.locks.common.container.ContainerKeyRing;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiKeyRing extends GuiContainer
{
	public static final ResourceLocation textures = new ResourceLocation("textures/gui/container/generic_54.png");
	public final int rows;
	public String title1, title2;

	public GuiKeyRing(ContainerKeyRing container)
	{
		super(container);
		// TODO this.allowUserInput = false;
		this.rows = container.rows;
		this.title1 = container.stack.getDisplayName();
		this.title2 = container.player.inventory.getDisplayName().getUnformattedText();
		this.ySize = 114 + container.rows * 18;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTick);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		this.fontRenderer.drawString(this.title1, 8, 6, 4210752);
		this.fontRenderer.drawString(this.title2, 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
        GlStateManager.color(1F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(textures);
        int cornerX = (this.width - this.xSize) / 2;
        int cornerY = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.rows * 18 + 17);
        this.drawTexturedModalRect(cornerX, cornerY + this.rows * 18 + 17, 0, 126, this.xSize, 96);
	}	
}