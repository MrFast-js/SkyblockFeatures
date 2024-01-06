package mrfast.sbf.features.overlays;

import java.awt.Color;
import java.util.Iterator;

import com.google.gson.*;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.DataManager;
import mrfast.sbf.events.ProfileSwapEvent;
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

    JsonObject fairySouls = new JsonObject();
    JsonObject enigmaSouls = new JsonObject();

    @SubscribeEvent
    public void onProfileSwap(ProfileSwapEvent event) {
        fairySouls = (JsonObject) DataManager.getProfileDataDefault("collectedFairySouls", new JsonObject());
        enigmaSouls = (JsonObject) DataManager.getProfileDataDefault("collectedEnigmaSouls", new JsonObject());
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (event.target instanceof EntityArmorStand && ((EntityArmorStand) event.target).getCurrentArmor(3) != null) {
            boolean fairySoul = ((EntityArmorStand) event.target).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk2OTIzYWQyNDczMTAwMDdmNmFlNWQzMjZkODQ3YWQ1Mzg2NGNmMTZjMzU2NWExODFkYzhlNmIyMGJlMjM4NyJ9fX0=");
            boolean enigma = ((EntityArmorStand) event.target).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTMwZmU3NzFmY2MzZWNjMDUzMGVlOTU0NWFiMDc3OTc0MzdmOTVlMDlhMGVhYTliNTEyNDk3ZmU4OTJmNTJmYiJ9fX0=");

            if(fairySoul) {
                fairySouls.addProperty(event.target.getPosition().toString(),true);
                DataManager.saveProfileData("collectedFairySouls",fairySouls);
            }
            if(enigma) {
                enigmaSouls.addProperty(event.target.getPosition().toString(),true);
                DataManager.saveProfileData("collectedEnigmaSouls",enigmaSouls);
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld != null && Utils.inSkyblock) {
            if (SkyblockFeatures.config.fairySoulHelper) {
                Iterator<Entity> var3 = mc.theWorld.loadedEntityList.iterator();

                GlStateManager.disableDepth();
                while (var3.hasNext()) {
                    Entity entity = var3.next();
                    if (entity instanceof EntityArmorStand) {
                        if (((EntityArmorStand) entity).getCurrentArmor(3) != null && SkyblockFeatures.config.fairySoulHelper) {
                            String id = ((EntityArmorStand) entity).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString();

                            if (id.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk2OTIzYWQyNDczMTAwMDdmNmFlNWQzMjZkODQ3YWQ1Mzg2NGNmMTZjMzU2NWExODFkYzhlNmIyMGJlMjM4NyJ9fX0=")) {
                                if (!fairySouls.has(entity.getPosition().toString())) {
                                    highlightBlock(SkyblockFeatures.config.fairySoulUnfound, entity.posX - 0.5D, 1.5D + entity.posY, entity.posZ - 0.5D, 1.0D, event.partialTicks);
                                } else {
                                    highlightBlock(SkyblockFeatures.config.fairySoulFound, entity.posX - 0.5D, 1.5D + entity.posY, entity.posZ - 0.5D, 1.0D, event.partialTicks);
                                }
                            }
                        }
                    }
                }
                GlStateManager.enableDepth();
            }

            if (SkyblockFeatures.config.riftSouls || SkyblockFeatures.config.highlightOdanta) {

                for (Entity entity : mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityArmorStand) {
                        GlStateManager.disableDepth();
                        // Highlight
                        if (((EntityArmorStand) entity).getCurrentArmor(3) != null && SkyblockFeatures.config.riftSouls) {
                            String id = ((EntityArmorStand) entity).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString();
                            if (id.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTMwZmU3NzFmY2MzZWNjMDUzMGVlOTU0NWFiMDc3OTc0MzdmOTVlMDlhMGVhYTliNTEyNDk3ZmU4OTJmNTJmYiJ9fX0=")) {
                                if (!enigmaSouls.has(entity.getPosition().toString())) {
                                    highlightBlock(SkyblockFeatures.config.riftSoulUnfound, entity.posX - 0.5D, 1.5D + entity.posY, entity.posZ - 0.5D, 1.0D, event.partialTicks);
                                } else {
                                    highlightBlock(SkyblockFeatures.config.riftSoulFound, entity.posX - 0.5D, 1.5D + entity.posY, entity.posZ - 0.5D, 1.0D, event.partialTicks);
                                }
                            }
                        }
                        GlStateManager.enableDepth();
                        // Highlight Odanta
                        if (((EntityArmorStand) entity).getEquipmentInSlot(0) != null && SkyblockFeatures.config.highlightOdanta) {
                            String id = ((EntityArmorStand) entity).getEquipmentInSlot(0).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString();
                            if (id.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZkODA2ZGVmZGZkZjU5YjFmMjYwOWM4ZWUzNjQ2NjZkZTY2MTI3YTYyMzQxNWI1NDMwYzkzNThjNjAxZWY3YyJ9fX0=")) {
                                highlightBlock(new Color(85, 255, 255), entity.posX - 0.5D, 0.5D + entity.posY, entity.posZ - 0.5D, 1.0D, event.partialTicks);
                            }
                        }
                    }

                }
            }
        }
    }


    public static void highlightBlock(Color c, double d, double d1, double d2, double size, float ticks) {
        RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(d, d1, d2, d + size, d1 + size, d2 + size), c, ticks);
    }
}
