package mrfast.sbf.features.overlays.maps;

import java.util.HashMap;

import mrfast.sbf.utils.GuiUtils;
import org.lwjgl.opengl.GL11;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
public class CrimsonMap {
    public static final ResourceLocation map = new ResourceLocation("skyblockfeatures","map/crimson.png");
    public static final ResourceLocation playerIcon = new ResourceLocation("skyblockfeatures","map/mapIcon.png");

    static boolean loaded = false;
    static boolean start = false;   
    static int ticks = 0;
    public static HashMap<String,BlockPos> locations = new HashMap<>();
    @SubscribeEvent
    public void onload(WorldEvent.Load event) {
        locations.clear();
        loaded = false;
        ticks = 0;
        start = Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null && SkyblockFeatures.config.dwarvenMinesMap;
    }
    
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(start && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null && SkyblockFeatures.config.dwarvenMinesMap) {
            ticks++;
            if(ticks >= 40) {
                loaded = true;
                ticks = 0;
            }
        }
    }

    static double lastPlayerX = 0;
    static double lastPlayerZ = 0;
    static double lastPlayerR = 0;

    static {
        new CrimsonIslesMap();
    }   
    public static class CrimsonIslesMap extends UIElement {
        public CrimsonIslesMap() {
            super("Crimson Isles Map", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            try {
                if(SkyblockInfo.getLocation()==null) return;
                if (loaded && Minecraft.getMinecraft().thePlayer != null && Utils.inSkyblock && Minecraft.getMinecraft().theWorld != null && SkyblockFeatures.config.crimsonsIslesMap) {
                    GlStateManager.pushMatrix(); 
                        GlStateManager.enableBlend();
                        GlStateManager.color(1, 1, 1, 1);
                        GlStateManager.pushMatrix();
                            Utils.GetMC().getTextureManager().bindTexture(map);
                            GuiUtils.drawTexturedRect(0, 0, 1653/8,1429/8, 0, 1, 0, 1, GL11.GL_NEAREST);
                        GlStateManager.popMatrix();
                        GlStateManager.pushMatrix();
                        GlStateManager.popMatrix();
                        EntityPlayerSP player = Utils.GetMC().thePlayer;
                        double x = lastPlayerX;
                        double z = lastPlayerZ;
                        double rotation = lastPlayerR;

                        double newX = Math.round((player.posX+360)/2+821/4)/2;
                        double newZ = Math.round((player.posZ+423)/2+1391/4)/2;
                        double newRotation = player.rotationYawHead;

                        double deltaX = newX-x;
                        double deltaZ = newZ-z;
                        double deltaR = newRotation-rotation;

                        x+=deltaX/50;
                        z+=deltaZ/50;
                        rotation+=deltaR/50;

                        lastPlayerX = x;
                        lastPlayerZ = z;
                        lastPlayerR = rotation;

                        GlStateManager.color(1, 1, 1, 1);
                        Utils.GetMC().getTextureManager().bindTexture(playerIcon);
                        GlStateManager.pushMatrix();
                            GlStateManager.translate(x, z, 0);
                            GlStateManager.rotate(player.rotationYawHead-180, 0, 0, 1);
                            GlStateManager.translate(-x, -z, 0);
                            GuiUtils.drawTexturedRect((float)(x-2.5),(float) (z-3.5), 5, 7, 0, 1, 0, 1, GL11.GL_NEAREST);
                        GlStateManager.popMatrix();
                    GlStateManager.popMatrix();
                }
            } catch (Exception e) {
                e.printStackTrace();
                //TODO: handle exception
            }
        }
        @Override
        public void drawElementExample() {
            GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.pushMatrix();
                    Utils.GetMC().getTextureManager().bindTexture(map);
                    GuiUtils.drawTexturedRect(0, 0, 1653/8,1429/8, 0, 1, 0, 1, GL11.GL_NEAREST);
                GlStateManager.popMatrix();

                double x = Math.round((323-202)/4.9);
                double z = Math.round((621-202)/4.9);
                GlStateManager.pushMatrix();
                    Utils.GetMC().getTextureManager().bindTexture(playerIcon);
                    GlStateManager.translate(x, z, 0);
                    GlStateManager.rotate(-128, 0, 0, 1);
                    GlStateManager.translate(-x, -z, 0);
                    GuiUtils.drawTexturedRect((float)(x-2.5),(float) (z-3.5), 5, 7, 0, 1, 0, 1, GL11.GL_NEAREST);
                GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.crimsonsIslesMap && Utils.inSkyblock && SkyblockInfo.map.equals("Crimson Isle");
        }

        @Override
        public int getHeight() {
            return 1429/8;
        }

        @Override
        public int getWidth() {
            return 1653/8;
        }
    }
}