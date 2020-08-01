package melonslise.locks.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Lockable extends Observable implements Observer
{
	public static class State
	{
		public static final AxisAlignedBB
			VERT_Z_BB = new AxisAlignedBB(-2d/16d, -3d/16d, 0.5d/16d, 2d/16d, 3d/16d, 0.5d/16d),
			VERT_X_BB = LocksUtil.rotateY(VERT_Z_BB),
			HOR_Z_BB = LocksUtil.rotateX(VERT_Z_BB),
			HOR_X_BB = LocksUtil.rotateY(HOR_Z_BB);

		public final Vector3d pos;
		public final Orientation orient;
		public final AxisAlignedBB bb;

		public State(Vector3d pos, Orientation orient)
		{
			this(pos, orient, (orient.face == AttachFace.WALL ? orient.dir.getAxis() == Axis.Z ? VERT_Z_BB : VERT_X_BB : orient.dir.getAxis() == Axis.Z ? HOR_Z_BB : HOR_X_BB).offset(pos));
		}

		public State(Vector3d pos, Orientation orient, AxisAlignedBB bounds)
		{
			this.pos = pos;
			this.orient = orient;
			this.bb = bounds;
		}

		@OnlyIn(Dist.CLIENT)
		public boolean inView(ClippingHelper clippingHelper)
		{
			return clippingHelper.isBoxInFrustum(this.bb.minX, this.bb.minY, this.bb.minZ, this.bb.maxX, this.bb.maxY, this.bb.maxZ);
		}

		@OnlyIn(Dist.CLIENT)
		public boolean inRange(Vector3d pos)
		{
			Minecraft mc = Minecraft.getInstance();
			double dist = this.pos.squareDistanceTo(pos);
			double max = mc.gameSettings.renderDistanceChunks * 8;
			return dist < max * max;
		}
	}

	private static final AtomicInteger networkIDs = new AtomicInteger();

	public final Cuboid6i box;
	public final Lock lock;
	public final Orientation orient;
	public final int networkID;

	public Map<List<BlockState>, State> cache = new HashMap<>();

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

	public State getLockState(World world)
	{
		List<BlockState> states = new ArrayList<>(this.box.volume());
		for(BlockPos pos : this.box.getContainedBlockPositions())
		{
			if(!world.isBlockLoaded(pos))
				return null;
			states.add(world.getBlockState(pos));
		}
		State state = this.cache.get(states);
		if(state != null)
			return state;
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
		Vector3d center = this.box.getSideCenter(side);
		Vector3d point = center;
		double min = -1d;
		for(AxisAlignedBB box : boxes)
			for(Direction side1 : Direction.values())
			{
				Vector3d point1 = LocksUtil.getAABBSideCenter(box, side1).add(Vector3d.func_237491_b_(side1.getDirectionVec()).scale(0.05d));
				double dist = center.squareDistanceTo(point1);
				if(min != -1d && dist >= min)
					continue;
				point = point1;
				min = dist;
				side = side1;
			}
		state = new State(point, Orientation.fromDirection(side, this.orient.dir));
		this.cache.put(states, state);
		return state;
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