package melonslise.locks.common.util;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public enum Orientation
{
	NORTH_UP(EnumFacing.NORTH, AttachFace.CEILING),
	SOUTH_UP(EnumFacing.SOUTH, AttachFace.CEILING),
	WEST_UP(EnumFacing.WEST, AttachFace.CEILING),
	EAST_UP(EnumFacing.EAST, AttachFace.CEILING),
	NORTH_MID(EnumFacing.NORTH, AttachFace.WALL),
	SOUTH_MID(EnumFacing.SOUTH, AttachFace.WALL),
	WEST_MID(EnumFacing.WEST, AttachFace.WALL),
	EAST_MID(EnumFacing.EAST, AttachFace.WALL),
	NORTH_DOWN(EnumFacing.NORTH, AttachFace.FLOOR),
	SOUTH_DOWN(EnumFacing.SOUTH, AttachFace.FLOOR),
	WEST_DOWN(EnumFacing.WEST, AttachFace.FLOOR),
	EAST_DOWN(EnumFacing.EAST, AttachFace.FLOOR);

	public static final HashMap<Pair<EnumFacing, AttachFace>, Orientation> LOOKUP = Maps.newHashMap();

	static
	{
		for(Orientation orient : Orientation.values())
			LOOKUP.put(Pair.of(orient.dir, orient.face), orient);
	}

	public final EnumFacing dir;
	public final AttachFace face;

	Orientation(EnumFacing dir, AttachFace face)
	{
		this.dir = dir;
		this.face = face;
	}

	public EnumFacing getCuboidFace()
	{
		return this.face == AttachFace.CEILING ? EnumFacing.UP : this.face == AttachFace.FLOOR ? EnumFacing.DOWN : this.dir;
	}

	public static Orientation fromDirectionAndFace(EnumFacing dir, AttachFace face, EnumFacing defDir)
	{
		return LOOKUP.get(Pair.of(dir.getAxis() == Axis.Y ? defDir : dir, face));
	}

	public static Orientation fromDirection(EnumFacing dir, EnumFacing defDir)
	{
		return fromDirectionAndFace(dir, LocksUtil.faceFromDir(dir), defDir);
	}
}