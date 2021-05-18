package melonslise.locks.common.capability;

import java.util.Observer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.common.util.Lockable;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public interface ILockableHandler extends INBTSerializable<IntNBT>, Observer
{
	int nextId();

	Int2ObjectMap<Lockable> getLoaded();

	Int2ObjectMap<Lockable> getInChunk(BlockPos pos);

	boolean add(Lockable lkb);

	boolean remove(int id);
}