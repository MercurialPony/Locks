package melonslise.locks.storage;

import dev.onyxstudios.cca.api.v3.component.Component;

public interface LockableStorage extends Component
{
	void add(Lockable lockable);

	void remove(int id);
}