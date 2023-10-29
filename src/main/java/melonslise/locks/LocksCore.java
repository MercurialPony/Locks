package melonslise.locks;

import melonslise.locks.init.ModItemTabs;
import melonslise.locks.init.ModItems;
import melonslise.locks.init.RegistryScannerV2;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocksCore implements ModInitializer
{
	public static final String ID = "locks";

	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static Identifier id(String name)
	{
		return new Identifier(ID, name);
	}


	@Override
	public void onInitialize()
	{
		RegistryScannerV2.register(ModItems.class, Registries.ITEM, ID);
		RegistryScannerV2.register(ModItemTabs.class, Registries.ITEM_GROUP, ID);
	}
}