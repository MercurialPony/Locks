package melonslise.locks.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

public class LockItem extends Item
{
	private final int length;
	private final int enchantmentValue;
	private final int explosionResistance;

	public LockItem(int length, int enchantmentValue, int explosionResistance, Settings settings)
	{
		super(settings);
		this.length = length;
		this.enchantmentValue = enchantmentValue;
		this.explosionResistance = explosionResistance;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		return super.useOnBlock(context);
	}
}