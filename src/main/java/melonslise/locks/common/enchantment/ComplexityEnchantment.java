package melonslise.locks.common.enchantment;

import melonslise.locks.common.init.LocksEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;

public class ComplexityEnchantment extends Enchantment
{
	public ComplexityEnchantment()
	{
		super(Rarity.VERY_RARE, LocksEnchantments.LOCK_TYPE, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMinCost(int level)
	{
		return 7 + level * 10;
	}

	@Override
	public int getMaxCost(int level)
	{
		return this.getMinCost(level) + 15;
	}

	@Override
	public int getMaxLevel()
	{
		return 3;
	}
}