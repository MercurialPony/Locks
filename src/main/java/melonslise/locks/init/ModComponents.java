package melonslise.locks.init;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import melonslise.locks.LocksCore;
import melonslise.locks.storage.DefaultLockableStorage;
import melonslise.locks.storage.LockableStorage;

public final class ModComponents implements ChunkComponentInitializer
{
	public static final ComponentKey<LockableStorage> LOCKABLE_STORAGE = ComponentRegistryV3.INSTANCE.getOrCreate(LocksCore.id("lockable_storage"), LockableStorage.class);

	@Override
	public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry)
	{
		registry.register(LOCKABLE_STORAGE, DefaultLockableStorage::new);
	}
}