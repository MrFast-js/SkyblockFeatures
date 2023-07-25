package mrfast.skyblockfeatures.features.overlays;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import mrfast.skyblockfeatures.SkyblockFeatures;

import mrfast.skyblockfeatures.gui.components.UIElement;
import mrfast.skyblockfeatures.core.SkyblockInfo;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import mrfast.skyblockfeatures.gui.components.Point;
public class DwarvenMap {
    // Map Asset Inspired by Skyblock Extra's
    public static final ResourceLocation map = new ResourceLocation("skyblockfeatures","map/dwarven.png");
    public static final ResourceLocation playerIcon = new ResourceLocation("skyblockfeatures","map/mapIcon.png");

    static boolean loaded = false;
    static boolean start = false;   
    static int ticks = 0;
    public static HashMap<String,BlockPos> locations = new HashMap<>();
    @SubscribeEvent
    public void onload(WorldEvent.Load event) {
        try {
            locations.clear();
            loaded = false;
            ticks = 0;
            start = false;
            if(Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null && SkyblockFeatures.config.dwarvenMinesMap) {
                start = true;
            }
        } catch(Exception e) {
            
        }
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
        new DwarvenMapGui();
    }   
    public static class DwarvenMapGui extends UIElement {
        public DwarvenMapGui() {
            super("Dwarven Mines Map", new Point(0.4f, 0.4f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            try {
                if(SkyblockInfo.getInstance().getLocation()==null) return;
                if (loaded && Minecraft.getMinecraft().thePlayer != null && Utils.inSkyblock && Minecraft.getMinecraft().theWorld != null && SkyblockFeatures.config.dwarvenMinesMap && SkyblockInfo.getInstance().map.equals("Dwarven Mines")) {
                    GlStateManager.pushMatrix(); 
                        GlStateManager.enableBlend();
                        GlStateManager.color(1, 1, 1, 1);
                        GlStateManager.pushMatrix();
                            Utils.GetMC().getTextureManager().bindTexture(map);
                            Utils.drawTexturedRect(0, 0, 735/4,804/4, 0, 1, 0, 1, GL11.GL_NEAREST);
                        GlStateManager.popMatrix();
                        GlStateManager.pushMatrix();
                        GlStateManager.popMatrix();
                        EntityPlayerSP player = Utils.GetMC().thePlayer;
                        double x = lastPlayerX;
                        double z = lastPlayerZ;
                        double rotation = lastPlayerR;
                        System.out.println();
                        double newX = Math.round((player.posX+245)/2.5);
                        double newZ = Math.round((player.posZ+170)/2.5);
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
                            Utils.drawTexturedRect((float)(x-2.5),(float) (z-3.5), 5, 7, 0, 1, 0, 1, GL11.GL_NEAREST);
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
                    Utils.drawTexturedRect(0, 0, 735/4,804/4, 0, 1, 0, 1, GL11.GL_NEAREST);
                GlStateManager.popMatrix();

                double x = Math.round((323-202)/4.9);
                double z = Math.round((621-202)/4.9);
                GlStateManager.pushMatrix();
                    Utils.GetMC().getTextureManager().bindTexture(playerIcon);
                    GlStateManager.translate(x, z, 0);
                    GlStateManager.rotate(-128, 0, 0, 1);
                    GlStateManager.translate(-x, -z, 0);
                    Utils.drawTexturedRect((float)(x-2.5),(float) (z-3.5), 5, 7, 0, 1, 0, 1, GL11.GL_NEAREST);
                GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.dwarvenMinesMap && Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return 804/4;
        }

        @Override
        public int getWidth() {
            return 735/4;
        }
    }
}