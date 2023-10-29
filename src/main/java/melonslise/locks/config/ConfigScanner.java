package melonslise.locks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import melonslise.locks.util.ReflectionUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Deprecated
public final class ConfigScanner
{
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Comment
	{
		String value();
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Category
	{
		String value();
	}

	public record Entry(Type type, String name, Object defaultValue, String comment, String category, Supplier<Object> getter, Consumer<Object> setter)
	{
		public Entry(Field field)
		{
			this(
				field.getType(),
				field.getName().toLowerCase(),
				ReflectionUtil.getStaticField(field),
				Optional.ofNullable(field.getAnnotation(Comment.class))
					.map(Comment::value)
					.orElse(null),
				Optional.ofNullable(field.getAnnotation(Category.class))
					.map(Category::value)
					.orElse(null),
				() -> ReflectionUtil.getStaticField(field),
				v -> ReflectionUtil.setStaticField(field, v)
			);
		}
	}

	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.excludeFieldsWithModifiers(Modifier.TRANSIENT)
		.create();

	private static final Map<Class<?>, List<Entry>> ENTRY_CACHE = new HashMap<>();

	public static List<Entry> entries(Class<?> c)
	{
		return ENTRY_CACHE.computeIfAbsent(c, k ->
			ReflectionUtil.visibleStaticFields(c)
				.map(Entry::new)
				.toList()
		);
	}

	public static void load(Class<?> c, String modId)
	{
		try
		{
			Path path = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
			GSON.fromJson(Files.readString(path), c);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void save(Class<?> c, String modId)
	{
		try
		{
			Path path = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");

			Files.writeString(path, GSON.toJson(c.getDeclaredConstructor().newInstance()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}