package melonslise.locks.common.util;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public enum Orientation
{
	NORTH_UP(Direction.NORTH, AttachFace.CEILING),
	SOUTH_UP(Direction.SOUTH, AttachFace.CEILING),
	WEST_UP(Direction.WEST, AttachFace.CEILING),
	EAST_UP(Direction.EAST, AttachFace.CEILING),
	NORTH_MID(Direction.NORTH, AttachFace.WALL),
	SOUTH_MID(Direction.SOUTH, AttachFace.WALL),
	WEST_MID(Direction.WEST, AttachFace.WALL),
	EAST_MID(Direction.EAST, AttachFace.WALL),
	NORTH_DOWN(Direction.NORTH, AttachFace.FLOOR),
	SOUTH_DOWN(Direction.SOUTH, AttachFace.FLOOR),
	WEST_DOWN(Direction.WEST, AttachFace.FLOOR),
	EAST_DOWN(Direction.EAST, AttachFace.FLOOR);

	public static final HashMap<Pair<Direction, AttachFace>, Orientation> LOOKUP = Maps.newHashMap();

	static
	{
		for(Orientation orient : Orientation.values())
			LOOKUP.put(Pair.of(orient.dir, orient.face), orient);
	}

	public final Direction dir;
	public final AttachFace face;

	Orientation(Direction dir, AttachFace face)
	{
		this.dir = dir;
		this.face = face;
	}

	public Direction getCuboidFace()
	{
		return this.face == AttachFace.CEILING ? Direction.UP : this.face == AttachFace.FLOOR ? Direction.DOWN : this.dir;
	}

	public static Orientation fromDirectionAndFace(Direction dir, AttachFace face, Direction defDir)
	{
		return LOOKUP.get(Pair.of(dir.getAxis() == Axis.Y ? defDir : dir, face));
	}

	public static Orientation fromDirection(Direction dir, Direction defDir)
	{
		return fromDirectionAndFace(dir, LocksUtil.faceFromDir(dir), defDir);
	}
}