package mrfast.sbf.features.overlays;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Vector2d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class CrystalHollowsMap {
    // Map Asset Inspired by Skyblock Extra's
     public static final ResourceLocation MAP = new ResourceLocation("skyblockfeatures", "map/CrystalHollowsMap.png");
    public static final ResourceLocation PLAYER_ICON = new ResourceLocation("skyblockfeatures", "map/mapIcon.png");
    public static final ResourceLocation PLAYER_ICON2 = new ResourceLocation("skyblockfeatures", "map/mapIcon2.png");

    private static boolean loaded = false;
    private static boolean start = false;
    private static int ticks = 0;
    private static final HashMap<String, BlockPos> locations = new HashMap<>();
    private static final List<Vector2d> playerBreadcrumbs = new ArrayList<>();

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        locations.clear();
        playerBreadcrumbs.clear();
        loaded = false;
        ticks = 0;
        start = Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null && SkyblockFeatures.config.CrystalHollowsMap;
        if (!start) {
            return;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!start || Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null || !SkyblockFeatures.config.CrystalHollowsMap) {
            return;
        }

        if (SkyblockInfo.getInstance().getLocation() != null && !SkyblockInfo.getInstance().getMap().equals("Crystal Hollows")) {
            return;
        }

        ticks++;
        BlockPos location = Utils.GetMC().thePlayer.getPosition().down(1);
        if (ticks % 4 == 0) {
            Vector2d vector = new Vector2d((Utils.GetMC().thePlayer.posX - 202) / 4.9, (Utils.GetMC().thePlayer.posZ - 202) / 4.9);
            if (!playerBreadcrumbs.contains(vector)) {
                if (playerBreadcrumbs.size() > 1) {
                    if (playerBreadcrumbs.size() > 3000) {
                        playerBreadcrumbs.remove(0);
                    }
                    playerBreadcrumbs.add(vector);
                } else {
                    playerBreadcrumbs.add(vector);
                }
            }
            for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
                if (entity.hasCustomName() && entity.getCustomNameTag().contains("Corleone") && !locations.containsKey("§5Corleone") && Utils.GetMC().thePlayer.canEntityBeSeen(entity)) {
                    locations.put("§5Corleone", entity.getPosition().down(1));
                }
                if (entity.hasCustomName() && entity.getCustomNameTag().contains("Yolkar") && !locations.containsKey("§2King") && Utils.GetMC().thePlayer.canEntityBeSeen(entity)) {
                    locations.put("§2King", entity.getPosition().down(1));
                }
            }
        }

        if (ticks >= 40) {
            loaded = true;
            ticks = 0;
        }

        addLocation("lost precursor city", "§fCity", location);
        addLocation("khazaddm", "§cBal", location);
        addLocation("mines of divan", "§6Divan", location);
        addLocation("jungle temple", "§aTemple", location);
        addLocation("goblin queen's den", "§2Queen", location);
    }
    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        if (!start || Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null || !SkyblockFeatures.config.CrystalHollowsMap || SkyblockInfo.getInstance().getLocation() != null && !SkyblockInfo.getInstance().getMap().equals("Crystal Hollows")) return;

        GlStateManager.disableDepth();
        locations.forEach((locationName,pos)->{
            RenderUtil.drawWaypoint(pos,Color.green,locationName,event.partialTicks);
        });
        GlStateManager.enableDepth();
    }

    private void addLocation(String keyword, String locationName, BlockPos location) {
        String position = SkyblockFeatures.locationString.toLowerCase();
        if (position.contains(keyword) && !locations.containsKey(locationName)) {
            locations.put(locationName, location);
        }
    }

    static double lastPlayerX = 0;
    static double lastPlayerZ = 0;
    static double lastPlayerR = 0;

    static {
        new CHMap();
    }   
    public static class CHMap extends UIElement {
        public CHMap() {
            super("CrystalHollowsMap", new Point(0, 5));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            try {
                if (loaded && Minecraft.getMinecraft().thePlayer != null && Utils.inSkyblock && Minecraft.getMinecraft().theWorld != null && this.getToggled() && SkyblockInfo.getInstance().getMap().equals("Crystal Hollows")) {
                    GlStateManager.pushMatrix(); 
                        GlStateManager.enableBlend();
                        GlStateManager.color(1, 1, 1, 1);
                        GlStateManager.pushMatrix();
                            Utils.GetMC().getTextureManager().bindTexture(MAP);
                            Utils.drawTexturedRect(0, 0, 512/4,512/4, 0, 1, 0, 1, GL11.GL_NEAREST);
                        GlStateManager.popMatrix();

                        GlStateManager.pushMatrix();
                        
                        for(int i=1;i<playerBreadcrumbs.size();i++) {
                            if(i<playerBreadcrumbs.size()-1) {
                                Vector2d p1 = playerBreadcrumbs.get(i);
                                Vector2d p2 = playerBreadcrumbs.get(i+1);

                                double distance = Math.sqrt(Math.pow(Math.abs(p1.y - p2.y), 2) + Math.pow(Math.abs(p1.x - p2.x), 2));
                                if(distance<40)
                                Utils.drawLine((int) playerBreadcrumbs.get(i).x, (int) playerBreadcrumbs.get(i).y,(int)  playerBreadcrumbs.get(i+1).x,(int)  playerBreadcrumbs.get(i+1).y, new Color(0,0,0),5);
                            }
                        }
                        GlStateManager.popMatrix();
    
                        for(String name:locations.keySet()) {
                            ResourceLocation locationIcon = new ResourceLocation("skyblockfeatures","map/locations/"+Utils.cleanColor(name.toLowerCase())+".png");
                            BlockPos position = locations.get(name);
                            double locationX = Math.round((position.getX()-202)/4.9);
                            double locationZ = Math.round((position.getZ()-202)/4.9);
                            GlStateManager.color(1, 1, 1, 1);
                            Utils.GetMC().getTextureManager().bindTexture(locationIcon);
                            GlStateManager.pushMatrix();
                                Utils.drawTexturedRect((float)(locationX-3.5),(float) (locationZ-4), 7, 8, 0, 1, 0, 1, GL11.GL_NEAREST);
                                int textWidth = Utils.GetMC().fontRendererObj.getStringWidth(name);
                                GlStateManager.translate(locationX-textWidth/2, locationZ-10, 0);
                                Utils.drawTextWithStyle2(name,0, 0);
                                GlStateManager.translate(-locationX+textWidth/2, -locationZ+10, 0);
                            GlStateManager.popMatrix();
                        }

                        EntityPlayerSP player = Utils.GetMC().thePlayer;
                        double x = lastPlayerX;
                        double z = lastPlayerZ;
                        double newX = Math.round((player.posX-202)/4.9);
                        double newZ = Math.round((player.posZ-202)/4.9);
                        double deltaX = newX-x;
                        double deltaZ = newZ-z;

                        x+=deltaX/50;
                        z+=deltaZ/50;

                        lastPlayerX = x;
                        lastPlayerZ = z;
                        if(SkyblockFeatures.config.CrystalHollowsMapHeads) {
                            AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
                            ResourceLocation skin = aplayer.getLocationSkin();
                            GlStateManager.pushMatrix();
                            DrawHead(x, z, skin, (float) player.rotationYawHead);
                            GlStateManager.popMatrix();
                        } else {
                            GlStateManager.color(1, 1, 1, 1);
                            Utils.GetMC().getTextureManager().bindTexture(PLAYER_ICON);
                            GlStateManager.pushMatrix();
                                GlStateManager.translate(x, z, 0);
                                GlStateManager.rotate(player.rotationYawHead-180, 0, 0, 1);
                                GlStateManager.translate(-x, -z, 0);
                                Utils.drawTexturedRect((float)(x-2.5),(float) (z-3.5), 5, 7, 0, 1, 0, 1, GL11.GL_NEAREST);
                            GlStateManager.popMatrix();
                        }
                    GlStateManager.popMatrix();
                }
            } catch (Exception e) {
                //TODO: handle exception
            }
        }
        @Override
        public void drawElementExample() {
            GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.pushMatrix();
                    Utils.GetMC().getTextureManager().bindTexture(MAP);
                    Utils.drawTexturedRect(0, 0, 512/4,512/4, 0, 1, 0, 1, GL11.GL_NEAREST);
                GlStateManager.popMatrix();

                double x = Math.round((323-202)/4.9);
                double z = Math.round((621-202)/4.9);
                GlStateManager.pushMatrix();
                    Utils.GetMC().getTextureManager().bindTexture(PLAYER_ICON);
                    GlStateManager.translate(x, z, 0);
                    GlStateManager.rotate(-128, 0, 0, 1);
                    GlStateManager.translate(-x, -z, 0);
                    Utils.drawTexturedRect((float)(x-2.5),(float) (z-3.5), 5, 7, 0, 1, 0, 1, GL11.GL_NEAREST);
                GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.CrystalHollowsMap && Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return 128;
        }

        @Override
        public int getWidth() {
            return 128;
        }
    }

    public static void DrawHead(Double x,Double z,ResourceLocation skin, Float rotation) {
        GlStateManager.pushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(skin);

		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GlStateManager.translate(x, z, -0.02F);
		GlStateManager.rotate(rotation, 0.0F, 0.0F, 1.0F);
		
        GlStateManager.scale(0.75, 0.75, 1);
		Gui.drawRect(-8/2-1,-8/2-1, 8/2+1, 8/2+1, 0xff111111);
		GlStateManager.color(1, 1, 1, 1);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(-8/2f, 8/2f, 30).tex(8/64f, 8/64f).endVertex();
		worldrenderer.pos(8/2f, 8/2f, 30).tex(16/64f, 8/64f).endVertex();
		worldrenderer.pos(8/2f, -8/2f, 30).tex(16/64f, 16/64f).endVertex();
		worldrenderer.pos(-8/2f, -8/2f, 30).tex(8/64f, 16/64f).endVertex();
		tessellator.draw();

		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(-8/2f, 8/2f, 30+0.001f).tex(8/64f+0.5f, 8/64f).endVertex();
		worldrenderer.pos(8/2f, 8/2f, 30+0.001f).tex(16/64f+0.5f, 8/64f).endVertex();
		worldrenderer.pos(8/2f, -8/2f, 30+0.001f).tex(16/64f+0.5f, 16/64f).endVertex();
		worldrenderer.pos(-8/2f, -8/2f, 30+0.001f).tex(8/64f+0.5f, 16/64f).endVertex();
		tessellator.draw();
        
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}
}