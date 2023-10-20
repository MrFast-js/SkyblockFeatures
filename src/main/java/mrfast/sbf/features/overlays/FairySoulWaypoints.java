package mrfast.sbf.features.overlays;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FairySoulWaypoints {

   public Minecraft mc = Minecraft.getMinecraft();

   @SubscribeEvent
   public void onAttack(AttackEntityEvent event) {
      if(!SkyblockFeatures.config.fairy) return;
      
      if (event.target != null && event.target instanceof EntityArmorStand && ((EntityArmorStand)event.target).getCurrentArmor(3) != null && !soullocations.contains(event.target.getPosition().toString())) {
         Boolean fairySoul = ((EntityArmorStand)event.target).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk2OTIzYWQyNDczMTAwMDdmNmFlNWQzMjZkODQ3YWQ1Mzg2NGNmMTZjMzU2NWExODFkYzhlNmIyMGJlMjM4NyJ9fX0="); 
         Boolean enigma = ((EntityArmorStand)event.target).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTMwZmU3NzFmY2MzZWNjMDUzMGVlOTU0NWFiMDc3OTc0MzdmOTVlMDlhMGVhYTliNTEyNDk3ZmU4OTJmNTJmYiJ9fX0="); 

         if(!(fairySoul||enigma)) return;
         soullocations.add(event.target.getPosition().toString());
         writeSave();
      }
   }
   
   public static HashSet<String> soullocations = new HashSet<>();

   public FairySoulWaypoints() {
      saveFile = new File(SkyblockFeatures.modDir, "fairysouls.json");
      reloadSave();
   }

   private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private static File saveFile;

   public static String[] getStringArrayFromJsonArray(JsonArray jsonArray) {
      int arraySize = jsonArray.size();
      String[] stringArray = new String[arraySize];

      for (int i = 0; i < arraySize; i++) {
          stringArray[i] = jsonArray.get(i).getAsString();
      }

      return stringArray;
  }

   public static void reloadSave() {
      soullocations.clear();
      JsonArray dataArray;
      try (FileReader in = new FileReader(saveFile)) {
         dataArray = gson.fromJson(in, JsonArray.class);
         soullocations.addAll(Arrays.asList(getStringArrayFromJsonArray(dataArray)));
      } catch (Exception e) {
         dataArray = new JsonArray();
         try (FileWriter writer = new FileWriter(saveFile)) {
            gson.toJson(dataArray, writer);
         } catch (Exception ex) {
            ex.printStackTrace();
         }
      }
   }
   
   public static void writeSave() {
      try (FileWriter writer = new FileWriter(saveFile)) {
         JsonArray arr = new JsonArray();
         for (String itemId : soullocations) {
            arr.add(new JsonPrimitive(itemId));
         }
         gson.toJson(arr, writer);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   @SubscribeEvent
   public void onRender(RenderWorldLastEvent event) {
      Minecraft mc = Minecraft.getMinecraft();

      if (mc.theWorld != null && Utils.inSkyblock) {
         if(SkyblockFeatures.config.fairy) {
            Iterator<Entity> var3 = mc.theWorld.loadedEntityList.iterator();
            
            GlStateManager.disableDepth();
            while(var3.hasNext()) {
               Entity entity = (Entity)var3.next();
               if (entity instanceof EntityArmorStand ) {
                  if(((EntityArmorStand)entity).getCurrentArmor(3) != null && SkyblockFeatures.config.fairy) {
                     String id = ((EntityArmorStand)entity).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString();
                     
                     if(id.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk2OTIzYWQyNDczMTAwMDdmNmFlNWQzMjZkODQ3YWQ1Mzg2NGNmMTZjMzU2NWExODFkYzhlNmIyMGJlMjM4NyJ9fX0=")) {
                        if (!soullocations.contains(entity.getPosition().toString())) {
                           highlightBlock(SkyblockFeatures.config.fairySoulUnFound, entity.posX-0.5D, 1.5D+entity.posY, entity.posZ-0.5D, 1.0D,event.partialTicks);
                        } else {
                           highlightBlock(SkyblockFeatures.config.fairySoulFound, entity.posX-0.5D, 1.5D + entity.posY, entity.posZ-0.5D, 1.0D,event.partialTicks);
                        }
                     }
                  }
               }
            }
            GlStateManager.enableDepth();
         }
         if(SkyblockFeatures.config.riftSouls) {
            Iterator<Entity> var3 = mc.theWorld.loadedEntityList.iterator();

            while(var3.hasNext()) {
               Entity entity = (Entity)var3.next();
               if (entity instanceof EntityArmorStand ) {
                  GlStateManager.disableDepth();
                  // Highlight
                  if(((EntityArmorStand)entity).getCurrentArmor(3) != null && SkyblockFeatures.config.riftSouls) {
                     String id = ((EntityArmorStand)entity).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString();
                     if(id.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTMwZmU3NzFmY2MzZWNjMDUzMGVlOTU0NWFiMDc3OTc0MzdmOTVlMDlhMGVhYTliNTEyNDk3ZmU4OTJmNTJmYiJ9fX0=")) {
                        if (!soullocations.contains(entity.getPosition().toString())) {
                           highlightBlock(SkyblockFeatures.config.riftSoulUnFound, entity.posX-0.5D, 1.5D+entity.posY, entity.posZ-0.5D, 1.0D,event.partialTicks);
                        } else {
                           highlightBlock(SkyblockFeatures.config.riftSoulFound, entity.posX-0.5D, 1.5D + entity.posY, entity.posZ-0.5D, 1.0D,event.partialTicks);
                        }
                     }
                  }
                  GlStateManager.enableDepth();
                  // Highlight Odanta
                  if(((EntityArmorStand)entity).getEquipmentInSlot(0)!=null && SkyblockFeatures.config.riftSouls) {
                     String id = ((EntityArmorStand)entity).getEquipmentInSlot(0).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString();
                     if(id.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZkODA2ZGVmZGZkZjU5YjFmMjYwOWM4ZWUzNjQ2NjZkZTY2MTI3YTYyMzQxNWI1NDMwYzkzNThjNjAxZWY3YyJ9fX0=")) {
                        highlightBlock(new Color(85,255,255), entity.posX-0.5D, 0.5D+entity.posY, entity.posZ-0.5D, 1.0D,event.partialTicks);
                     }
                  }
               } 

            }
         }
      }
   }


   public static void highlightBlock(Color c, double d, double d1, double d2, double size,float ticks) {
      RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(d, d1, d2, d+size, d1+size, d2+size),c,ticks);
   }
}
