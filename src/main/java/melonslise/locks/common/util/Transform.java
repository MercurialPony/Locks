package melonslise.locks.common.util;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;

public enum Transform
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

	public static final HashMap<Pair<Direction, AttachFace>, Transform> LOOKUP = new HashMap<>(16); // 12 / 0.75

	static
	{
		for(Transform tr : Transform.values())
			LOOKUP.put(Pair.of(tr.dir, tr.face), tr);
	}

	public final Direction dir;
	public final AttachFace face;

	Transform(Direction dir, AttachFace face)
	{
		this.dir = dir;
		this.face = face;
	}

	public Direction getCuboidFace()
	{
		return this.face == AttachFace.CEILING ? Direction.UP : this.face == AttachFace.FLOOR ? Direction.DOWN : this.dir;
	}

	public static Transform fromDirectionAndFace(Direction dir, AttachFace face, Direction def)
	{
		return LOOKUP.get(Pair.of(dir.getAxis() == Direction.Axis.Y ? def : dir, face));
	}

	public static Transform fromDirection(Direction dir, Direction def)
	{
		return fromDirectionAndFace(dir, LocksUtil.faceFromDir(dir), def);
	}
}