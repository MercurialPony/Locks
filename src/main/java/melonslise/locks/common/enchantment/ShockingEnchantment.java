package melonslise.locks.common.enchantment;

import melonslise.locks.common.init.LocksEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;

public class ShockingEnchantment extends Enchantment
{
	public ShockingEnchantment()
	{
		super(Rarity.UNCOMMON, LocksEnchantments.LOCK_TYPE, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMinCost(int level)
	{
		return 2 + (level - 1) * 9;
	}

	@Override
	public int getMaxCost(int level)
	{
		return this.getMinCost(level) + 30;
	}

	@Override
	public int getMaxLevel()
	{
		return 5;
	}
}