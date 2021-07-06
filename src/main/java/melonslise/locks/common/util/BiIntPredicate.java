package melonslise.locks.common.util;

@FunctionalInterface
public interface BiIntPredicate
{
	boolean test(int x, int y);
}