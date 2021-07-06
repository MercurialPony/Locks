package melonslise.locks.common.util;

@FunctionalInterface
public interface BiIntFunction<T>
{
	T apply(int x, int y);
}
