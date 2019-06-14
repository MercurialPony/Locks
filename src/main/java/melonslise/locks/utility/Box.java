package melonslise.locks.utility;

import java.util.Objects;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

// TODO Rename to cuboid
public class Box
{
	public final int x1, y1, z1, x2, y2, z2;

	public Box(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		this.x1= Math.min(x1, x2);
		this.y1= Math.min(y1, y2);
		this.z1= Math.min(z1, z2);
		this.x2= Math.max(x1, x2);
		this.y2= Math.max(y1, y2);
		this.z2= Math.max(z1, z2);
	}

	public Box(BlockPos position1, BlockPos position2)
	{
		this.x1 = Math.min(position1.getX(), position2.getX());
		this.y1 = Math.min(position1.getY(), position2.getY());
		this.z1 = Math.min(position1.getZ(), position2.getZ());
		this.x2 = Math.max(position1.getX(), position2.getX()) + 1;
		this.y2 = Math.max(position1.getY(), position2.getY()) + 1;
		this.z2 = Math.max(position1.getZ(), position2.getZ()) + 1;
	}

	public Box(BlockPos position)
	{
		this(position.getX(), position.getY(), position.getZ(), position.getX() + 1, position.getY() + 1, position.getZ() + 1);
	}

	public boolean intersects(Box other)
	{
		return this.x1 < other.x2 && this.x2 > other.x1 && this.y1 < other.y2 && this.y2 > other.y1 && this.z1 < other.z2 && this.z2 > other.z1;
	}

	public int volume()
	{
		return (this.x2 - this.x1) * (this.y2 - this.y1) * (this.z2 - this.z1);
	}

	public Vec3d center()
	{
		return new Vec3d((double) (this.x2 + this.x1) / 2D, (double) (this.y2 + this.y1) / 2D, (double) (this.z2 + this.z1) / 2D);
	}

	@Override
	public boolean equals(Object object)
	{
		if(this == object) return true;
		if(!(object instanceof Box)) return false;
		Box box = (Box) object;
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