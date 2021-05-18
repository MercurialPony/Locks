package melonslise.locks.common.enchantment;

import melonslise.locks.common.init.LocksEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;

public class SturdyEnchantment extends Enchantment
{
	public SturdyEnchantment()
	{
		super(Rarity.RARE, LocksEnchantments.LOCK_TYPE, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMinCost(int level)
	{
		return 5 + (level - 1) * 15;
	}

	@Override
	public int getMaxCost(int level)
	{
		return 50;
	}

	@Override
	public int getMaxLevel()
	{
		return 3;
	}
}