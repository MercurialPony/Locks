package melonslise.locks.common.capability;

import java.util.Observable;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.RemoveLockablePacket;
import melonslise.locks.common.network.toclient.UpdateLockablePacket;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class LockableStorage implements ILockableStorage
{
	public static final ResourceLocation ID = new ResourceLocation(Locks.ID, "lockables");

	public final World world;
	protected Int2ObjectMap<Lockable> lockables = new Int2ObjectLinkedOpenHashMap<Lockable>();

	public LockableStorage(World world)
	{
		this.world = world;
	}

	// TODO Sync
	@Override
	public void set(Int2ObjectMap<Lockable> lockables)
	{
		this.lockables = lockables;
	}

	@Override
	public Int2ObjectMap<Lockable> get()
	{
		return new Int2ObjectLinkedOpenHashMap<Lockable>(this.lockables);
	}

	@Override
	public boolean add(Lockable lockable)
	{
		if(lockable == null || lockable.box == null || lockable.lock == null || lockable.orient == null || lockable.box.volume() > LocksConfig.getServer(this.world).maxLockableVolume || this.lockables.values().stream().anyMatch(other -> other.box.intersects(lockable.box)))
			return false;
		this.lockables.put(lockable.networkID, lockable);
		lockable.addObserver(this);
		if(this.world.isRemote)
			lockable.shake(10);
		else
			LocksNetworks.MAIN.sendToDimension(new AddLockablePacket(lockable), this.world.provider.getDimension());
		return true;
	}

	@Override
	public boolean remove(int networkID)
	{
		Lockable lockable = this.lockables.remove(networkID);
		if(lockable == this.lockables.defaultReturnValue())
			return false;
		lockable.deleteObserver(this);
		if(this.world.isRemote)
			return true;
		LocksNetworks.MAIN.sendToDimension(new RemoveLockablePacket(networkID), this.world.provider.getDimension());
		return true;
	}

	@Override
	public void update(Observable observable, Object data)
	{
		if(this.world.isRemote || !(observable instanceof Lockable))
			return;
		Lockable lockable = (Lockable) observable;
		LocksNetworks.MAIN.sendToDimension(new UpdateLockablePacket(lockable.networkID, lockable.lock.isLocked()), this.world.provider.getDimension());
	}

	@Override
	public NBTTagList serializeNBT()
	{
		NBTTagList list = new NBTTagList();
		for(Lockable lockable : this.lockables.values())
			list.appendTag(LocksUtil.writeLockableToNBT(lockable));
		return list;
	}

	@Override
	public void deserializeNBT(NBTTagList list)
	{
		for(int a = 0; a < list.tagCount(); ++a)
		{
			Lockable lockable = LocksUtil.readLockableFromNBT(list.getCompoundTagAt(a));
			this.lockables.put(lockable.networkID, lockable);
			lockable.addObserver(this);
		}
	}
}