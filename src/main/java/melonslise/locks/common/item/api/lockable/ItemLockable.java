package melonslise.locks.common.item.api.lockable;

import java.util.List;
import java.util.UUID;

import melonslise.locks.common.item.api.LocksItem;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ItemLockable extends LocksItem
{
	public ItemLockable(String name)
	{
		super(name);
		this.setMaxStackSize(1);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag)
	{
		UUID id = getID(stack);
		if(id != null) tooltip.add(id.toString());
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		if(!world.isRemote) ensureID(stack);
	}
	
	public static String keyIdentifier = "identifier";

	public static ItemStack assignID(ItemStack to, UUID id)
	{
		NBTTagCompound nbt = LocksUtilities.getTag(to);
		if(!nbt.hasUniqueId(keyIdentifier)) nbt.setUniqueId(keyIdentifier, id);
		return to;
	}

	public static ItemStack copyID(ItemStack to, ItemStack from)
	{
		UUID id = getID(from);
		if(id == null) return to;
		return assignID(to, id);
	}

	public static UUID getID(ItemStack stack)
	{
		return LocksUtilities.hasUUID(stack, keyIdentifier) ? stack.getTagCompound().getUniqueId(keyIdentifier) : null;
	}

	public static void ensureID(ItemStack stack)
	{
		NBTTagCompound nbt = LocksUtilities.getTag(stack);
		if(!nbt.hasUniqueId(keyIdentifier)) nbt.setUniqueId(keyIdentifier, UUID.randomUUID());
	}
}