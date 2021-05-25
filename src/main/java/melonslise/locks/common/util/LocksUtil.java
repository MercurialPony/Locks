package melonslise.locks.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Random;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.mixin.ForgeHooksAccessor;
import melonslise.locks.mixin.LootPoolAccessor;
import melonslise.locks.mixin.LootTableAccessor;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;

public final class LocksUtil
{
	public static IResourceManager resourceManager;

	public static Constructor lootTableContextConstructor;

	static
	{
		try
		{
			lootTableContextConstructor = Class.forName("net.minecraftforge.common.ForgeHooks$LootTableContext").getDeclaredConstructor(ResourceLocation.class, boolean.class);
			lootTableContextConstructor.setAccessible(true);
		}
		catch (SecurityException | IllegalArgumentException | NoSuchMethodException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	private LocksUtil() {}

	public static void shuffle(byte[] array, Random rng)
	{
		for (int a = array.length - 1; a > 0; --a)
		{
			int index = rng.nextInt(a + 1);
			byte temp = array[index];
			array[index] = array[a];
			array[a] = temp;
		}
	}

	public static boolean chance(Random rng, double ch)
	{
		return ch == 1d || ch != 0d && rng.nextDouble() <= ch;
	}

	public static BlockPos transform(int x, int y, int z, PlacementSettings settings)
	{
		switch(settings.getMirror())
		{
		case LEFT_RIGHT:
			z = -z + 1;
			break;
		case FRONT_BACK:
			x = -x + 1;
			break;
		default:
			break;
		}
		int x1 = settings.getRotationPivot().getX();
		int z1 = settings.getRotationPivot().getZ();
		switch(settings.getRotation())
		{
		case COUNTERCLOCKWISE_90:
			return new BlockPos(x1 - z1 + z, y, x1 + z1 - x + 1);
		case CLOCKWISE_90:
			return new BlockPos(x1 + z1 - z + 1, y, z1 - x1 + x);
		case CLOCKWISE_180:
			return new BlockPos(x1 + x1 - x + 1, y, z1 + z1 - z + 1);
		default:
			return new BlockPos(x, y, z);
		}
	}

	public static AttachFace faceFromDir(Direction dir)
	{
		return dir == Direction.UP ? AttachFace.CEILING : dir == Direction.DOWN ? AttachFace.FLOOR : AttachFace.WALL;
	}

	public static AxisAlignedBB rotateY(AxisAlignedBB bb)
	{
		return new AxisAlignedBB(bb.minZ, bb.minY, bb.minX, bb.maxZ, bb.maxY, bb.maxX);
	}

	public static AxisAlignedBB rotateX(AxisAlignedBB bb)
	{
		return new AxisAlignedBB(bb.minX, bb.minZ, bb.minY, bb.maxX, bb.maxZ, bb.maxY);
	}

	public static boolean intersectsInclusive(AxisAlignedBB bb1, AxisAlignedBB bb2)
	{
		return bb1.minX <= bb2.maxX && bb1.maxX >= bb2.minX && bb1.minY <= bb2.maxY && bb1.maxY >= bb2.minY && bb1.minZ <= bb2.maxZ && bb1.maxZ >= bb2.minZ;
	}

	public static Vector3d sideCenter(AxisAlignedBB bb, Direction side)
	{
		Vector3i dir = side.getNormal();
		return new Vector3d((bb.minX + bb.maxX + (bb.maxX - bb.minX) * dir.getX()) * 0.5d, (bb.minY + bb.maxY + (bb.maxY - bb.minY) * dir.getY()) * 0.5d, (bb.minZ + bb.maxZ + (bb.maxZ - bb.minZ) * dir.getZ()) * 0.5d);
	}

	public static LootTable lootTableFrom(ResourceLocation loc) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		JsonElement json = JSONUtils.fromJson(LootTableManager.GSON, new BufferedReader(new InputStreamReader(resourceManager.getResource(loc).getInputStream(), StandardCharsets.UTF_8)), JsonElement.class);
		Deque que = ForgeHooksAccessor.getLootContext().get();
		Object lootCtx = lootTableContextConstructor.newInstance(loc, false);
		try
		{
			que.push(lootCtx);
			return LootTableManager.GSON.fromJson(json, LootTable.class);
		}
		catch(JsonSyntaxException e)
		{
			throw e;
		}
		finally // Still executes even if catch throws according to SO!
		{
			que.pop();
		}
	}

	// Only merges entries, not conditions and functions
	public static LootTable mergeEntries(LootTable table, LootTable inject)
	{
		for(LootPool injectPool : ((LootTableAccessor) inject).getPools())
		{
			LootPool pool = table.getPool(injectPool.getName());
			if(pool == null)
				table.addPool(injectPool);
			else
				((LootPoolAccessor) pool).getEntries().addAll(((LootPoolAccessor) injectPool).getEntries());
		}
		return table;
	}

	public static Stream<Lockable> intersecting(World world, BlockPos pos)
	{
		return world.getCapability(LocksCapabilities.LOCKABLE_HANDLER).lazyMap(cap -> cap.getInChunk(pos).values().stream().filter(lkb -> lkb.bb.intersects(pos))).orElse(Stream.empty());
	}

	public static boolean locked(World world, BlockPos pos)
	{
		return intersecting(world, pos).anyMatch(LocksPredicates.LOCKED);
	}
}