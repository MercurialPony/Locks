package melonslise.locks.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.item.LockItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
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
			VERT_Z_BB = new AxisAlignedBB(-2d / 16d, -3d / 16d, 0.5d / 16d, 2d / 16d, 3d / 16d, 0.5d / 16d),
			VERT_X_BB = LocksUtil.rotateY(VERT_Z_BB),
			HOR_Z_BB = LocksUtil.rotateX(VERT_Z_BB),
			HOR_X_BB = LocksUtil.rotateY(HOR_Z_BB);

		public static AxisAlignedBB getBounds(Transform tr)
		{
			return tr.face == AttachFace.WALL ? tr.dir.getAxis() == Direction.Axis.Z ? VERT_Z_BB : VERT_X_BB : tr.dir.getAxis() == Direction.Axis.Z ? HOR_Z_BB : HOR_X_BB;
		}

		public final Vector3d pos;
		public final Transform tr;
		public final AxisAlignedBB bb;

		public State(Vector3d pos, Transform tr)
		{
			this(pos, tr, getBounds(tr).move(pos));
		}

		public State(Vector3d pos, Transform tr, AxisAlignedBB bb)
		{
			this.pos = pos;
			this.tr = tr;
			this.bb = bb;
		}

		@OnlyIn(Dist.CLIENT)
		public boolean inView(ClippingHelper ch)
		{
			return ch.cubeInFrustum(this.bb.minX, this.bb.minY, this.bb.minZ, this.bb.maxX, this.bb.maxY, this.bb.maxZ);
		}

		@OnlyIn(Dist.CLIENT)
		public boolean inRange(Vector3d pos)
		{
			Minecraft mc = Minecraft.getInstance();
			double dist = this.pos.distanceToSqr(pos);
			double max = mc.options.renderDistance * 8;
			return dist < max * max;
		}
	}

	public final Cuboid6i bb;
	public final Lock lock;
	public final Transform tr;
	public final ItemStack stack;
	public final int id;

	public int oldSwingTicks, swingTicks, maxSwingTicks;

	public Map<List<BlockState>, State> cache = new HashMap<>(6);

	public Lockable(Cuboid6i bb, Lock lock, Transform tr, ItemStack stack, World world)
	{
		this(bb, lock, tr, stack, world.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null).nextId());
	}

	public Lockable(Cuboid6i bb, Lock lock, Transform tr, ItemStack stack, int id)
	{
		this.bb = bb;
		this.lock = lock;
		this.tr = tr;
		this.stack = stack;
		this.id = id;
		lock.addObserver(this);
	}

	public static final String KEY_BB = "Bb", KEY_LOCK = "Lock", KEY_TRANSFORM = "Transform", KEY_STACK = "Stack", KEY_ID = "Id";

	public static Lockable fromNbt(CompoundNBT nbt)
	{
		return new Lockable(Cuboid6i.fromNbt(nbt.getCompound(KEY_BB)), Lock.fromNbt(nbt.getCompound(KEY_LOCK)), Transform.values()[(int) nbt.getByte(KEY_TRANSFORM)], ItemStack.of(nbt.getCompound(KEY_STACK)), nbt.getInt(KEY_ID));
	}

	public static CompoundNBT toNbt(Lockable lkb)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.put(KEY_BB, Cuboid6i.toNbt(lkb.bb));
		nbt.put(KEY_LOCK, Lock.toNbt(lkb.lock));
		nbt.putByte(KEY_TRANSFORM, (byte) lkb.tr.ordinal());
		nbt.put(KEY_STACK, lkb.stack.serializeNBT());
		nbt.putInt(KEY_ID, lkb.id);
		return nbt;
	}

	public static int idFromNbt(CompoundNBT nbt)
	{
		return nbt.getInt(KEY_ID);
	}

	public static Lockable fromBuf(PacketBuffer buf)
	{
		return new Lockable(Cuboid6i.fromBuf(buf), Lock.fromBuf(buf), buf.readEnum(Transform.class), buf.readItem(), buf.readInt());
	}

	public static void toBuf(PacketBuffer buf, Lockable lkb)
	{
		Cuboid6i.toBuf(buf, lkb.bb);
		Lock.toBuf(buf, lkb.lock);
		buf.writeEnum(lkb.tr);
		buf.writeItem(lkb.stack);
		buf.writeInt(lkb.id);
	}

	@Override
	public void update(Observable lock, Object data)
	{
		this.setChanged();
		this.notifyObservers();
		LockItem.setOpen(this.stack, !this.lock.locked);
	}

	public void tick()
	{
		this.oldSwingTicks = this.swingTicks;
		if(this.swingTicks > 0)
			--this.swingTicks;
	}

	public void swing(int ticks)
	{
		this.swingTicks = this.oldSwingTicks = this.maxSwingTicks = ticks;
	}

	// FIXME use array instead of list
	public State getLockState(World world)
	{
		List<BlockState> states = new ArrayList<>(this.bb.volume());
		for(BlockPos pos : this.bb.getContainedPos())
		{
			if(!world.hasChunkAt(pos))
				return null;
			states.add(world.getBlockState(pos));
		}
		State state = this.cache.get(states);
		if(state != null)
			return state;
		ArrayList<AxisAlignedBB> boxes = new ArrayList<>(4);
		for(BlockPos pos : this.bb.getContainedPos())
		{
			VoxelShape shape = world.getBlockState(pos).getShape(world, pos);
			if(shape.isEmpty())
				continue;
			AxisAlignedBB bb = shape.bounds();
			bb = bb.move(pos);
			AxisAlignedBB union = bb;
			Iterator<AxisAlignedBB> it = boxes.iterator();
			while(it.hasNext())
			{
				AxisAlignedBB bb1 = it.next();
				if(LocksUtil.intersectsInclusive(union, bb1))
				{
					union = union.minmax(bb1);
					it.remove();
				}
			}
			boxes.add(union);
		}
		if(boxes.isEmpty())
			return null;
		Direction side = this.tr.getCuboidFace();
		Vector3d center = this.bb.sideCenter(side);
		Vector3d point = center;
		double min = -1d;
		for(AxisAlignedBB box : boxes)
			for(Direction side1 : Direction.values())
			{
				Vector3d point1 = LocksUtil.sideCenter(box, side1).add(Vector3d.atLowerCornerOf(side1.getNormal()).scale(0.05d));
				double dist = center.distanceToSqr(point1);
				if(min != -1d && dist >= min)
					continue;
				point = point1;
				min = dist;
				side = side1;
			}
		state = new State(point, Transform.fromDirection(side, this.tr.dir));
		this.cache.put(states, state);
		return state;
	}
}