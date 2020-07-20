package melonslise.locks.common.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Lockable extends Observable implements Observer
{
	private static final AtomicInteger networkIDs = new AtomicInteger();

	public final Cuboid6i box;
	public final Lock lock;
	public final Orientation orient;
	public final int networkID;

	public Map<List<BlockState>, Pair<Vec3d, Orientation>> cache = Maps.newHashMap();

	public int prevShakeTicks, shakeTicks, maxShakeTicks;

	// Server only
	public Lockable(Cuboid6i box, Lock lock, Orientation orient)
	{
		this(box, lock, orient, networkIDs.incrementAndGet());
	}

	// Client only
	public Lockable(Cuboid6i box, Lock lock, Orientation orient, int networkID)
	
	{
		this.box = box;
		this.lock = lock;
		lock.addObserver(this);
		this.orient = orient;
		this.networkID = networkID;
	}

	@Override
	public void update(Observable lock, Object data)
	{
		this.setChanged();
		this.notifyObservers();
	}

	public void tick()
	{
		this.prevShakeTicks = this.shakeTicks;
		if(this.shakeTicks > 0)
			--this.shakeTicks;
	}

	public void shake(int ticks)
	{
		this.shakeTicks = this.prevShakeTicks = this.maxShakeTicks = ticks;
	}

	public Pair<Vec3d, Orientation> getLockState(World world)
	{
		List<BlockState> states = new ArrayList<>(this.box.volume());
		for(BlockPos pos : this.box.getContainedBlockPositions())
		{
			if(!world.isBlockLoaded(pos))
				return null;
			states.add(world.getBlockState(pos));
		}
		Pair<Vec3d, Orientation> pair = this.cache.get(states);
		if(pair != null)
			return pair;
		ArrayList<AxisAlignedBB> boxes = new ArrayList<>(4);
		for(BlockPos pos : this.box.getContainedBlockPositions())
		{
			VoxelShape shape = world.getBlockState(pos).getRenderShape(world, pos);
			if(shape.isEmpty())
				continue;
			AxisAlignedBB box = shape.getBoundingBox();
			box = box.offset(pos);
			AxisAlignedBB union = box;
			Iterator<AxisAlignedBB> iterator = boxes.iterator();
			while(iterator.hasNext())
			{
				AxisAlignedBB box1 = iterator.next();
				if(LocksUtil.intersectsInclusive(union, box1))
				{
					union = union.union(box1);
					iterator.remove();
				}
			}
			boxes.add(union);
		}
		if(boxes.isEmpty())
			return null;
		Direction side = this.orient.getCuboidFace();
		Vec3d center = this.box.getSideCenter(side);
		Vec3d point = center;
		double min = -1d;
		for(AxisAlignedBB box : boxes)
			for(Direction side1 : Direction.values())
			{
				Vec3d point1 = LocksUtil.getBoxSideCenter(box, side1).add(new Vec3d(side1.getDirectionVec()).scale(0.05d));
				double dist = center.squareDistanceTo(point1);
				if(min != -1d && dist >= min)
					continue;
				point = point1;
				min = dist;
				side = side1;
			}
		pair = Pair.of(point, Orientation.fromDirection(side, this.orient.dir));
		this.cache.put(states, pair);
		return pair;
	}

	@OnlyIn(Dist.CLIENT)
	public boolean inRange()
	{
		Minecraft mc = Minecraft.getInstance();
		Pair<Vec3d, Orientation> state = this.getLockState(mc.world);
		if(state == null)
			return false;
		Vec3d origin = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
		double dist = state.getLeft().squareDistanceTo(origin);
		double max = mc.gameSettings.renderDistanceChunks * 8;
		return dist < max * max;
	}

	@Override
	public boolean equals(Object object)
	{
		if(this == object)
			return true;
		if(!(object instanceof Lockable))
			return false;
		Lockable lockable = (Lockable) object;
		return (this.networkID == lockable.networkID) && ((this.box == null && lockable.box == null) || this.box.equals(lockable.box)) && ((this.lock == null && lockable.lock == null) || this.lock.equals(lockable.lock)) && (this.orient == lockable.orient);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.box, this.lock, this.orient, this.networkID);
	}
}