package melonslise.locks.common.command;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.capability.ILockableStorage;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

public class CommandLocksDebug extends CommandBase
{
	@Override
	public String getName()
	{
		return "locksdebug";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/locksdebug";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		World world = sender.getEntityWorld();
		String dimensionInfo = "Running locksdebug for dimension "+world.provider.getDimension();
		Locks.logger.info(dimensionInfo);
		informPlayer(dimensionInfo, sender);
		ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
		
		Set<Integer> loadedIds = new HashSet<>();
		boolean foundAnyIssue = false;
		int issues = 0;
		
		//Check Loaded
		for(Map.Entry<Integer, Lockable> entry : handler.getLoaded().entrySet())
		{
			loadedIds.add(entry.getKey().intValue());
			boolean sendLockableInfo = false;
			Integer mappedId = entry.getKey();
			Lockable lockable = entry.getValue();
			String lockableInfo = mappedId+" : "+lockable.toString();
			Locks.logger.info(lockableInfo);
			
			if(lockable.networkID != mappedId)
			{
				Locks.logger.warn("Lock has mismatched network ID");
				informPlayer("Lock has mismatched network ID", sender);
				sendLockableInfo = true;
				issues++;
			}
			
			//Lockable should only have one observer
			if(lockable.countObservers() != 1)
			{
				Locks.logger.warn("Lockable has incorrect observer count: "+lockable.countObservers());
				informPlayer("Lockable has incorrect observer count: "+lockable.countObservers(), sender);
				sendLockableInfo = true;
				issues++;
			}
			
			//Lock should only have one observer
			if(lockable.lock.countObservers() != 1 )
			{
				Locks.logger.warn("Lockable's Lock has incorrect observer count: "+lockable.lock.countObservers());
				informPlayer("Lockable's lock has incorrect observer count: "+lockable.lock.countObservers(), sender);
				sendLockableInfo = true;
				issues++;
			}
			
			List<Chunk> chs = lockable.box.containedChunksTo((x, z) -> LocksUtil.hasChunk(world, x, z) ? world.getChunkFromChunkCoords(x, z) : null, true);
			if(chs == null || chs.size() == 0)
			{
				Locks.logger.warn("Loaded lockable has no chunks");
				informPlayer("Loaded lockable has no chunks", sender);
				sendLockableInfo = true;
				issues++;
			}
			else
			{
				for(Chunk chunk : chs)
				{
					ILockableStorage storage = chunk.getCapability(LocksCapabilities.LOCKABLE_STORAGE, null);
					Lockable chunkLockable = storage.get().get(lockable.networkID);
					if(chunkLockable == null)
					{
						Locks.logger.warn("Loaded lockable has chunk that is missing itself");
						informPlayer("Loaded lockable has chunk that is missing itself", sender);
						sendLockableInfo = true;
						issues++;
					}
					else if(!lockable.equals(chunkLockable))
					{
						Locks.logger.warn("Loaded lockable has chunk that is outdated");
						informPlayer("Loaded lockable has chunk that is outdated", sender);
						sendLockableInfo = true;
						issues++;
					}
				}
			}
			
			if(sendLockableInfo)
			{
				foundAnyIssue = true;
				informPlayer("|_ "+lockableInfo, sender);
			}
		}
		
		//Check All Chunks for ghost locks
		for (Chunk chunk : Lists.newArrayList(((ChunkProviderServer)world.getChunkProvider()).getLoadedChunks()))
		{
			ILockableStorage storage = chunk.getCapability(LocksCapabilities.LOCKABLE_STORAGE, null);
			for(Map.Entry entry : storage.get().entrySet())
			{
				if(!loadedIds.contains(entry.getKey()))
				{
					foundAnyIssue = true;
					issues++;
					Locks.logger.warn("Chunk has ghost lockable: "+chunk.getPos()+" : "+entry.getValue().toString());
					informPlayer("Chunk has ghost lockable: "+chunk.getPos()+" : "+entry.getValue().toString(), sender);
				}
			}
				
		}
		
		//FIXME check each lockable for validness but that's a pain
		
		if(foundAnyIssue)
		{
			Locks.logger.info("Locks Debug Completed with issues: "+issues);
			informPlayer("Locks Debug Completed with issues: "+issues, sender);
		}
		else
		{
			Locks.logger.info("Locks Debug completed with no obvious issues found");
			informPlayer("Locks Debug completed with no obvious issues found", sender);
		}
	}
	
	@Override
	public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	private void informPlayer(String str, ICommandSender sender)
	{
		if(sender instanceof EntityPlayer)
			sender.sendMessage(new TextComponentString(str));
	}
}
