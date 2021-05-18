package melonslise.locks.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootPool;

@Mixin(LootPool.class)
public interface LootPoolAccessor
{
	@Accessor
	List<LootEntry> getEntries();
}