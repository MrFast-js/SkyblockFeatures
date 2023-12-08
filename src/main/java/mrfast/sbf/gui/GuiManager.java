package mrfast.sbf.gui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GuiManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static File positionFile;
    public static Map<String, Point> GuiPositions;

    private static final Map<Integer, UIElement> elements = new HashMap<>();
    private int counter = 0;
    public static final Map<String, UIElement> names = new HashMap<>();

    public static String title = null;
    public static String subtitle = null;
    public static int titleDisplayTicks = 0;
    public static int subtitleDisplayTicks = 0;

    public GuiManager() {
        positionFile  = new File(SkyblockFeatures.modDir, "guipositions.json");
        GuiPositions = new HashMap<>();
        readConfig();
    }

    public void registerElement(UIElement e) {
        try {
            counter++;
            elements.put(counter, e);
            names.put(e.getName(), e);
        } catch(Exception err) {
            err.printStackTrace();
        }
    }

    public Map<Integer,UIElement> getElements() {
        return elements;
    }

    public static void readConfig() {
        JsonObject file;
        try (FileReader in = new FileReader(positionFile)) {
            file = gson.fromJson(in, JsonObject.class);
            for (Map.Entry<String, JsonElement> e : file.entrySet()) {
                try {
                    Point pnt = new Point(e.getValue().getAsJsonObject().get("x").getAsFloat(), e.getValue().getAsJsonObject().get("y").getAsFloat());
                    if(pnt.getX()<0) pnt.x = 0;
                    if(pnt.getY()<0) pnt.y = 0;
                    if(pnt.getX()>1) pnt.x = 0f;
                    if(pnt.getY()>1) pnt.y = 0f;
                    GuiPositions.put(e.getKey(),pnt);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } catch (Exception e) {
            GuiPositions = new HashMap<>();
            try (FileWriter writer = new FileWriter(positionFile)) {
                gson.toJson(GuiPositions, writer);
            } catch (Exception ignored) {

            }
        }
    }

    public static void saveConfig() {
        for (Map.Entry<String, UIElement> e : names.entrySet()) {
            // System.out.println("Saving "+e.getKey()+" "+e.getValue().getPos().getX()+" "+e.getValue().getPos().getY());
            GuiPositions.put(e.getKey(), e.getValue().getPos());
        }
        try (FileWriter writer = new FileWriter(positionFile)) {
            gson.toJson(GuiPositions, writer);
        } catch (Exception ignored) {

        }
    }

    @SubscribeEvent
    public void renderPlayerInfo(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE && event.type != RenderGameOverlayEvent.ElementType.JUMPBAR) return;
        if (Minecraft.getMinecraft().currentScreen instanceof EditLocationsGui || Minecraft.getMinecraft().theWorld == null) return;
        for(Map.Entry<Integer, UIElement> e : elements.entrySet()) {
            try {
                UIElement element = e.getValue();
                if(element.getToggled()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate((element.getX() * (Utils.GetMC().displayWidth / 2)), (element.getY() * (Utils.GetMC().displayHeight / 2)), 0);
                    element.drawElement();
                    GlStateManager.popMatrix();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        renderTitles(event.resolution);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (titleDisplayTicks > 0) {
            titleDisplayTicks--;
        } else {
            titleDisplayTicks = 0;
            GuiManager.title = null;
        }

        if (subtitleDisplayTicks > 0) {
            subtitleDisplayTicks--;
        } else {
            subtitleDisplayTicks = 0;
            GuiManager.subtitle = null;
        }
    }

    public static void createTitle(String title, int ticks) {
        Utils.playSound("random.orb", 0.5);
        GuiManager.title = title;
        GuiManager.titleDisplayTicks = ticks;
    }

    /**
     * Adapted from SkyblockAddons under MIT license
     * @link https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
     * @author BiscuitDevelopment
     */
    private void renderTitles(ScaledResolution scaledResolution) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || !Utils.inSkyblock) {
            return;
        }

        int scaledWidth = scaledResolution.getScaledWidth();
        int scaledHeight = scaledResolution.getScaledHeight();
        if (title != null) {
            int stringWidth = mc.fontRendererObj.getStringWidth(title);

            float scale = 4; // Scale is normally 4, but if its larger than the screen, scale it down...
            if (stringWidth * scale > (scaledWidth * 0.9F)) {
                scale = (scaledWidth * 0.9F) / (float) stringWidth;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale); // TODO Check if changing this scale breaks anything...

            mc.fontRendererObj.drawString(title, (float) (-mc.fontRendererObj.getStringWidth(title) / 2), -20.0F, 0xFF0000, true);

            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
        if (subtitle != null) {
            int stringWidth = mc.fontRendererObj.getStringWidth(subtitle);

            float scale = 2; // Scale is normally 2, but if its larger than the screen, scale it down...
            if (stringWidth * scale > (scaledWidth * 0.9F)) {
                scale = (scaledWidth * 0.9F) / (float) stringWidth;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);  // TODO Check if changing this scale breaks anything...

            mc.fontRendererObj.drawString(subtitle, -mc.fontRendererObj.getStringWidth(subtitle) / 2F, -23.0F, 0xFF0000, true);

            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

}
