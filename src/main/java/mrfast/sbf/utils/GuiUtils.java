package mrfast.sbf.utils;

import com.mojang.realmsclient.gui.ChatFormatting;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.universal.UMatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiUtils {

    public enum TextStyle {
        DROP_SHADOW(),
        BLACK_OUTLINE();

        TextStyle() {}
    }

    public static void drawText(String text, float x, float y,TextStyle style) {
        boolean bold = text.contains(ChatFormatting.BOLD.toString());
        String shadowText = Utils.cleanColor(text);
        if(bold) shadowText=ChatFormatting.BOLD+shadowText;

        if(style.equals(TextStyle.BLACK_OUTLINE)) {
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x + 1, y, 0x000000, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x - 1, y, 0x000000, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x, y + 1, 0x000000, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x, y - 1, 0x000000, false);
        }
        // Main Text
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, 0xFFFFFF, style.equals(TextStyle.DROP_SHADOW));
    }

    public static void drawTextLines(List<String> lines, float x, float y, TextStyle style) {
        int index = 0;
        for (String line : lines) {
            GuiUtils.drawText(line, x, y + index * (Utils.GetMC().fontRendererObj.FONT_HEIGHT+1),style);
            index++;
        }
    }

    public static void drawSideMenu(List<String> lines, TextStyle style) {
        drawSideMenu(lines, style,false);
    }

    public static void drawSideMenu(List<String> lines, TextStyle style,int x,int y) {
        drawSideMenu(lines, style,false,x,y);
    }

    public static void drawSideMenu(List<String> lines, TextStyle style,boolean leftSide) {
        drawSideMenu(lines, style,leftSide,0,0);
    }

    public static void drawSideMenu(List<String> lines, TextStyle style,boolean leftSide,int xPos,int y) {
        int x=xPos==0?180:xPos; // width of chest gui

        int maxLineLength = 0;

        for (String line : lines) {
            if(line.length()>maxLineLength) maxLineLength=line.length();
        }
        int boxWidth = maxLineLength * 5;
        if(leftSide) x = -boxWidth - 7;

        drawGraySquareWithBorder(x, y, maxLineLength * 5, (lines.size()+1)*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1));
        drawTextLines(lines,x+7, y +7,style);
    }

    /**
     * Taken from NotEnoughUpdates under GNU Lesser General Public License v3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/COPYING
     * @author Moulberry
     */
    public static void drawTexturedRect(float x, float y, float width, float height, float uMin, float uMax, float vMin, float vMax, int filter) {
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        drawTexturedRectNoBlend(x, y, width, height, uMin, uMax, vMin, vMax, filter);
        GlStateManager.disableBlend();
    }

    /**
     * Taken from NotEnoughUpdates under GNU Lesser General Public License v3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/COPYING
     * @author Moulberry
     */
    public static void drawTexturedRectNoBlend(float x, float y, float width, float height, float uMin, float uMax, float vMin, float vMax, int filter) {
        GlStateManager.enableTexture2D();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer
                .pos(x, y+height, 0.0D)
                .tex(uMin, vMax).endVertex();
        worldrenderer
                .pos(x+width, y+height, 0.0D)
                .tex(uMax, vMax).endVertex();
        worldrenderer
                .pos(x+width, y, 0.0D)
                .tex(uMax, vMin).endVertex();
        worldrenderer
                .pos(x, y, 0.0D)
                .tex(uMin, vMin).endVertex();
        tessellator.draw();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    }

    public static void drawLine(int x1, int y1, int x2, int y2, Color color, float width) {
        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend(); //disabled means no opacity
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Vector2f vec = new Vector2f(x2 - x1, y2 - y1);
        vec.normalise(vec);
        Vector2f side = new Vector2f(vec.x, -vec.y);

        GL11.glLineWidth(width);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(),(float) 0.3);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x1 - side.x + side.x, y1 - side.y + side.y, 0.0D).endVertex();
        worldrenderer.pos(x2 - side.x + side.x, y2 - side.y + side.y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    /**
     * Taken from NotEnoughUpdates under GNU Lesser General Public License v3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/COPYING
     * @author Moulberry
     */
    public static void drawLineInGui(int x1, int y1, int x2, int y2,Color color,float width,double d) {
        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.translate(0, 0, 700);

        Vector2f vec = new Vector2f(x2 - x1, y2 - y1);
        vec.normalise(vec);
        Vector2f side = new Vector2f(vec.x, -vec.y);

        GL11.glLineWidth(width);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(),(float) d);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x1 - side.x + side.x, y1 - side.y + side.y, 0.0D).endVertex();
        worldrenderer.pos(x2 - side.x + side.x, y2 - side.y + side.y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.translate(0, 0, -700);
        GlStateManager.enableTexture2D();
    }

    public static void drawGraySquareWithBorder(int x,int y,int width,int height) {
        UIRoundedRectangle.Companion.drawRoundedRectangle(new UMatrixStack(),x, y, x+width, height+2, 5, new Color(0,0,0,125));
        UIRoundedRectangle.Companion.drawRoundedRectangle(new UMatrixStack(),x-2, y-2, x+width+2, height+2+2, 5, new Color(55,55,55,125));
    }
    public static void drawGraySquare(int x,int y,int width,int height, boolean hovered) {
        int backgroundColor = new Color(25, 25, 25, hovered ? 200 : 159).getRGB();
        int borderColor = new Color(100, 100, 100, 220).getRGB();
        if(hovered) borderColor = new Color(150, 150, 150,  220).getRGB();

        // Draw the filled square
        Gui.drawRect(x, y, x + width, y + height, backgroundColor);

        // Draw the border without overlapping on the corners
        Gui.drawRect(x, y, x + width, y + 1, borderColor);  // Top
        Gui.drawRect(x, y + 1, x + 1, y + height - 1, borderColor);  // Left
        Gui.drawRect(x + width - 1, y + 1, x + width, y + height - 1, borderColor);  // Right
        Gui.drawRect(x, y + height - 1, x + width, y + height, borderColor);  // Bottom
    }

    public static void drawText(String string, int x, int y) {
        Utils.GetMC().fontRendererObj.drawString(string, x, y, 0xFFFFFF, true);
    }

    public static int lastGuiScale = 0;

    public static void saveGuiScale() {
        lastGuiScale = Utils.GetMC().gameSettings.guiScale;
        Utils.GetMC().gameSettings.guiScale = 2;
    }
    public static void openGui(GuiScreen screen) {
        Utils.setTimeout(()->{
            Utils.GetMC().displayGuiScreen(screen);
        },50,true);
    }
}
