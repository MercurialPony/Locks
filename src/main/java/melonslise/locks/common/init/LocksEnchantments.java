package melonslise.locks.common.init;

import java.util.ArrayList;
import java.util.List;

import melonslise.locks.Locks;
import melonslise.locks.common.enchantment.ComplexityEnchantment;
import melonslise.locks.common.enchantment.ShockingEnchantment;
import melonslise.locks.common.enchantment.SturdyEnchantment;
import melonslise.locks.common.item.LockItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.Item;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;

public class LocksEnchantments
{
	public static final EnumEnchantmentType LOCK_TYPE = EnumHelper.addEnchantmentType("LOCK", item -> item instanceof LockItem); // FIXME check if is in tag instead?
	
	public static final List<Enchantment> ENCHANTMENTS = new ArrayList<Enchantment>(3);
	
	public static final Enchantment
		SHOCKING = add("shocking", new ShockingEnchantment()),
		STURDY = add("sturdy", new SturdyEnchantment()),
		COMPLEXITY = add("complexity", new ComplexityEnchantment());
	
	public static void register(RegistryEvent.Register<Enchantment> event)
	{
		for(Enchantment ench : ENCHANTMENTS)
			event.getRegistry().register(ench);
	}
	
	public static Enchantment add(String name, Enchantment ench)
	{
		ENCHANTMENTS.add(ench.setRegistryName(Locks.ID, name).setName(Locks.ID + "." + name));
		return ench;
	}
}
