package melonslise.locks.client.event;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.Lists;

import melonslise.locks.LocksCore;
import melonslise.locks.common.capability.LocksCapabilities;
import melonslise.locks.common.config.LocksConfiguration;
import melonslise.locks.common.item.LocksItems;
import melonslise.locks.common.world.storage.Box;
import melonslise.locks.common.world.storage.Lockable;
import melonslise.locks.common.world.storage.StorageLockables;
import melonslise.locks.utility.LocksUtilities;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = LocksCore.ID, value = Side.CLIENT)
public class LocksClientEvents
{
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		LocksItems.registerModels(event);
	}

	public static boolean intersectsInclusive(AxisAlignedBB box1, AxisAlignedBB box2)
	{
		return box1.minX <= box2.maxX && box1.maxX >= box2.minX && box1.minY <= box2.maxY && box1.maxY >= box2.minY && box1.minZ <= box2.maxZ && box1.maxZ >= box2.minZ;
	}

	// TODO Configurable color
	// TODO Helpers
	// TODO Lighting
	@SubscribeEvent
	public static void render(RenderWorldLastEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Vec3d origin = LocksUtilities.getRenderOrigin(event.getPartialTicks());
		GlStateManager.pushMatrix();
		GlStateManager.translate(-origin.x, -origin.y, -origin.z);
		for(Lockable lockable : StorageLockables.get(mc.world).getLockables())
		{
			/*
			GlStateManager.pushMatrix();
			Vec3d center = LocksUtilities.getBoxSideCenter(lockable.box, lockable.side);
			GlStateManager.translate(center.x, center.y, center.z);
			GlStateManager.rotate(lockable.side.getHorizontalAngle(), 0F, 1F, 0F);
			if(lockable.side == EnumFacing.UP) GlStateManager.rotate(90F, 1F, 0F, 0F);
			if(lockable.side == EnumFacing.DOWN) GlStateManager.rotate(-90F, 1F, 0F, 0F);
			GlStateManager.scale(0.5D, 0.5D, 0.5D);
			mc.getRenderItem().renderItem(new ItemStack(LocksItems.lock), TransformType.FIXED);
			GlStateManager.popMatrix();
			 */
			boolean open = true;
			ArrayList<AxisAlignedBB> boxes = Lists.newArrayList();
			for(MutableBlockPos position : BlockPos.getAllInBoxMutable(lockable.box.x1, lockable.box.y1, lockable.box.z1, lockable.box.x2 - 1, lockable.box.y2 - 1, lockable.box.z2 - 1))
			{
				AxisAlignedBB box = mc.world.getBlockState(position).getCollisionBoundingBox(mc.world, position);
				if(box == null || box == Block.NULL_AABB) continue;
				box = box.offset(position);
				AxisAlignedBB union = box;
				Iterator<AxisAlignedBB> iterator = boxes.iterator();
				while(iterator.hasNext())
				{
					AxisAlignedBB box1 = iterator.next();
					if(intersectsInclusive(union, box1))
					{
						union = union.union(box1);
						iterator.remove();
					}
				}
				boxes.add(union);
			}
			if(boxes.isEmpty()) continue;
			Vec3d center = LocksUtilities.getBoxSideCenter(lockable.box, lockable.side);
			EnumFacing side = lockable.side;
			Vec3d point = center;
			double distanceMinimum = -1D;
			for(AxisAlignedBB box : boxes) for(EnumFacing side1 : EnumFacing.VALUES)
			{
				Vec3d point1 = LocksUtilities.getBoxSideCenter(box, side1).add(new Vec3d(side1.getDirectionVec()).scale(0.05D));
				double distance = center.squareDistanceTo(point1);
				if(distanceMinimum != -1D && distance >= distanceMinimum) continue;
				point = point1;
				distanceMinimum = distance;
				side = side1;
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate(point.x, point.y, point.z);
			GlStateManager.rotate(lockable.side.getHorizontalAngle(), 0F, 1F, 0F);
			if(lockable.side == EnumFacing.UP) GlStateManager.rotate(90F, 1F, 0F, 0F);
			if(lockable.side == EnumFacing.DOWN) GlStateManager.rotate(-90F, 1F, 0F, 0F);
			GlStateManager.scale(0.5D, 0.5D, 0.5D);
			mc.entityRenderer.enableLightmap();
			mc.getRenderItem().renderItem(new ItemStack(LocksItems.lock), TransformType.FIXED);
			mc.entityRenderer.disableLightmap();
			GlStateManager.popMatrix();
		}
		if(LocksConfiguration.client.enable_cui)
		{
			BlockPos position1 = LocksCapabilities.getLockBounds(mc.player).get();
			if(position1 != null)
			{
				BlockPos position2 = mc.objectMouseOver.getBlockPos() != null ? mc.objectMouseOver.getBlockPos() : position1;
				Box box = new Box(position1, position2);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(1F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.disableDepth();
				RenderGlobal.drawBoundingBox((double) box.x1, (double) box.y1, (double) box.z1, (double) box.x2, (double) box.y2, (double) box.z2, 0F, 1F, 0F, 1F);
				GlStateManager.enableDepth();
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
			}
		}
		GlStateManager.popMatrix();
	}
}