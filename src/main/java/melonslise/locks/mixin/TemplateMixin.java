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

import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LockableInfo;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.util.Orientation;
import net.minecraft.block.Block;
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
	private final List<LockableInfo> lockables = new ArrayList<>();

	@Inject(at = @At("HEAD"), method = "takeBlocksFromWorld(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/block/Block;)V")
	public void takeBlocksFromWorld(World world, BlockPos start, BlockPos size, boolean takeEntities, @Nullable Block toIgnore, CallbackInfo ci)
	{
		if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1)
		{
			this.lockables.clear();
			world.getCapability(LocksCapabilities.LOCKABLES)
				.ifPresent(lockables ->
				{
					Cuboid6i box = new Cuboid6i(start, start.add(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
					lockables.get().values().stream()
						.filter(lockable1 -> lockable1.box.intersects(box))
						.forEach(lockable ->
						{
							Cuboid6i newBox = box.intersection(lockable.box).offset(-start.getX(), -start.getY(), -start.getZ());
							this.lockables.add(new LockableInfo(newBox, lockable.lock, lockable.orient));
						});
				});
		}
	}

	// Second return
	@Inject(at = @At(value = "RETURN", ordinal = 1), method = "func_237146_a_(Lnet/minecraft/world/IServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/template/PlacementSettings;Ljava/util/Random;I)Z")
	public void addBlocksToWorld(IServerWorld world, BlockPos start, BlockPos size, PlacementSettings settings, Random rand, int i, CallbackInfoReturnable<Boolean> cir)
	{
		world.getWorld().getCapability(LocksCapabilities.LOCKABLES)
			.ifPresent(lockables ->
			{
				for(LockableInfo lockable : this.lockables)
				{
					BlockPos pos1 = LocksUtil.transformPos(lockable.box.x1, lockable.box.y1, lockable.box.z1, settings);
					BlockPos pos2 = LocksUtil.transformPos(lockable.box.x2, lockable.box.y2, lockable.box.z2, settings);
					Cuboid6i box = new Cuboid6i(pos1.getX() + start.getX(), pos1.getY() + start.getY(), pos1.getZ() + start.getZ(), pos2.getX() + start.getX(), pos2.getY() + start.getY(), pos2.getZ() + start.getZ());
					Lock lock = LocksConfig.RANDOMIZE_LOADED_LOCKS.get() ? new Lock(world.getRandom().nextInt(), LocksConfig.randLockLen(world.getRandom()), true) : lockable.lock;
					Orientation orient = Orientation.fromDirectionAndFace(settings.getRotation().rotate(settings.getMirror().toRotation(lockable.lockOrientation.dir).rotate(lockable.lockOrientation.dir)), lockable.lockOrientation.face, Direction.NORTH);
					lockables.add(new Lockable(box, lock, orient));
				}
			});
	}

	private static final String KEY_LOCKABLES = "lockables";

	@Inject(at = @At("HEAD"), method = "writeToNBT(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;")
	public void writeToNBT(CompoundNBT nbt, CallbackInfoReturnable<CompoundNBT> cir)
	{
		ListNBT lockableNBTList = new ListNBT();
		for(LockableInfo lockable : lockables)
		{
			CompoundNBT lockableNBT = new CompoundNBT();
			lockableNBT.put(LocksUtil.KEY_BOX, LocksUtil.writeBoxToNBT(lockable.box));
			lockableNBT.put(LocksUtil.KEY_LOCK, LocksUtil.writeLockToNBT(lockable.lock));
			lockableNBT.putByte(LocksUtil.KEY_ORIENTATION, (byte) lockable.lockOrientation.ordinal());
			lockableNBTList.add(lockableNBT);
		}
		nbt.put(KEY_LOCKABLES, lockableNBTList);
	}

	@Inject(at = @At("HEAD"), method = "read(Lnet/minecraft/nbt/CompoundNBT;)V")
	public void read(CompoundNBT nbt, CallbackInfo ci)
	{
		this.lockables.clear();
		ListNBT nbtList = nbt.getList(KEY_LOCKABLES, Constants.NBT.TAG_COMPOUND);
		for(int a = 0, b = nbtList.size(); a < b; ++a)
		{
			CompoundNBT lockableNBT = nbtList.getCompound(a);
			Cuboid6i box = LocksUtil.readBoxFromNBT(lockableNBT.getCompound(LocksUtil.KEY_BOX));
			Lock lock = LocksUtil.readLockFromNBT(lockableNBT.getCompound(LocksUtil.KEY_LOCK));
			Orientation orient = Orientation.values()[(int) lockableNBT.getByte(LocksUtil.KEY_ORIENTATION)];
			this.lockables.add(new LockableInfo(box, lock, orient));
		}
	}
}