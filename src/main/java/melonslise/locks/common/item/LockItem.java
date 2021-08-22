package melonslise.locks.common.item;

import java.util.List;

import javax.annotation.Nullable;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.capability.ISelection;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.util.Orientation;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoor.EnumDoorHalf;
import net.minecraft.block.BlockDoor.EnumHingePosition;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LockItem extends LockingItem
{
	public final int length;
	public final int enchantmentValue;
	public final float resistance;
	
	public static final String KEY_LENGTH = "Length";
	public static final String KEY_OPEN = "Open";

	public LockItem(int length, int enchVal, float resist)
	{
		super();
		this.length = length;
		this.enchantmentValue = enchVal;
		this.resistance = resist;
		
		addPropertyOverride(new ResourceLocation("locks:open"), new IItemPropertyGetter()
		{
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, World world, EntityLivingBase entity)
			{
				if(stack.getItem() instanceof LockItem)
				{
					return isOpen(stack)? 1.0f : 0.0f;
				}
				
				return 0.0f;
			}
		});
	}
	
	@Override
	public boolean isEnchantable(ItemStack stack)
	{
		return true;
	}

	@Override
	public int getItemEnchantability()
	{
		return this.enchantmentValue;
	}
	
	public static boolean isOpen(ItemStack stack)
	{
		NBTTagCompound nbt = LocksUtil.getTag(stack);
		if(!nbt.hasKey(KEY_OPEN))
			nbt.setBoolean(KEY_OPEN, false);
		return nbt.getBoolean(KEY_OPEN);
	}

	public static void setOpen(ItemStack stack, boolean open)
	{
		LocksUtil.getTag(stack).setBoolean(KEY_OPEN, open);
	}
	
	/*
	public static ItemStack from(Lock lock)
	{
		ItemStack stack = new ItemStack(LocksItems.LOCK);
		NBTTagCompound nbt = LocksUtil.getTag(stack);
		nbt.setInteger(KEY_ID, lock.id);
		nbt.setInteger(KEY_OPEN, lock.id);
		nbt.setByte(KEY_LENGTH, (byte) lock.getLength());
		return stack;
	}
	*/

	public static byte getOrSetLength(ItemStack stack)
	{
		NBTTagCompound nbt = LocksUtil.getTag(stack);
		if(!nbt.hasKey(KEY_LENGTH))
			nbt.setByte(KEY_LENGTH, (byte) ((LockItem)stack.getItem()).length);
		return nbt.getByte(KEY_LENGTH);
	}
	
	public static float getResistance(ItemStack stack)
	{
		//For blast resistance
		return ((LockItem) stack.getItem()).resistance;
	}


	// TODO Delegate logic to cap?
	// TODO Change null checks to optionals/whatever
	// TODO Sound pitch
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ)
	{
		ILockableHandler lockables = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER, null);
		if (!LocksConfig.getServer(world).canLock(world, pos) || lockables.getInChunk(pos).values().stream().anyMatch(lockable1 -> lockable1.box.intersects(pos)))
			return EnumActionResult.PASS;
		
		return LocksConfig.getServer(world).easyLock ? easyLock(lockables, player, world, pos, hand, face, hitX, hitY, hitZ) : freeLock(lockables, player, world, pos, hand, face, hitX, hitY, hitZ);
	}
	
	public EnumActionResult freeLock(ILockableHandler lockables, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ)
	{
		ISelection select = player.getCapability(LocksCapabilities.SELECTION, null);
		ItemStack stack = player.getHeldItem(hand);
		
		BlockPos pos1 = select.get();
		if (pos1 == null)
			select.set(pos);
		else
		{
			select.set(null);
			// TODO Go through the add checks here as well
			world.playSound(player, pos, LocksSoundEvents.LOCK_CLOSE, SoundCategory.BLOCKS, 1F, 1F);
			if (world.isRemote)
				return EnumActionResult.SUCCESS;
			
			ItemStack lockStack = stack.copy();
			lockStack.setCount(1);
			
			if (!lockables.add(new Lockable(new Cuboid6i(pos1, pos), Lock.from(lockStack), Orientation.fromDirection(face, player.getHorizontalFacing().getOpposite()), lockStack, world)))
				return EnumActionResult.PASS;
			if (!player.isCreative())
				stack.shrink(1);
		}
		return EnumActionResult.SUCCESS;
	}
	
	public EnumActionResult easyLock(ILockableHandler lockables, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ)
	{
		world.playSound(player, pos, LocksSoundEvents.LOCK_CLOSE, SoundCategory.BLOCKS, 1f, 1f);
		if(world.isRemote)
			return EnumActionResult.SUCCESS;
		
		IBlockState state = world.getBlockState(pos);
		BlockPos pos1 = pos;
		
		TileEntity te = world.getTileEntity(pos);
		
		Orientation placedOrientation = Orientation.fromDirection(face, player.getHorizontalFacing().getOpposite());
		
		if(te instanceof TileEntityChest)
		{
			BlockPos adjPos = LocksUtil.getAdjacentChest((TileEntityChest) te);
			if(adjPos != null)
				pos1 = adjPos;
			
			//Override orientation
			if(LocksConfig.COMMON.automaticallyOrientPlacedLocks && state.getBlock() instanceof BlockChest) //Safety first
				placedOrientation = Orientation.fromDirection(world.getBlockState(pos).getValue(BlockChest.FACING), EnumFacing.NORTH);
		}
		else if(state.getBlock() instanceof BlockDoor)
		{
			//TODO consider catching IllegalArgumentException for modded BlockDoor that for some reason remove the HALF, FACING, OPEN, or HINGE property
			//Could also check if the dropped item is AIR or not to determine top or bottom, although that's equally flimsy
			
			//TODO cleanup or redo
			EnumDoorHalf  clickedDoorHalf = state.getValue(BlockDoor.HALF);
			EnumHingePosition clickedDoorHinge = state.getValue(BlockDoor.HINGE);
			EnumFacing clickedDoorFacing = (EnumFacing)state.getValue(BlockDoor.FACING);
			boolean isOpen = state.getValue(BlockDoor.OPEN);
			
			//Find the other half of the door
			if(clickedDoorHalf == EnumDoorHalf.LOWER)
				pos1 = pos.up();
			else
				pos1 = pos.down();
			
			//Make sure it's not some glitched door
			IBlockState otherHalfState = world.getBlockState(pos1);
			
			//Top half of the door has no idea if it's open or not for some reason
			//Top half of the door has no idea where it's facing for some reason
			if(clickedDoorHalf == EnumDoorHalf.UPPER && otherHalfState.getBlock() instanceof BlockDoor)
			{
				isOpen = otherHalfState.getValue(BlockDoor.OPEN);
				clickedDoorFacing = otherHalfState.getValue(BlockDoor.FACING);
			}
			
			//Bottom half of the door has no idea which hinge it's on for some reason
			if(clickedDoorHalf == EnumDoorHalf.LOWER && otherHalfState.getBlock() instanceof BlockDoor)
				clickedDoorHinge = otherHalfState.getValue(BlockDoor.HINGE);
			
			if(!(otherHalfState.getBlock() instanceof BlockDoor) 
					//|| otherHalfState.getValue(BlockDoor.OPEN) != isOpen  //top half of the door has no idea if it's open or not for some reason
					|| otherHalfState.getValue(BlockDoor.HALF) == clickedDoorHalf)
					//|| otherHalfState.getValue(BlockDoor.HINGE) != clickedDoorHinge  //this is broken too, can you believe it?
					//|| otherHalfState.getValue(BlockDoor.FACING) != clickedDoorFacing) //wow so is this
			{
				pos1 = pos;
			}
			else if(!isOpen)
			{
				//Only try to find double doors if the door is closed and not glitched
				int xAdd = 0;
				int zAdd = 0;
				switch(clickedDoorFacing)
				{
					case EAST:
					default:
						zAdd = clickedDoorHinge == EnumHingePosition.RIGHT ? -1 : 1;
						break;
					case NORTH:
						xAdd = clickedDoorHinge == EnumHingePosition.RIGHT ? -1 : 1;
						break;
					case SOUTH:
						xAdd = clickedDoorHinge == EnumHingePosition.RIGHT ? 1: -1;
						break;
					case WEST:
						zAdd = clickedDoorHinge == EnumHingePosition.RIGHT ? 1: -1;
						break;
				}
				//Evaluate states
				IBlockState otherTop = world.getBlockState((clickedDoorHalf == EnumDoorHalf.LOWER ? pos1 : pos).add(xAdd, 0, zAdd));
				IBlockState otherBottom = world.getBlockState((clickedDoorHalf == EnumDoorHalf.LOWER ? pos : pos1).add(xAdd, 0, zAdd));
				
				//BlockDoor
				if(otherTop.getBlock() instanceof BlockDoor && otherBottom.getBlock() instanceof BlockDoor)
				{
					//Must Be Closed
					//if(!otherTop.getValue(BlockDoor.OPEN) && !otherBottom.getValue(BlockDoor.OPEN))
					if(!otherBottom.getValue(BlockDoor.OPEN))
					{
						//Same hinges to self and opposite the other door
						//if(otherTop.getValue(BlockDoor.HINGE) == otherBottom.getValue(BlockDoor.HINGE) && otherTop.getValue(BlockDoor.HINGE) == (clickedDoorHinge == EnumHingePosition.RIGHT ? EnumHingePosition.LEFT : EnumHingePosition.RIGHT))
						if(otherTop.getValue(BlockDoor.HINGE) == (clickedDoorHinge == EnumHingePosition.RIGHT ? EnumHingePosition.LEFT : EnumHingePosition.RIGHT))
						{
							//Facing is same as other door
							//if(otherTop.getValue(BlockDoor.FACING) == clickedDoorFacing && otherBottom.getValue(BlockDoor.FACING) == clickedDoorFacing)
							if(otherBottom.getValue(BlockDoor.FACING) == clickedDoorFacing)
							{
								//Finally, the halves
								if(otherTop.getValue(BlockDoor.HALF) == EnumDoorHalf.UPPER && otherBottom.getValue(BlockDoor.HALF) == EnumDoorHalf.LOWER)
								{
									//All passed, increase bounding box
									pos1 = pos1.add(xAdd, 0, zAdd);
								}
							}
						}
					}
				}
			}
			
			//Override orientation
			if(LocksConfig.COMMON.automaticallyOrientPlacedLocks)
			{
				if(isOpen)
				{
					EnumFacing curfacing = clickedDoorFacing;
					switch(clickedDoorFacing)
					{
						case NORTH: 
						case SOUTH: curfacing = EnumFacing.WEST; 
							break;
						case WEST:
						case EAST: curfacing = EnumFacing.SOUTH; 
							break;
						default: 
							break;
					}
					
					placedOrientation = Orientation.fromDirection(curfacing.getOpposite(), EnumFacing.NORTH); //Inconsistent, but so is locking an open door
				}
				else
				{
					placedOrientation = Orientation.fromDirection(clickedDoorFacing.getOpposite(), EnumFacing.NORTH);
				}
			}
		}
		
		ItemStack stack = player.getHeldItem(hand);
		ItemStack lockStack = stack.copy();
		lockStack.setCount(1);
		
		if (!lockables.add(new Lockable(new Cuboid6i(pos1, pos), Lock.from(lockStack), placedOrientation, lockStack, world)))
			return EnumActionResult.PASS;
		if (!player.isCreative())
			stack.shrink(1);
		
		return EnumActionResult.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!isOpen(stack))
			return super.onItemRightClick(world, player, hand);
		setOpen(stack, false);
		world.playSound(player, player.posX, player.posY, player.posZ, LocksSoundEvents.PIN_MATCH, SoundCategory.PLAYERS, 1F, 1F);
		return super.onItemRightClick(world, player, hand);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> lines, ITooltipFlag flag)
	{
		super.addInformation(stack, world, lines, flag);
		int length = LocksUtil.hasKey(stack, KEY_LENGTH) ? stack.getTagCompound().getByte(KEY_LENGTH) : this.length;
		ITextComponent txt = new TextComponentTranslation(Locks.ID + ".tooltip.length", ItemStack.DECIMALFORMAT.format(length));
		txt.getStyle().setColor(TextFormatting.DARK_GREEN);
		lines.add(txt.getFormattedText());
		
		float resist = (int)this.resistance; //TODO nbt resistance?
		String resistString = ".tooltip.resistance.weak";
		if(resist >= 10.0f)
			resistString = ".tooltip.resistance.strong";
		if(resist >= 40.0f)
			resistString = ".tooltip.resistance.supreme";
		
		ITextComponent txt2 = new TextComponentTranslation(Locks.ID + resistString);
		txt2.getStyle().setColor(TextFormatting.DARK_GREEN);
		lines.add(txt2.getFormattedText());
	}
}