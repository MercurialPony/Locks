package melonslise.locks.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonElement;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.loot.LootTableManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

@Mixin(LootTableManager.class)
public class LootTableManagerMixin
{
	@Inject(at = @At("HEAD"), method = "apply(Ljava/util/Map;Lnet/minecraft/resources/IResourceManager;Lnet/minecraft/profiler/IProfiler;)V")
	private void apply(Map<ResourceLocation, JsonElement> map, IResourceManager mgr, IProfiler p, CallbackInfo ci)
	{
		LocksUtil.resourceManager = mgr;
	}
}