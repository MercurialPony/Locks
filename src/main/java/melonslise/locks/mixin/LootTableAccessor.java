package melonslise.locks.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;

@Mixin(LootTable.class)
public interface LootTableAccessor
{
	@Accessor
	List<LootPool> getPools();
}