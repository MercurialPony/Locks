package melonslise.locks.common.world.storage;

import java.util.ArrayList;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.network.LocksNetworks;
import melonslise.locks.common.network.client.MessageAddLockable;
import melonslise.locks.common.network.client.MessageRemoveLockables;
import melonslise.locks.common.network.client.MessageSetLockables;
import melonslise.locks.common.network.client.MessageToggleLockables;
import melonslise.locks.common.network.client.MessageToggleLockablesByID;
import melonslise.locks.utility.LocksUtilities;
import melonslise.locks.utility.predicate.PredicateIntersecting;
import melonslise.locks.utility.predicate.PredicateMatching;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class StorageLockables extends WorldSavedData
{
	public static final String name = LocksUtilities.prefixLocks("lockables");
	protected World world;
	protected ArrayList<Lockable> lockables = Lists.newArrayList();

	public StorageLockables(String name)
	{
		super(name);
	}

	public void setLockables(ArrayList<Lockable> lockables)
	{
		this.lockables = lockables;
		this.synchronize();
	}

	public ArrayList<Lockable> getLockables()
	{
		return (ArrayList<Lockable>) this.lockables.clone();
	}

	public static StorageLockables get(World world)
	{
		MapStorage storage = world.getPerWorldStorage();
		StorageLockables instance = (StorageLockables) storage.getOrLoadData(StorageLockables.class, name);
		if(instance == null)
		{
			instance = new StorageLockables(name);
			storage.setData(name, instance);
		}
		instance.world = world;
		return instance;
	}

	public void synchronize()
	{
		if(!this.world.isRemote) LocksNetworks.network.sendToDimension(new MessageSetLockables(this.lockables), this.world.provider.getDimension());
	}

	public static final String keyLockables = "lockables";

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagList list = nbt.getTagList(keyLockables, Constants.NBT.TAG_COMPOUND);
		for(int a = 0; a < list.tagCount(); ++a) lockables.add(Lockable.readFromNBT(list.getCompoundTagAt(a)));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();
		for(Lockable lockable : this.lockables) list.appendTag(Lockable.writeToNBT(lockable));
		nbt.setTag(keyLockables, list);
		return nbt;
	}

	public boolean add(Lockable lockable)
	{
		if(lockable == null || lockable.box == null || lockable.lock == null || lockable.lock.id == null || lockable.side == null || lockable.box.volume() > LocksConfiguration.getMain(this.world).lockable_volume || this.contains(new PredicateIntersecting(lockable.box))) return false;
		this.lockables.add(lockable);
		if(this.world.isRemote) return true;
		LocksNetworks.network.sendToDimension(new MessageAddLockable(lockable), this.world.provider.getDimension());
		this.markDirty();
		return true;
	}

	// TODO Also do point contains
	public boolean contains(Predicate<Lockable> condition)
	{
		for(Lockable lockable : this.lockables) if(condition.apply(lockable)) return true;
		return false;
	}

	public ArrayList<Lockable> matching(Predicate<Lockable> condition)
	{
		ArrayList<Lockable> matching = Lists.newArrayList();
		for(Lockable lockable : this.lockables) if(condition.apply(lockable)) matching.add(lockable);
		return matching;
	}

	public ArrayList<Lockable> remove(Box box)
	{
		ArrayList<Lockable> matching = this.matching(new PredicateIntersecting(box));
		for(Lockable lockable : matching) this.lockables.remove(lockable);
		if(this.world.isRemote || matching.isEmpty()) return matching;
		LocksNetworks.network.sendToDimension(new MessageRemoveLockables(box), this.world.provider.getDimension());
		this.markDirty();
		return matching;
	}

	public ArrayList<Lockable> toggle(Box box)
	{
		ArrayList<Lockable> matching = this.matching(new PredicateIntersecting(box));
		for(Lockable lockable : matching) lockable.lock.locked = !lockable.lock.locked;
		if(this.world.isRemote || matching.isEmpty()) return matching;
		LocksNetworks.network.sendToDimension(new MessageToggleLockables(box), this.world.provider.getDimension());
		this.markDirty();
		return matching;
	}

	public ArrayList<Lockable> toggle(Box box, UUID id)
	{
		ArrayList<Lockable> matching = this.matching(Predicates.and(new PredicateIntersecting(box), new PredicateMatching(id)));
		for(Lockable lockable : matching) lockable.lock.locked = !lockable.lock.locked;
		if(this.world.isRemote || matching.isEmpty()) return matching;
		LocksNetworks.network.sendToDimension(new MessageToggleLockablesByID(box, id), this.world.provider.getDimension());
		this.markDirty();
		return matching;
	}

	public void shuffleCombination(Lockable lockable)
	{
		lockable.lock.shuffle();
		if(!this.world.isRemote && this.lockables.contains(lockable)) this.markDirty();
	}
}