package melonslise.locks.common.event;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = Locks.ID)
public class LocksDelayEvents
{
	private static Map<Integer, Map<ChunkPos, ChunkDelay>> chunksToPoll = new ConcurrentHashMap<>();
	public static int delay = 20; //Set delay in ticks here
	
	private LocksDelayEvents(){}
	
	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch event)
	{
		//Try chest population (delayed version)
		tryAddChunkToPoll(event.getChunkInstance());
	}
	
	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event)
	{
		if(event.phase == TickEvent.Phase.START || event.world.isRemote)
			return;
		
		pollChunksInWorld(event.world);
	}
	
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event)
	{
		if(event.getWorld().isRemote)
			return;
		
		clearDimensionInPollMap(event.getWorld());
	}
	
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event)
	{
		if(event.getWorld().isRemote)
			return;
		
		clearDimensionInPollMap(event.getWorld());
	}
	
	private static void tryAddChunkToPoll(Chunk c)
	{
		int dimension = c.getWorld().provider.getDimension();
		
		Map<ChunkPos, ChunkDelay> dimChunkMap = getChunksToPollMap(dimension);
		
		//Don't restart the delay just because somebody else showed up
		if(!dimChunkMap.containsKey(c.getPos()))
			dimChunkMap.put(c.getPos(), new ChunkDelay(delay));
	}
	
	private static void clearDimensionInPollMap(World world)
	{
		int dimension = world.provider.getDimension();
		Map<ChunkPos, ChunkDelay> dimChunkMap = getChunksToPollMap(dimension);
		dimChunkMap.clear();
	}
	
	private static void pollChunksInWorld(World world)
	{
		int dimension = world.provider.getDimension();
		Map<ChunkPos, ChunkDelay> dimChunkMap = getChunksToPollMap(dimension);
		
		Iterator<Map.Entry<ChunkPos, ChunkDelay>> it = dimChunkMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry<ChunkPos, ChunkDelay> entry = it.next();
			if(entry.getValue().tick())
			{
				/*
				ChunkPos cpos = entry.getKey();
				//Only run delayed generation on loaded chunks
				if(LocksUtil.hasChunk(world, cpos.x, cpos.z))
					world.getChunkFromChunkCoords(cpos.x, cpos.z).getCapability(LocksCapabilities.LOCKABLE_WORLDGEN_HANDLER, null).tryGeneratingLocks();
				it.remove();
				*/
				
				ChunkPos cpos = entry.getKey();
				//Only run delayed generation on loaded chunks
				if(LocksUtil.hasChunk(world, cpos.x, cpos.z))
				{
					//If surrounding chunks are not loaded, delay this routine
					//This should prevent a lot of issues
					if(LocksUtil.hasChunk(world, cpos.x + 1, cpos.z)
						&& LocksUtil.hasChunk(world, cpos.x - 1, cpos.z)
						&& LocksUtil.hasChunk(world, cpos.x, cpos.z - 1)
						&& LocksUtil.hasChunk(world, cpos.x, cpos.z + 1)
					)
					{
						//Try generating
						world.getChunkFromChunkCoords(cpos.x, cpos.z).getCapability(LocksCapabilities.LOCKABLE_WORLDGEN_HANDLER, null).tryGeneratingLocks();
						//Remove, since chunk is now processed
						it.remove();
					}
					else
					{
						//Don't remove after reset
						entry.getValue().reset();
					}
				}
				else
				{
					//Remove if chunk is unloaded
					it.remove();
				}
			}
		}
	}
	
	@Nonnull
	private static Map<ChunkPos, ChunkDelay> getChunksToPollMap(int dimension)
	{
		Map<ChunkPos, ChunkDelay> dimChunkMap = chunksToPoll.get(dimension);
		if(dimChunkMap == null)
		{
			dimChunkMap = new ConcurrentHashMap<>();
			chunksToPoll.put(dimension, dimChunkMap);
		}
		return dimChunkMap;
	}
	
	private static class ChunkDelay
	{
		private int delay;
		
		public ChunkDelay(int delay)
		{
			this.delay = delay;
		}
		
		public boolean tick()
		{
			this.delay -= 1;
			return this.delay <= 0;
		}
		
		public void reset()
		{
			this.delay = LocksDelayEvents.delay;
		}
	}
}
