package melonslise.locks.init;

import com.google.common.reflect.ClassPath;
import melonslise.locks.LocksCore;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/*
 * WARNING: This is an abomination. Do not use!
 *
 * Automatically discovers and registers all classes marked with @RegistryContainer in the specified directory
 * First, the registries in Registries.class are indexed by their generic type
 * Second, all classes marked with the annotation are discovered in the given package
 * Third, these classes are loaded and initialized in the given order after which their static fields are automatically registered
 */
@Deprecated
public class RegistryScannerV1
{
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Deprecated
	public @interface RegistryContainer
	{
		Class<?> value();
	}

	private final Map<Class<?>, Registry<?>> registriesByClass = new HashMap<>();

	private final Map<Class<?>, List<Class<?>>> orderedRegistryContainers = new LinkedHashMap<>();

	public RegistryScannerV1(Class<?>... registryOrder)
	{
		for (Class<?> c : registryOrder)
		{
			this.orderedRegistryContainers.put(c, new ArrayList<>());
		}
	}

	public RegistryScannerV1()
	{
		this(Block.class, Item.class, ItemGroup.class);
	}

	public void indexRegistries() throws IllegalAccessException
	{
		for (Field field : Registries.class.getDeclaredFields())
		{
			Type type = field.getGenericType();

			if(type instanceof ParameterizedType pType && Registry.class.isAssignableFrom((Class<?>) pType.getRawType()) && field.canAccess(null))
			{
				Type genericType = pType.getActualTypeArguments()[0];

				if(genericType instanceof Class<?> c)
				{
					this.registriesByClass.put(c, (Registry<?>) field.get(null));
				}

				if(genericType instanceof ParameterizedType genericPType)
				{
					this.registriesByClass.put((Class<?>) genericPType.getRawType(), (Registry<?>) field.get(null));
				}
			}
		}
	}

	public void scanRegistryContainers(String packageName) throws IOException, ClassNotFoundException
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader(); // TODO

		for (var info : ClassPath.from(loader).getTopLevelClasses(packageName))
		{
			Class<?> containerClass = loader.loadClass(info.getName());

			RegistryContainer containerAnnotation = containerClass.getAnnotation(RegistryContainer.class);

			if(containerAnnotation == null)
			{
				continue;
			}

			Class<?> registryClass = containerAnnotation.value();
			this.orderedRegistryContainers.computeIfAbsent(registryClass, k -> new ArrayList<>()).add(containerClass);
		}
	}

	public void registerEntries() throws IllegalAccessException
	{
		for (var entry : this.orderedRegistryContainers.entrySet())
		{
			Class<?> registryClass = entry.getKey();
			List<Class<?>> containerClasses = entry.getValue();

			Registry registry = this.registriesByClass.get(registryClass);

			if (registry == null)
			{
				continue;
			}

			for (Class<?> containerClass : containerClasses)
			{
				for (Field field : containerClass.getDeclaredFields())
				{
					if (registryClass.isAssignableFrom(field.getType()) && field.canAccess(null))
					{
						Registry.register(registry, LocksCore.id(field.getName().toLowerCase()), field.get(null));
					}
				}
			}
		}
	}

	public void scanAndRegister(String packageName)
	{
		try
		{
			this.indexRegistries();
			this.scanRegistryContainers(packageName);
			this.registerEntries();
		}
		catch (IllegalAccessException | IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}