package mrfast.sbf.features.overlays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;

public class RelicFinderWaypoints {

   public Minecraft mc = Minecraft.getMinecraft();
   
   public static HashSet<BlockPos> foundRelicLocations = new HashSet<>();

   @SubscribeEvent
   public void onRender(RenderWorldLastEvent event) {
      if(!SkyblockFeatures.config.spiderRelicHelper) return;

      Minecraft mc = Minecraft.getMinecraft();

      if (mc.theWorld != null) {
         Iterator<TileEntity> var3 = mc.theWorld.loadedTileEntityList.iterator();
         BlockPos closestOne = null;
         while(var3.hasNext()) {
            TileEntity entity = var3.next();
            if (entity instanceof TileEntitySkull) {
               TileEntitySkull skull = (TileEntitySkull) entity;
               BlockPos pos = entity.getPos();
               NBTTagCompound entityData = new NBTTagCompound();
               skull.writeToNBT(entityData);
               if(!entityData.toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmYwY2YxM2NiYjM2OGFmOWNlZTEyMzI5NzlhMTRiODRlNmViZjIyYzEzYjE0MjdmOTMxNmQ5NzRiY2UifX19")) continue;
               if(foundRelicLocations.contains(pos)) continue;
               GlStateManager.disableDepth();
               if(closestOne==null) {
                  closestOne = pos;
               } else {
                  if(mc.thePlayer.getDistanceSqToCenter(pos)<mc.thePlayer.getDistanceSqToCenter(closestOne)) {
                     closestOne = pos;
                  }
               }
               
               // Block
               highlightBlock(new Color(0x00AA00), pos.getX(), pos.getY(), pos.getZ(), 1.0D,event.partialTicks);
               // Tower
               AxisAlignedBB aabb2 = new AxisAlignedBB(pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5, pos.getX()+0.5, pos.getY()+100, pos.getZ()+0.5);
               RenderUtil.drawOutlinedFilledBoundingBox(aabb2, new Color(0x00AA00), event.partialTicks);
               GlStateManager.enableDepth();
            }
         }
        
         if(closestOne!=null) {
            if(mc.thePlayer.getDistanceSqToCenter(closestOne)<7) {
               foundRelicLocations.add(closestOne);
               closestOne = null;
            }
         }
      }
   }
   public static void highlightBlock(Color c, double d, double d1, double d2, double size,float ticks) {
      RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(d, d1, d2, d+size, d1+size, d2+size),c,ticks);
   }
}
