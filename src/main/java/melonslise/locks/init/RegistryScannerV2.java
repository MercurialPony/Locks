package melonslise.locks.init;

import melonslise.locks.util.ReflectionUtil;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class RegistryScannerV2
{
	public static <T> Stream<T> all(Class<?> c)
	{
		return ReflectionUtil.visibleStaticFields(c)
			.map(ReflectionUtil::getStaticField);
	}

	public static <T> void process(Class<?> c, BiConsumer<String, T> processor)
	{
		try
		{
			Class.forName(c.getName(), true, c.getClassLoader());
		}
		catch (ClassNotFoundException ignored)
		{
		}

		ReflectionUtil.visibleStaticFields(c)
			.forEach(field ->
				processor.accept(field.getName().toLowerCase(), ReflectionUtil.getStaticField(field))
			);
	}

	public static <R, T> void register(Class<?> c, Registry<? super T> registry, String modId, Function<R, T> mapper)
	{
		RegistryScannerV2.<R>process(c, (name, entry) ->
			Registry.register(registry, new Identifier(modId, name), mapper.apply(entry))
		);
	}

	public static void register(Class<?> c, Registry<?> registry, String modId)
	{
		RegistryScannerV2.register(c, registry, modId, Function.identity());
	}
}