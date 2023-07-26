package mrfast.sbf.features.dungeons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.Utils;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Nametags {

    public Minecraft mc = Minecraft.getMinecraft();
    public RenderGlobal renderGlobal = mc.renderGlobal;
    public static Map<EntityPlayer, String> players = new HashMap<EntityPlayer, String>();
    public static List<Vec3> NPCs = new ArrayList<>();

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        try {
            players.clear();
            NPCs.clear();
        } catch(Exception e) {

        }
    }

    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        if(!SkyblockFeatures.config.NameTags || !Utils.inDungeons) return;
        try {
            for(EntityPlayer player : Utils.GetMC().theWorld.playerEntities) {
                double x = interpolate(player.lastTickPosX, player.posX, event.partialTicks) - Utils.GetMC().getRenderManager().viewerPosX;
                double y = interpolate(player.lastTickPosY, player.posY, event.partialTicks) - Utils.GetMC().getRenderManager().viewerPosY;
                double z = interpolate(player.lastTickPosZ, player.posZ, event.partialTicks) - Utils.GetMC().getRenderManager().viewerPosZ;
                // renderNameTag(player, ChatFormatting.GREEN+player.getName(), x , y, z, event.partialTicks);
                for (String line : ScoreboardUtil.getSidebarLines()) {
                    String cleanedLine = ScoreboardUtil.cleanSB(line);
                    String cutShort = player.getName();
                    if(cutShort.length()>12) {
                        cutShort = cutShort.substring(0, 12);
                    }
                    
                    if(cleanedLine.contains("[M] "+cutShort)) {// MAGE CLASS "[M] Skyblock_Lobby"
                        renderNameTag(player, ChatFormatting.YELLOW+"[M] "+ChatFormatting.GREEN+player.getName(), x , y, z, event.partialTicks, "§b");
                    }
                    if(cleanedLine.contains("[T] "+cutShort)) {// TANK CLASS "[T] Skyblock_Lobby"
                        renderNameTag(player, ChatFormatting.YELLOW+"[T] "+ChatFormatting.GREEN+player.getName(), x , y, z, event.partialTicks, "§7");
                    }
                    if(cleanedLine.contains("[A] "+cutShort)) {// ARCHER CLASS "[A] Skyblock_Lobby"
                        renderNameTag(player, ChatFormatting.YELLOW+"[A] "+ChatFormatting.GREEN+player.getName(), x , y, z, event.partialTicks, "§a");
                    }
                    if(cleanedLine.contains("[B] "+cutShort)) {// BESERKER CLASS "[B] Skyblock_Lobby"
                        renderNameTag(player, ChatFormatting.YELLOW+"[B] "+ChatFormatting.GREEN+player.getName(), x , y, z, event.partialTicks, "§c");
                    }
                    if(cleanedLine.contains("[H] "+cutShort)) {// HEALER CLASS "[H] Skyblock_Lobby"
                        renderNameTag(player, ChatFormatting.YELLOW+"[H] "+ChatFormatting.GREEN+player.getName(), x , y, z, event.partialTicks, "§d");
                    }
                }
            }
        } catch (Exception e) {
            //TODO: handle exception
        }
    }

    private double interpolate(double previous, double current, float delta) {
        return (previous + (current - previous) * delta);
    }

    private void renderNameTag(EntityPlayer player, String displayName, double x, double y, double z, float delta, String color) {
        if(player.equals(Utils.GetMC().thePlayer)) return;
        
        players.put(player, color);

        ////////////////////////////////////////////////
        // NAME TAGS
        ////////////////////////////////////////////////

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
    }

    public static double getDistanceSquared(Vec3 npcLocation, Entity entityToCheck) {
        double d0 = npcLocation.xCoord - entityToCheck.posX;
        double d1 = npcLocation.yCoord - entityToCheck.posY;
        double d2 = npcLocation.zCoord - entityToCheck.posZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

}
