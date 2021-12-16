package melonslise.locks.common.item;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class MasterKeyItem extends Item
{
	public MasterKeyItem()
	{
		this.setMaxStackSize(1);
	}

	// TODO Sound pitch
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ)
	{
		if(Locks.debug)
		{
			if(player.isSneaking())
			{
				if(world.isRemote)
				{
					//Client
					for(Map.Entry<Integer, Lockable> entry : world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null).getLoaded().entrySet())
					{
						String message = "C:"+entry.getKey()+":"+entry.getValue().toString();
						player.sendMessage(new TextComponentString(message));
					}
				}
				else
				{
					//Server
					for(Map.Entry<Integer, Lockable> entry : world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null).getLoaded().entrySet())
					{
						String message = "S:"+entry.getKey()+":"+entry.getValue().toString();
						player.sendMessage(new TextComponentString(message));
					}
				}
			}
		}
		
		List<Lockable> matching = LocksUtil.intersecting(world, pos).collect(Collectors.toList());
		if(matching.isEmpty())
			return EnumActionResult.PASS;
		world.playSound(player, pos, LocksSoundEvents.LOCK_OPEN, SoundCategory.BLOCKS, 1F, 1F);
		if(world.isRemote)
			return EnumActionResult.SUCCESS;
		for(Lockable lockable : matching)
			lockable.lock.setLocked(!lockable.lock.isLocked());
		return EnumActionResult.SUCCESS;
	}
}