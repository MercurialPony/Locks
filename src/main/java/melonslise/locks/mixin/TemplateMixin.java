package melonslise.locks.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LockableInfo;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.util.Transform;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.common.util.Constants;

@Mixin(Template.class)
public class TemplateMixin
{
	private final List<LockableInfo> lockableInfos = new ArrayList<>();

	@Inject(at = @At("HEAD"), method = "fillFromWorld(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/block/Block;)V")
	private void fillFromWorld(World world, BlockPos start, BlockPos size, boolean takeEntities, @Nullable Block toIgnore, CallbackInfo ci)
	{
		if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1)
		{
			this.lockableInfos.clear();
			ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
			Cuboid6i bb = new Cuboid6i(start, start.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
			handler.getLoaded().values().stream()
				.filter(lkb -> lkb.bb.intersects(bb))
				.forEach(lkb ->
				{
					Cuboid6i newBB = bb.intersection(lkb.bb).offset(-start.getX(), -start.getY(), -start.getZ());
					this.lockableInfos.add(new LockableInfo(newBB, lkb.lock, lkb.tr, lkb.stack, lkb.id));
				});
		}
	}

	// Second return
	@Inject(at = @At(value = "RETURN", ordinal = 1), method = "placeInWorld(Lnet/minecraft/world/IServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/template/PlacementSettings;Ljava/util/Random;I)Z")
	private void placeInWorld(IServerWorld world, BlockPos start, BlockPos size, PlacementSettings settings, Random rng, int i, CallbackInfoReturnable<Boolean> cir)
	{
		World level;
		try
		{
			level = world.getLevel();
		}
		catch(Exception e)
		{
			Locks.LOGGER.warn(world + "#getLevel threw an error! Skipping lockable placement for this template ");
			return;
		}
		ILockableHandler handler = level.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
		for(LockableInfo lkb : this.lockableInfos)
		{
			BlockPos pos1 = LocksUtil.transform(lkb.bb.x1, lkb.bb.y1, lkb.bb.z1, settings);
			BlockPos pos2 = LocksUtil.transform(lkb.bb.x2, lkb.bb.y2, lkb.bb.z2, settings);
			Cuboid6i bb = new Cuboid6i(pos1.getX() + start.getX(), pos1.getY() + start.getY(), pos1.getZ() + start.getZ(), pos2.getX() + start.getX(), pos2.getY() + start.getY(), pos2.getZ() + start.getZ());
			ItemStack stack = LocksConfig.RANDOMIZE_LOADED_LOCKS.get() ? LocksConfig.getRandomLock(rng) : lkb.stack;
			Lock lock = LocksConfig.RANDOMIZE_LOADED_LOCKS.get() ? Lock.from(stack) : lkb.lock;
			Transform tr = Transform.fromDirectionAndFace(settings.getRotation().rotate(settings.getMirror().getRotation(lkb.tr.dir).rotate(lkb.tr.dir)), lkb.tr.face, Direction.NORTH);
			handler.add(new Lockable(bb, lock, tr, stack, level));
		}
	}

	private static final String KEY_LOCKABLES = "Lockables";

	@Inject(at = @At("HEAD"), method = "save(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;")
	private void save(CompoundNBT nbt, CallbackInfoReturnable<CompoundNBT> cir)
	{
		ListNBT list = new ListNBT();
		for(LockableInfo lkb : this.lockableInfos)
			list.add(LockableInfo.toNbt(lkb));
		nbt.put(KEY_LOCKABLES, list);
	}

	@Inject(at = @At("HEAD"), method = "load(Lnet/minecraft/nbt/CompoundNBT;)V")
	private void read(CompoundNBT nbt, CallbackInfo ci)
	{
		this.lockableInfos.clear();
		ListNBT list = nbt.getList(KEY_LOCKABLES, Constants.NBT.TAG_COMPOUND);
		for(int a = 0, b = list.size(); a < b; ++a)
			this.lockableInfos.add(LockableInfo.fromNbt(list.getCompound(a)));
	}
}