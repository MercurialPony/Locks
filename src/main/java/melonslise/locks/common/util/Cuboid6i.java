package melonslise.locks.common.util;

import java.util.Objects;

import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

	public Cuboid6i(BlockPos pos)
	{
		this(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

	public Cuboid6i offset(int x, int y, int z)
	{
		return new Cuboid6i(this.x1 + x, this.y1 + y, this.z1 + z, this.x2 + x, this.y2 + y, this.z2 + z);
	}

	public Cuboid6i offset(BlockPos pos)
	{
		return this.offset(pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean intersects(Cuboid6i other)
	{
		return this.intersects(other.x1, other.y1, other.z1, other.x2, other.y2, other.z2);
	}

	public boolean intersects(BlockPos pos)
	{
		return this.intersects(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

	public boolean intersects(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		return this.x1 < x2 && this.x2 >x1 && this.y1 < y2 && this.y2 > y1 && this.z1 < z2 && this.z2 > z1;
	}

	public boolean contains(Cuboid6i other)
	{
		return this.contains(other.x1, other.y1, other.z1) && this.contains(other.x2, other.y2, other.z2);
	}

	public boolean contains(int x, int y, int z)
	{
		return x >= this.x1 && x < this.x2 && y >= this.y1 && y < this.y2 && z >= this.z1 && z < this.z2;
	}

	public Cuboid6i intersection(Cuboid6i other)
	{
		return new Cuboid6i(Math.max(this.x1, other.x1), Math.max(this.y1, other.y1), Math.max(this.z1, other.z1), Math.min(this.x2, other.x2), Math.min(this.y2, other.y2), Math.min(this.z2, other.z2));
	}

	public Cuboid6i union(Cuboid6i other)
	{
		return new Cuboid6i(Math.min(this.x1, other.x1), Math.min(this.y1, other.y1), Math.min(this.z1, other.z1), Math.max(this.x2, other.x2), Math.max(this.y2, other.y2), Math.max(this.z2, other.z2));
	}

	public int volume()
	{
		return (this.x2 - this.x1) * (this.y2 - this.y1) * (this.z2 - this.z1);
	}

	public Vec3d center()
	{
		return new Vec3d((double) (this.x2 + this.x1) / 2D, (double) (this.y2 + this.y1) / 2D, (double) (this.z2 + this.z1) / 2D);
	}

	public Iterable<MutableBlockPos> getContainedBlockPositions()
	{
		return BlockPos.getAllInBoxMutable(this.x1, this.y1, this.z1, this.x2 - 1, this.y2 - 1, this.z2 - 1);
	}

	public Vec3d getSideCenter(EnumFacing side)
	{
		switch(side)
		{
		case DOWN: return new Vec3d((double) (this.x1 + this.x2) / 2D, (double) this.y1, (double) (this.z1 + this.z2) / 2D);
		case UP: return new Vec3d((double) (this.x1 + this.x2) / 2D, (double) this.y2, (double) (this.z1 + this.z2) / 2D);
		case NORTH: return new Vec3d((double) (this.x1 + this.x2) / 2D, (double) (this.y1 + this.y2) / 2D, (double) this.z1);
		case SOUTH: return new Vec3d((double) (this.x1 + this.x2) / 2D, (double) (this.y1 + this.y2) / 2D, (double) this.z2);
		case WEST: return new Vec3d((double) this.x1, (double) (this.y1 + this.y2) / 2D, (double) (this.z1 + this.z2) / 2D);
		case EAST: return new Vec3d((double) this.x2, (double) (this.y1 + this.y2) / 2D, (double) (this.z1 + this.z2) / 2D);
		default: return null;
		}
	}

	public AxisAlignedBB toAABB()
	{
		return new AxisAlignedBB(this.x1, this.y1, this.z1, this.x2, this.y2, this.z2);
	}

	public boolean loaded(World world)
	{
		return world.isAreaLoaded(this.x1, this.y1, this.z1, this.x2, this.y2, this.z2, true);
	}

	@SideOnly(Side.CLIENT)
	public boolean inView()
	{
		return ClippingHelperImpl.getInstance().isBoxInFrustum(this.x1 - TileEntityRendererDispatcher.staticPlayerX, this.y1 - TileEntityRendererDispatcher.staticPlayerY, this.z1 - TileEntityRendererDispatcher.staticPlayerZ, this.x2 - TileEntityRendererDispatcher.staticPlayerX, this.y2 - TileEntityRendererDispatcher.staticPlayerY, this.z2 - TileEntityRendererDispatcher.staticPlayerZ);
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