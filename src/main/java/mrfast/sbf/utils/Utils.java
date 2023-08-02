package mrfast.sbf.utils;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import com.mojang.realmsclient.gui.ChatFormatting;

import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.universal.UMatrixStack;
import mrfast.sbf.gui.ProfileViewerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class Utils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean inSkyblock = false;
    public static boolean inDungeons = false;
    public static int Health;
    public static int maxHealth;

    public static int Mana;
    public static int maxMana;

    public static int Defence;
    public static Utils INSTANCE = new Utils();
    public Map<UUID, Boolean> glowingCache = new HashMap<>();
    static Random random = new Random();
    public static final NumberFormat nf = NumberFormat.getInstance(Locale.US);

    private static final String[] steps = new String[] {"", "K", "M", "B","T"};

    public static String formatNumber(double number) {
        int magnitudeIndex = 0;

        while (number >= 1000) {
            magnitudeIndex++;
            number /= 1000;
        }

        String formattedNumber;

        if (magnitudeIndex > 0 && Math.floor(number) == number) {
            formattedNumber = String.valueOf((int) number);
        } else {
            formattedNumber = round(number, 1);
        }

        return formattedNumber + steps[magnitudeIndex];
    }

    public static String percentOf(Double num,Double OutOf) {
		double lowPercent = num/OutOf;
		double percent = Math.floor(lowPercent*10000)/100;
		return ChatFormatting.GRAY+" ("+percent+"%)";
	}
	public static String percentOf(long num, long outOf) {
		double percent = (double) num / outOf * 100;
		return ChatFormatting.GRAY + " (" + String.format("%.2f", percent) + "%)";
	}
	public static String percentOf(Integer num,Integer OutOf) {
		Double lowPercent = num.doubleValue()/OutOf.doubleValue();
		Double percent = Math.floor(lowPercent*10000)/100;
		return ChatFormatting.GRAY+" ("+percent+"%)";
	}
    
    public static boolean isOnHypixel() {
        try {
            if (mc != null && mc.theWorld != null && !mc.isSingleplayer()) {
                if (mc.thePlayer != null && mc.thePlayer.getClientBrand() != null) {
                    if (mc.thePlayer.getClientBrand().toLowerCase().contains("hypixel")) return true;
                }
                if (mc.getCurrentServerData() != null) return mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel");
            }
            return false;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Taken from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
    */
    public static void checkForSkyblock() {
        try {
            if (isOnHypixel()) {
                if(mc.theWorld.getScoreboard() == null) return;
                ScoreObjective scoreboardObj = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
                if (scoreboardObj != null) {
                    String scObjName = ScoreboardUtil.cleanSB(scoreboardObj.getDisplayName());
                    if (scObjName.contains("SKYBLOCK")) {
                        inSkyblock = true;
                        return;
                    }
                }
            }
            inSkyblock = false;
        } catch (NoSuchMethodError e) {
            //TODO: handle exception
        }
    }

    public static String cleanColor(String in) {
        return in.replaceAll("(?i)\\u00A7.", "");
    }
    
    public static Minecraft GetMC() {
        return mc;
    }

    public static void drawTexturedRect(float x, float y, float width, float height, float uMin, float uMax, float vMin, float vMax, int filter) {
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        drawTexturedRectNoBlend(x, y, width, height, uMin, uMax, vMin, vMax, filter);
        GlStateManager.disableBlend();
    }

     public static String convertToTitleCase(String input) {
        String[] words = input.split("_");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (!word.isEmpty()) {
                String firstCharacter = word.substring(0, 1);
                String remainingCharacters = word.substring(1).toLowerCase();

                if (i > 0) {
                    result.append(" ");
                }

                result.append(firstCharacter.toUpperCase()).append(remainingCharacters);
            }
        }
        return result.toString();
    }

	static int[] dungeonsXPPerLevel = {0, 50, 75, 110, 160, 230, 330, 470, 670, 950, 1340, 1890, 2665, 3760, 5260, 7380, 10300, 14400,
									  20000, 27600, 38000, 52500, 71500, 97000, 132000, 180000, 243000, 328000, 445000, 600000, 800000,
									  1065000, 1410000, 1900000, 2500000, 3300000, 4300000, 5600000, 7200000, 9200000, 12000000, 15000000,
									  19000000, 24000000, 30000000, 38000000, 48000000, 60000000, 75000000, 93000000, 116250000};

    public static double xpToDungeonsLevel(double xp) {
		for (int i = 0, xpAdded = 0; i < dungeonsXPPerLevel.length; i++) {
			xpAdded += dungeonsXPPerLevel[i];
			if (xp < xpAdded) {
				double level =  (i - 1) + (xp - (xpAdded - dungeonsXPPerLevel[i])) / dungeonsXPPerLevel[i];
				return (double) Math.round(level * 100) / 100;
			}
		}
		return 50D;
	}
    
    public static double randomNumber(int min,int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
    
    public static void drawTextWithStyle(String text, float x, float y, int color) {
        Minecraft.getMinecraft().fontRendererObj.drawString(text,1, 0, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(text, -1, 0, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(text, 0, 1, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(text, 0, -1, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(text, 0, 0, color, false);
    }

    public static void drawTextWithStyle2(String text, float x, float y) {
        String shadowText = Utils.cleanColor(text);
        Minecraft.getMinecraft().fontRendererObj.drawString(shadowText,1, 0, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, -1, 0, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, 0, 1, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, 0, -1, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(text, 0, 0, 0xFFFFFF, false);
    }

    public static void drawTextWithStyle3(String text, float x, float y) {
        String shadowText = Utils.cleanColor(text);
        Minecraft.getMinecraft().fontRendererObj.drawString(shadowText,x+1, y, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x-1, y, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x, y+1, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x, y-1, 0x000000, false);
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, 0xFFFFFF, false);
    }

    /**
     * Original code was taken from Skytils under GNU Affero General Public License v3.0 and modified by MrFast
     *
     * @author Skytils Team
     * @link https://github.com/Skytils/SkytilsMod/blob/1.x/LICENSE.md
     */
    public static String msToDuration(long value) {
        long currentTime = System.currentTimeMillis();
        long age = currentTime - value;
        String ageString = DurationFormatUtils.formatDuration(age, "d") + "d";
        if ("0d".equals(ageString)) {
            ageString = DurationFormatUtils.formatDuration(age, "H") + "h";
            if ("0h".equals(ageString)) {
                ageString = DurationFormatUtils.formatDuration(age, "m") + "m";
                if ("0m".equals(ageString)) {
                    ageString = DurationFormatUtils.formatDuration(age, "s") + "s";
                    if ("0s".equals(ageString)) {
                        ageString = age + "ms";
                    }
                }
            }
        }
        return ageString;
    }
    
    public static String secondsToTime(int seconds) {
        String time = "";
        int sec = seconds % 60;
        int min = (seconds / 60) % 60;
        int hours = (seconds / 3600) % 24;
        int days = seconds / (3600 * 24);
    
        if (days > 0) time += days + "d ";
        if (hours > 0) time += hours + "h ";
        if (min > 0) time += min + "m ";
        time += sec + "s";
    
        return time.trim();
    }

    public static String round(double value, int precision) {
        double scale = Math.pow(10, precision);
        double roundedValue = Math.round(value * scale) / scale;
        return String.valueOf(roundedValue);
    }

    public static String[] getListOfPlayerUsernames() {
        final Collection<NetworkPlayerInfo> players = Utils.GetMC().getNetHandler().getPlayerInfoMap();
        final List<String> list = new ArrayList<>();
        for (final NetworkPlayerInfo info : players) {
            if(!info.getGameProfile().getName().contains("!")) list.add(info.getGameProfile().getName());
        }
        return list.toArray(new String[0]);
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
    
    /**
     * Taken from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    public static void checkForDungeons() {
        if (inSkyblock) {
            List<String> scoreboard = ScoreboardUtil.getSidebarLines();
            for (String s : scoreboard) {
                String sCleaned = ScoreboardUtil.cleanSB(s);
                if ((sCleaned.contains("The Catacombs") && !sCleaned.contains("Queue")) || sCleaned.contains("Cleared:")) {
                    inDungeons = true;
                    return;
                }
            }
        }
        inDungeons = false;
    }


    public static void SendMessage(String string) {
        if (Utils.GetMC().ingameGUI != null || Utils.GetMC().thePlayer == null) {
            Utils.GetMC().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(ChatFormatting.AQUA+"[SBF] "+ChatFormatting.RESET+string));
        }
    }
    public static void SendMessage(Integer string) {
        if (Utils.GetMC().ingameGUI != null || Utils.GetMC().thePlayer == null) {
            Utils.GetMC().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(ChatFormatting.AQUA+"[SBF] "+ChatFormatting.RESET+string));
        }
    }
    public static void SendMessage(Double string) {
        if (Utils.GetMC().ingameGUI != null || Utils.GetMC().thePlayer == null) {
            Utils.GetMC().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(ChatFormatting.AQUA+"[SBF] "+ChatFormatting.RESET+string));
        }
    }
    public static void SendMessage(IChatComponent msg) {
        if (Utils.GetMC().ingameGUI != null || Utils.GetMC().thePlayer == null) {
            ChatComponentText prefix = new ChatComponentText(EnumChatFormatting.AQUA+"[SBF] "+EnumChatFormatting.RESET);
            Utils.GetMC().thePlayer.addChatMessage(new ChatComponentText("").appendSibling(prefix).appendSibling(msg));
        }
    }

    public static void playSound(String sound, double pitch) {
        mc.thePlayer.playSound(sound, 1, (float) pitch);
    }
    
	public static int getDungeonFloor() {
        String floor="";
        for (String l : ScoreboardUtil.getSidebarLines()) {
            String line = ScoreboardUtil.cleanSB(l);
            if(line.contains("Catacombs")) {
                floor = line;
            }
        }

        if(floor.replaceAll("[^0-9]", "") != "") {
            return Integer.parseInt(floor.replaceAll("[^0-9]", ""));
        } else {
            return 0;
        }
    }

    public static boolean isNPC(Entity entity) {
        if(entity instanceof EntityPlayer) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            return entity.getUniqueID().version() == 2 && entityLivingBase.getHealth() == 20.0F && !entityLivingBase.isPlayerSleeping() && Utils.inSkyblock;
        } else return false;
    }

    public static void setTimeout(Runnable code, int ms) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                code.run();
            }
        }, ms);
    }

    public static String convertIdToLocation(String id) {
        switch (id) {
            case "dynamic":
                return "Private Island";
            case "winter":
                return "Jerry's Workshop";
            case "mining_1":
                return "Gold Mine";
            case "mining_2":
                return "Deep Caverns";
            case "mining_3":
                return "Dwarven Mines";
            case "combat_1":
                return "Spider's Den";
            case "combat_3":
                return "The End";
            case "farming_1":
                return "The Farming Islands";
            case "foraging_1":
                return "The Park";    
            default:
                return Utils.convertToTitleCase(id);
        }
    }

    public static void drawLine(int x1, int y1, int x2, int y2,Color color,float width) {
        GlStateManager.disableLighting();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend(); //disabled means no opacity
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
		Vector2f vec = new Vector2f(x2 - x1, y2 - y1);
		vec.normalise(vec);
		Vector2f side = new Vector2f(vec.y, -vec.x);
        
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
		Vector2f side = new Vector2f(vec.y, -vec.x);
        
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

    public static void drawGraySquareWithBorder(int x,int y,int width,int height,int borderWidth) {
        UIRoundedRectangle.Companion.drawRoundedRectangle(new UMatrixStack(),x, y, x+width, height+2, 5, new Color(0,0,0,125));
        UIRoundedRectangle.Companion.drawRoundedRectangle(new UMatrixStack(),x-2, y-2, x+width+2, height+2+2, 5, new Color(55,55,55,125));
    }
    public static void drawGraySquare(int x,int y,int width,int height,int borderWidth, Color c) {
        UIRoundedRectangle.Companion.drawRoundedRectangle(new UMatrixStack(),x, y, x+width, height, 5, c);
    }

    public static void drawText(String string, int x, int y) {
        Utils.GetMC().fontRendererObj.drawString(string, x, y, 0xFFFFFF, true);
    }
}