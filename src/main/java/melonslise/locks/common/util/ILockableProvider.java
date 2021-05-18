package melonslise.locks.common.util;

import java.util.List;

public interface ILockableProvider
{
	List<Lockable> getLockables();
}