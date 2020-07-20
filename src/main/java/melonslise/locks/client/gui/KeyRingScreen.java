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

	public KeyRingScreen(KeyRingContainer container, PlayerInventory inv, ITextComponent title)
	{
		super(container, inv, title);
		//int i = 222;
		//int j = 114;
		this.ySize = 114 + container.rows * 18;
		this.field_238745_s_ = this.ySize - 94;
	}

	@Override
	public void render(MatrixStack mtx, int mouseX, int mouseY, float partialTick)
	{
		this.renderBackground(mtx);
		super.render(mtx, mouseX, mouseY, partialTick);
		this.func_230459_a_(mtx, mouseX, mouseY);
	}

	@Override
	protected void func_230450_a_ (MatrixStack mtx, float partialTick, int mouseX, int mouseY)
	{
		int rows = this.getContainer().rows;
		//GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(mtx, i, j, 0, 0, this.xSize, rows * 18 + 17);
		this.blit(mtx, i, j + rows * 18 + 17, 0, 126, this.xSize, 96);
	}
}