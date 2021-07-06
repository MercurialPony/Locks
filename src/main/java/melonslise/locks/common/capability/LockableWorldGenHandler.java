package melonslise.locks.common.capability;

import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.util.Orientation;
import net.minecraft.block.BlockChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class LockableWorldGenHandler implements ILockableWorldGenHandler
{
	public static final ResourceLocation ID = new ResourceLocation(Locks.ID, "lockable_worldgen_handler");

	public static final int NONE = 0; //Default value on pre-existing chunks
	public static final int SHOULD_GENERATE = 1;
	public static final int FINISHED_GENERATING = 2;
	
	public final Chunk chunk;
	
	public AtomicInteger phase = new AtomicInteger();
	
	public LockableWorldGenHandler(Chunk chunk)
	{
		this.chunk = chunk;
	}

	@Override
	public void setChunkShouldGenerateChests()
	{
		if(this.phase.get() != FINISHED_GENERATING)
			this.phase.set(SHOULD_GENERATE);
	}
	
	@Override
	public void tryGeneratingLocks()
	{
		// TODO Retrogen for existing chunks? (NONE)
		if(this.phase.get() == SHOULD_GENERATE)
		{
			//Do chunk chest generation
			
			// TODO this is same-seed inconsistent
			// Not like people really care about this sort of thing, but...
			
			World world = chunk.getWorld();
			Random rand = world.rand;
			
			ILockableHandler lockables = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
			for(Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet())
			{
				if(!LocksConfig.canGen(rand))
					continue;
				
				BlockPos pos = entry.getKey();
				if(!(entry.getValue() instanceof TileEntityChest) || lockables.getInChunk(pos).values().stream().anyMatch(lockable1 -> lockable1.box.intersects(pos)))
					continue;
				
				BlockPos adjPos = LocksUtil.getAdjacentChest((TileEntityChest) entry.getValue());
				ItemStack stack = LocksConfig.getRandomLock(rand);
				
				lockables.add(new Lockable(
						new Cuboid6i(pos, adjPos == null ? pos : adjPos), 
						Lock.from(stack), 
						Orientation.fromDirection(world.getBlockState(pos).getValue(BlockChest.FACING), EnumFacing.NORTH),
						stack,
						world)
						);
				
			}
			
			this.phase.set(FINISHED_GENERATING);
			this.chunk.markDirty();
		}
	}
	@Override
	public NBTTagInt serializeNBT()
	{
		return new NBTTagInt(this.phase.get());
	}

	@Override
	public void deserializeNBT(NBTTagInt nbt)
	{
		this.phase.set(nbt.getInt());
	}

	

}
