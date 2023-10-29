package melonslise.locks.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

public final class ReflectionUtil
{
	@SuppressWarnings("unchecked")
	public static <T> T getStaticField(Field field)
	{
		try
		{
			return (T) field.get(null);
		}
		catch (IllegalAccessException e)
		{
			return null;
		}
	}

	public static void setStaticField(Field field, Object value)
	{
		try
		{
			field.set(null, value);
		}
		catch (IllegalAccessException ignored)
		{
		}
	}

	public static Stream<Field> visibleStaticFields(Class<?> c)
	{
		return Arrays.stream(c.getDeclaredFields())
			.filter(field -> field.canAccess(null));
	}
}