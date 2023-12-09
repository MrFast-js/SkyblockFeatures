package mrfast.sbf.features.dungeons;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.OutlineUtils;
import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.Utils;

import net.minecraft.client.renderer.entity.RenderManager;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Nametags {

    public Minecraft mc = Minecraft.getMinecraft();
    public static Set<EntityPlayer> players = new HashSet<>();

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        players.clear();
    }
    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        if(!Utils.inDungeons) return;
        try {
            List<String> sidebarLines = ScoreboardUtil.getSidebarLines();
            RenderManager renderManager = Utils.GetMC().getRenderManager();

            for (EntityPlayer player : mc.theWorld.playerEntities) {
                double x = interpolate(player.lastTickPosX, player.posX, event.partialTicks) - renderManager.viewerPosX;
                double y = interpolate(player.lastTickPosY, player.posY, event.partialTicks) - renderManager.viewerPosY;
                double z = interpolate(player.lastTickPosZ, player.posZ, event.partialTicks) - renderManager.viewerPosZ;

                String cutShort = player.getName().substring(0, Math.min(12, player.getName().length()));

                for (String cleanedLine : sidebarLines) {
                    String classTag = getClassTag(cleanedLine);
                    if (classTag != null && cleanedLine.contains("[" + classTag + "] " + cutShort)) {
                        if(player != Utils.GetMC().thePlayer && SkyblockFeatures.config.glowingDungeonPlayers) {
                            OutlineUtils.renderOutline(player,getColorForClass(classTag),true);
                        }

                        if(SkyblockFeatures.config.NameTags) {
                            renderNameTag(player, ChatFormatting.YELLOW + "[" + classTag + "] " + ChatFormatting.GREEN + player.getName(), x, y, z);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getClassTag(String cleanedLine) {
        // Example: "[M] Skyblock_Lobby" -> "M"
        if(cleanedLine.contains("[") && cleanedLine.indexOf("[") < cleanedLine.indexOf("]")) {
            return cleanedLine.substring(cleanedLine.indexOf("[") + 1, cleanedLine.indexOf("]"));
        }
        return null;
    }

    private Color getColorForClass(String classTag) {
        switch (classTag) {
            case "M":
                return Color.cyan;
            case "T":
                return Color.gray;
            case "A":
                return Color.green;
            case "B":
                return new Color(255,85,85);
            case "H":
                return Color.magenta;
            default:
                return Color.white; // Default color if no match is found
        }
    }

    private double interpolate(double previous, double current, float delta) {
        return (previous + (current - previous) * delta);
    }

    private void renderNameTag(EntityPlayer player, String displayName, double x, double y, double z) {
        if(player.equals(Utils.GetMC().thePlayer)) return;
        players.add(player);

        float f = 1.6F;
		float f1 = 0.016666668F * f;

        Entity renderViewEntity = mc.getRenderViewEntity();

        double distanceScale = Math.max(1, renderViewEntity.getPositionVector().distanceTo(player.getPositionVector()) / 10F);

        Minecraft mc = Minecraft.getMinecraft();
        int iconSize = 25;

        if (player.isSneaking()) {
            y -= 0.65F;
        }

        y += player.height;

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.translate(x, y+distanceScale, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-f1, -f1, f1);

        GlStateManager.scale(distanceScale, distanceScale, distanceScale);

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableAlpha();

        mc.fontRendererObj.drawString(displayName, -mc.fontRendererObj.getStringWidth(displayName) / 2F, iconSize / 2F + 13, -1, true);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }
}
