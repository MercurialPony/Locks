package melonslise.locks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

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

	public KeyRingScreen(KeyRingContainer cont, PlayerInventory inv, ITextComponent title)
	{
		super(cont, inv, title);
		this.imageHeight = 114 + cont.rows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void render(MatrixStack mtx, int mouseX, int mouseY, float partialTick)
	{
		this.renderBackground(mtx);
		super.render(mtx, mouseX, mouseY, partialTick);
		this.renderTooltip(mtx, mouseX, mouseY);
	}

	@Override
	protected void renderBg(MatrixStack mtx, float partialTick, int mouseX, int mouseY)
	{
		int rows = this.getMenu().rows;
		this.minecraft.getTextureManager().bind(TEXTURE);
		int cornerX = (this.width - this.imageWidth) / 2;
		int cornerY = (this.height - this.imageHeight) / 2;
		this.blit(mtx, cornerX, cornerY, 0, 0, this.imageWidth, rows * 18 + 17);
		this.blit(mtx, cornerX, cornerY + rows * 18 + 17, 0, 126, this.imageWidth, 96);
	}
}