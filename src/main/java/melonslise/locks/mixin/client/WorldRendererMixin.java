package melonslise.locks.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;

import melonslise.locks.client.event.LocksClientForgeEvents;
import melonslise.locks.client.util.LocksClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin
{
	// Before first checkPoseStack call
	@Inject(at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/WorldRenderer.checkPoseStack(Lcom/mojang/blaze3d/matrix/MatrixStack;)V", ordinal = 0), method = "renderLevel(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V")
	private void renderLevel(MatrixStack mtx, float pt, long nanoTime, boolean renderOutline, ActiveRenderInfo cam, GameRenderer gr, LightTexture lightTex, Matrix4f proj, CallbackInfo ci)
	{
		LocksClientForgeEvents.renderLocks(mtx, Minecraft.getInstance().renderBuffers().bufferSource(), LocksClientUtil.getClippingHelper(mtx, proj), pt);
	}
}