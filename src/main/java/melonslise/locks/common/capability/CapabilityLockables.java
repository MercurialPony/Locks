package melonslise.locks.common.capability;

import java.util.Observable;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.init.LocksNetworks;
import melonslise.locks.common.network.toClient.PacketLockableAdd;
import melonslise.locks.common.network.toClient.PacketLockableRemove;
import melonslise.locks.common.network.toClient.PacketLockableStatus;
import melonslise.locks.utility.Lockable;
import melonslise.locks.utility.LocksUtilities;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class CapabilityLockables implements ICapabilityLockables
{
	public static final ResourceLocation ID = new ResourceLocation(Locks.ID, "lockables");

	public final World world;
	protected Int2ObjectMap<Lockable> lockables = new Int2ObjectLinkedOpenHashMap<Lockable>();

	public CapabilityLockables(World world)
	{
		this.world = world;
	}

	// TODO Sync
	@Override
	public void setLockables(Int2ObjectMap<Lockable> lockables)
	{
		this.lockables = lockables;
	}

	@Override
	public Int2ObjectMap<Lockable> getLockables()
	{
		return new Int2ObjectLinkedOpenHashMap<Lockable>(this.lockables);
	}

	@Override
	public boolean add(Lockable lockable)
	{
		if(lockable == null || lockable.box == null || lockable.lock == null || lockable.side == null || lockable.box.volume() > LocksConfiguration.MAIN.lockableVolume.get() || this.lockables.values().stream().anyMatch(new PredicateIntersecting(lockable.box))) return false;
		this.lockables.put(lockable.networkID, lockable);
		lockable.addObserver(this);
		if(this.world.isRemote) return true;
		LocksNetworks.MAIN.send(PacketDistributor.DIMENSION.with(() -> this.world.dimension.getType()), new PacketLockableAdd(lockable));
		return true;
	}

	@Override
	public boolean remove(int networkID)
	{
		Lockable lockable = this.lockables.remove(networkID);
		if(lockable == this.lockables.defaultReturnValue()) return false;
		lockable.deleteObserver(this);
		if(this.world.isRemote) return true;
		LocksNetworks.MAIN.send(PacketDistributor.DIMENSION.with(() -> this.world.dimension.getType()), new PacketLockableRemove(networkID));
		return true;
	}

	@Override
	public void update(Observable observable, Object data)
	{
		if(this.world.isRemote || !(observable instanceof Lockable)) return;
		Lockable lockable = (Lockable) observable;
		LocksNetworks.MAIN.send(PacketDistributor.DIMENSION.with(() -> this.world.dimension.getType()), new PacketLockableStatus(lockable.networkID, lockable.lock.isLocked()));
	}

	@Override
	public INBT serializeNBT()
	{
		ListNBT list = new ListNBT();
		for(Lockable lockable : this.lockables.values()) list.add(LocksUtilities.writeLockableToNBT(lockable));
		return list;
	}

	@Override
	public void deserializeNBT(INBT nbt)
	{
		ListNBT list = (ListNBT) nbt;
		for(int a = 0; a < list.size(); ++a)
		{
			Lockable lockable = LocksUtilities.readLockableFromNBT(list.getCompound(a));
			this.lockables.put(lockable.networkID, lockable);
			lockable.addObserver(this);
		}
	}
}