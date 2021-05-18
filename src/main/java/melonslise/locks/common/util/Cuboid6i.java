package melonslise.locks.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

public class Cuboid6i
{
	public final int x1, y1, z1, x2, y2, z2;

	public Cuboid6i(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		this.x1= Math.min(x1, x2);
		this.y1= Math.min(y1, y2);
		this.z1= Math.min(z1, z2);
		this.x2= Math.max(x1, x2);
		this.y2= Math.max(y1, y2);
		this.z2= Math.max(z1, z2);
	}

	public Cuboid6i(BlockPos pos1, BlockPos pos2)
	{
		this.x1 = Math.min(pos1.getX(), pos2.getX());
		this.y1 = Math.min(pos1.getY(), pos2.getY());
		this.z1 = Math.min(pos1.getZ(), pos2.getZ());
		this.x2 = Math.max(pos1.getX(), pos2.getX()) + 1;
		this.y2 = Math.max(pos1.getY(), pos2.getY()) + 1;
		this.z2 = Math.max(pos1.getZ(), pos2.getZ()) + 1;
	}

	public static final String KEY_X1 = "X1", KEY_Y1 = "Y1", KEY_Z1 = "Z1", KEY_X2 = "X2", KEY_Y2 = "Y2", KEY_Z2 = "Z2";

	public static Cuboid6i fromNbt(CompoundNBT nbt)
	{
		return new Cuboid6i(nbt.getInt(KEY_X1), nbt.getInt(KEY_Y1), nbt.getInt(KEY_Z1), nbt.getInt(KEY_X2), nbt.getInt(KEY_Y2), nbt.getInt(KEY_Z2));
	}

	public static CompoundNBT toNbt(Cuboid6i bb)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt(KEY_X1, bb.x1);
		nbt.putInt(KEY_Y1, bb.y1);
		nbt.putInt(KEY_Z1, bb.z1);
		nbt.putInt(KEY_X2, bb.x2);
		nbt.putInt(KEY_Y2, bb.y2);
		nbt.putInt(KEY_Z2, bb.z2);
		return nbt;
	}

	public static Cuboid6i fromBuf(PacketBuffer buf)
	{
		return new Cuboid6i(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
	}

	public static void toBuf(PacketBuffer buf, Cuboid6i bb)
	{
		buf.writeInt(bb.x1);
		buf.writeInt(bb.y1);
		buf.writeInt(bb.z1);
		buf.writeInt(bb.x2);
		buf.writeInt(bb.y2);
		buf.writeInt(bb.z2);
	}

	public Cuboid6i offset(int x, int y, int z)
	{
		return new Cuboid6i(this.x1 + x, this.y1 + y, this.z1 + z, this.x2 + x, this.y2 + y, this.z2 + z);
	}

	public Cuboid6i intersection(Cuboid6i other)
	{
		return new Cuboid6i(Math.max(this.x1, other.x1), Math.max(this.y1, other.y1), Math.max(this.z1, other.z1), Math.min(this.x2, other.x2), Math.min(this.y2, other.y2), Math.min(this.z2, other.z2));
	}

	public int length()
	{
		return this.x2 - this.x1;
	}

	public int height()
	{
		return this.y2 - this.y1;
	}

	public int width()
	{
		return this.z2 - this.z1;
	}

	public int volume()
	{
		return this.length() * this.height() * this.width();
	}

	public Vector3d center()
	{
		return new Vector3d((this.x1 + this.x2) * 0.5d, (this.y1 + this.y2) * 0.5d, (this.z1 + this.z2) * 0.5d);
	}

	public boolean intersects(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		return this.x1 < x2 && this.x2 >x1 && this.y1 < y2 && this.y2 > y1 && this.z1 < z2 && this.z2 > z1;
	}

	public boolean intersects(Cuboid6i other)
	{
		return this.intersects(other.x1, other.y1, other.z1, other.x2, other.y2, other.z2);
	}

	public boolean intersects(BlockPos pos)
	{
		return this.intersects(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

	public Iterable<BlockPos> getContainedPos()
	{
		return BlockPos.betweenClosed(this.x1, this.y1, this.z1, this.x2 - 1, this.y2 - 1, this.z2 - 1);
	}

	public boolean getContainedChunks(BiIntPredicate p)
	{
		// Get the intersecting chunks and go through the checks
		// Use bitshift because apparently / 16 behaves differently with certain negative numbers
		int x1 = this.x1 >> 4;
		int x2 = this.x2 >> 4;
		int z1 = this.z1 >> 4;
		int z2 = this.z2 >> 4;
		// Prevents funky behavior at positive x/z chunk edges
		if(this.x2 % 16 == 0)
			x2 -= 1;
		if(this.z2 % 16 == 0)
			z2 -= 1;

		int sizeX = x2 - x1 + 1;
		int length = sizeX * (z2 - z1 + 1);
		for(int a = 0; a < length; ++a)
			if(p.test(x1 + a / sizeX, z1 + a % sizeX))
				return false;
		return true;
	}

	public <T> List<T> containedChunksTo(BiIntFunction<T> f, boolean endEarly)
	{
		// Get the intersecting chunks and go through the checks
		// Use bitshift because apparently / 16 behaves differently with certain negative numbers
		int x1 = this.x1 >> 4;
		int x2 = this.x2 >> 4;
		int z1 = this.z1 >> 4;
		int z2 = this.z2 >> 4;
		// Prevents funky behavior at positive x/z chunk edges
		if(this.x2 % 16 == 0)
			x2 -= 1;
		if(this.z2 % 16 == 0)
			z2 -= 1;

		int sizeX = x2 - x1 + 1;
		int length = sizeX * (z2 - z1 + 1);
		List<T> list = new ArrayList<>(length);
		for(int a = 0; a < length; ++a)
		{
			T t = f.apply(x1 + a % sizeX, z1 + a / sizeX);
			if(endEarly && t == null)
				return null;
			list.add(a, t);
		}
		return list;
	}

	/*
	public Iterable<BlockPos> getContainedChunkPos()
	{
		// Get the intersecting chunks and go through the checks
		// Use bitshift because apparently / 16 behaves differently with negative numbers
		final int x1 = this.x1 >> 4;
		final int x2 = this.x2 >> 4 + (this.x2 >> 4) % 16 == 0 ? -1 : 0;
		final int z1 = this.z1 >> 4;
		final int z2 = this.z2 >> 4 + (this.z2 >> 4) % 16 == 0 ? -1 : 0;
		// Prevents funky behavior at positive x/z chunk edges
		if(this.x2 % 16 == 0)
			x2 -= 1;
		if(this.z2 % 16 == 0)
			z2 -= 1;
		final int sizeX = x2 - x1 + 1;
		return new Iterable<BlockPos>()
		{
			@Override
			public Iterator<BlockPos> iterator()
			{
				return new Iterator<BlockPos>()
				{
					public BlockPos.Mutable mut = new BlockPos.Mutable(x2 - 1, 0 , z1);

					@Override
					public boolean hasNext()
					{
						return this.mut != null;
					}

					@Override
					public BlockPos next()
					{
						if(this.mut.getX() != x2)
							return this.mut.move(1, 0, 0);
						if(this.mut.getZ() == z2)
							return null;
						return this.mut.set(x1, 0, this.mut.getZ() + 1);
					}
				};
			}

			@Override
			public Spliterator<BlockPos> spliterator()
			{
				return new AbstractSpliterator<BlockPos>(sizeX * (z2 - z1 + 1), 64)
				{
					public BlockPos.Mutable mut = new BlockPos.Mutable(x1 - 1, 0, z1);

					@Override
					public boolean tryAdvance(Consumer<? super BlockPos> c)
					{
						if(this.mut.getX() == x2)
						{
							if(this.mut.getZ() == z2)
								return false;
							this.mut.set(x1, 0, this.mut.getZ() + 1);
						}
						else
							this.mut.move(1, 0, 0);
						c.accept(this.mut);
						return true;
					}
				};
			}
		};
	}
	*/

	public Vector3d sideCenter(Direction side)
	{
		Vector3i dir = side.getNormal();
		return new Vector3d((this.x1 + this.x2 + this.length() * dir.getX()) * 0.5d, (this.y1 + this.y2 + this.height() * dir.getY()) * 0.5d, (this.z1 + this.z2 + this.width() * dir.getZ()) * 0.5d);
	}

	public boolean isLoaded(World world)
	{
		return world.hasChunksAt(this.x1, this.y1, this.z1, this.x2, this.y2, this.z2);
	}

	@Override
	public boolean equals(Object object)
	{
		if(this == object)
			return true;
		if(!(object instanceof Cuboid6i))
			return false;
		Cuboid6i box = (Cuboid6i) object;
		return this.x1 == box.x1 && this.x2 == box.x2 && this.y1 == box.y1 && this.y2 == box.y2 && this.z1 == box.z1 && this.z2 == box.z2;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.x1,this. y1, this.z1, this.x2, this.y2, this.z2);
	}

	@Override
	public String toString()
	{
		return "Box{" + this.x1 + ", " + this.y1 + ", " + this.z1 + ", " + this.x2 + ", " + this.y2 + ", " + this.z2 +"}";
	}
}