package melonslise.locks.storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class AABBi
{
	public final int x1, y1, z1, x2, y2, z2;

	public AABBi(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.z1 = Math.min(z1, z2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
		this.z2 = Math.max(z1, z2);
	}

	public AABBi(BlockPos pos1, BlockPos pos2)
	{
		this.x1 = Math.min(pos1.getX(), pos2.getX());
		this.y1 = Math.min(pos1.getY(), pos2.getY());
		this.z1 = Math.min(pos1.getZ(), pos2.getZ());
		this.x2 = Math.max(pos1.getX(), pos2.getX()) + 1;
		this.y2 = Math.max(pos1.getY(), pos2.getY()) + 1;
		this.z2 = Math.max(pos1.getZ(), pos2.getZ()) + 1;
	}

	public static AABBi fromNbt(NbtCompound nbt)
	{
		return new AABBi(nbt.getInt("X1"), nbt.getInt("Y1"), nbt.getInt("Z1"), nbt.getInt("X2"), nbt.getInt("Y2"), nbt.getInt("Z2"));
	}

	public static NbtCompound toNbt(AABBi bounds)
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putInt("X1", bounds.x1);
		nbt.putInt("Y1", bounds.y1);
		nbt.putInt("Z1", bounds.z1);
		nbt.putInt("X2", bounds.x2);
		nbt.putInt("Y2", bounds.y2);
		nbt.putInt("Z2", bounds.z2);
		return nbt;
	}

	public AABBi offset(int x, int y, int z)
	{
		return new AABBi(this.x1 + x, this.y1 + y, this.z1 + z, this.x2 + x, this.y2 + y, this.z2 + z);
	}

	public AABBi intersect(AABBi other)
	{
		return new AABBi(Math.max(this.x1, other.x1), Math.max(this.y1, other.y1), Math.max(this.z1, other.z1), Math.min(this.x2, other.x2), Math.min(this.y2, other.y2), Math.min(this.z2, other.z2));
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

	public boolean intersects(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		return this.x1 < x2 && this.x2 >x1 && this.y1 < y2 && this.y2 > y1 && this.z1 < z2 && this.z2 > z1;
	}

	public boolean intersects(AABBi other)
	{
		return this.intersects(other.x1, other.y1, other.z1, other.x2, other.y2, other.z2);
	}

	public boolean intersects(BlockPos pos)
	{
		return this.intersects(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}
}