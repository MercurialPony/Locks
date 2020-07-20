package melonslise.locks.common.capability;

import java.util.Observable;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.RemoveLockablePacket;
import melonslise.locks.common.network.toclient.UpdateLockablePacket;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

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
		if(lockable == null || lockable.box == null || lockable.lock == null || lockable.orient == null || lockable.box.volume() > LocksServerConfig.MAX_LOCKABLE_VOLUME.get() || this.lockables.values().stream().anyMatch(lockable1 -> lockable1.box.intersects(lockable.box)))
			return false;
		this.lockables.put(lockable.networkID, lockable);
		lockable.addObserver(this);
		if(this.world.isRemote)
			lockable.shake(10);
		else
			LocksNetworks.MAIN.send(PacketDistributor.DIMENSION.with(() -> this.world.dimension.getType()), new AddLockablePacket(lockable));
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
		LocksNetworks.MAIN.send(PacketDistributor.DIMENSION.with(() -> this.world.dimension.getType()), new RemoveLockablePacket(networkID));
		return true;
	}

	@Override
	public void update(Observable observable, Object data)
	{
		if(this.world.isRemote || !(observable instanceof Lockable))
			return;
		Lockable lockable = (Lockable) observable;
		LocksNetworks.MAIN.send(PacketDistributor.DIMENSION.with(() -> this.world.dimension.getType()), new UpdateLockablePacket(lockable.networkID, lockable.lock.isLocked()));
	}

	@Override
	public ListNBT serializeNBT()
	{
		ListNBT list = new ListNBT();
		for(Lockable lockable : this.lockables.values())
			list.add(LocksUtil.writeLockableToNBT(lockable));
		return list;
	}

	@Override
	public void deserializeNBT(ListNBT list)
	{
		for(int a = 0; a < list.size(); ++a)
		{
			Lockable lockable = LocksUtil.readLockableFromNBT(list.getCompound(a));
			this.lockables.put(lockable.networkID, lockable);
			lockable.addObserver(this);
		}
	}
}