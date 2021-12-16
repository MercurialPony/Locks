package melonslise.locks.common.enchantment;

import melonslise.locks.common.init.LocksEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ComplexityEnchantment extends Enchantment
{
	public ComplexityEnchantment()
	{
		super(Rarity.VERY_RARE, LocksEnchantments.LOCK_TYPE, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
	}
	
	@Override
	public int getMinEnchantability(int level)
	{
		return 7 + level * 10;
	}

	@Override
	public int getMaxEnchantability(int level)
	{
		return this.getMinEnchantability(level) + 15;
	}

	@Override
	public int getMaxLevel()
	{
		return 3;
	}
}
