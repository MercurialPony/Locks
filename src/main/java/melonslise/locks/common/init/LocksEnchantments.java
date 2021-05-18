package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.enchantment.ComplexityEnchantment;
import melonslise.locks.common.enchantment.ShockingEnchantment;
import melonslise.locks.common.enchantment.SturdyEnchantment;
import melonslise.locks.common.item.LockItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class LocksEnchantments
{
	public static final EnchantmentType LOCK_TYPE = EnchantmentType.create("LOCK", item -> item instanceof LockItem); // FIXME check if is in tag instead?

	public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Locks.ID);

	public static final RegistryObject<Enchantment>
		SHOCKING = add("shocking", new ShockingEnchantment()),
		STURDY = add("sturdy", new SturdyEnchantment()),
		COMPLEXITY = add("complexity", new ComplexityEnchantment());

	private LocksEnchantments() {}

	public static void register()
	{
		ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public static RegistryObject<Enchantment> add(String name, Enchantment ench)
	{
		return ENCHANTMENTS.register(name, () -> ench);
	}
}