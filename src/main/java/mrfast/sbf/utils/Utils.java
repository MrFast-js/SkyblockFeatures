package mrfast.sbf.utils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonObject;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.*;

public class Utils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean inSkyblock = false;
    public static boolean inDungeons = false;
    public static int health=0;
    public static int maxHealth=0;
    public static int mana=0;
    public static int maxMana=0;
    public static int overflowMana=0;

    public static int Defense;
    public static final NumberFormat nf = NumberFormat.getInstance(Locale.US);

    private static final String[] steps = new String[] {"", "k", "m", "b","t"};

    public static String shortenNumber(double number) {
        if(number<1000) {
            return ((int) number)+"";
        }
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

    /*
    Returns a int if it exists and 0 if it doesnt.
    Useful for /pv when certain APIs are turned off
     */
    public static int safeGetInt(JsonObject jsonObject, String key) {
        try {
            return jsonObject.get(key).getAsInt();
        } catch (Exception ignored) {
            return 0; // Default value when an exception occurs
        }
    }
    public static long safeGetLong(JsonObject jsonObject, String key) {
        try {
            return jsonObject.get(key).getAsLong();
        } catch (Exception ignored) {
            return 0L; // Default value when an exception occurs
        }
    }

	public static String percentOf(long num, long outOf) {
		double percent = (double) num / outOf * 100;
		return ChatFormatting.DARK_GRAY + " (" + String.format("%.2f", percent) + "%)";
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

    static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    public static void copyToClipboard(String text) {
        clipboard.setContents(new StringSelection(text), null);
    }

    /*
        Unsure if going to use this because it does cause a performance impact
     */
    public static boolean isBlockVisible(BlockPos targetBlockPos) {
        Minecraft mc = Minecraft.getMinecraft();

        // Get the player's eye position
        double eyeX = mc.thePlayer.posX;
        double eyeY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
        double eyeZ = mc.thePlayer.posZ;
        List<Vec3> corners = Arrays.asList(
                new Vec3(targetBlockPos.getX()+0.1, targetBlockPos.getY() + 0.5, targetBlockPos.getZ()+0.1),
                new Vec3(targetBlockPos.getX()+0.1, targetBlockPos.getY() + 0.5, targetBlockPos.getZ() + .9),
                new Vec3(targetBlockPos.getX() + 0.9, targetBlockPos.getY() + 0.5, targetBlockPos.getZ() + .9),
                new Vec3(targetBlockPos.getX() + 0.9, targetBlockPos.getY() + 0.5, targetBlockPos.getZ()+0.1)
        );
        boolean visible = false;
        for (Vec3 corner : corners) {
            MovingObjectPosition rayTraceResult = mc.theWorld.rayTraceBlocks(
                    new Vec3(eyeX, eyeY, eyeZ),
                    corner
            );
            if(checkRaytrace(rayTraceResult,targetBlockPos)) {
                visible = true;
                break;
            }
        }
        return visible;
    }

    public static boolean checkRaytrace(MovingObjectPosition result, BlockPos targetBlockPos) {
        return result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && result.getBlockPos().equals(targetBlockPos);
    }

    /**
     * Taken from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
    */
    public static void checkForSkyblock() {
        if (isOnHypixel()) {
            if(mc.theWorld.getScoreboard() == null) return;
            ScoreObjective scoreboardObj = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
            if (scoreboardObj != null) {
                String scObjName = ScoreboardUtil.fixFormatting(scoreboardObj.getDisplayName(),true);
                if (scObjName.contains("SKYBLOCK")) {
                    inSkyblock = true;
                    return;
                }
            }
        }
        inSkyblock = false;
    }

    public static String cleanColor(String in) {
        return in.replaceAll("(?i)\\u00A7.", "");
    }
    
    public static Minecraft GetMC() {
        return mc;
    }

    public static boolean overrideDevMode = false;
    public static boolean overrideDevModeValue = false;
    public static boolean isDeveloper() {
        return SkyblockFeatures.config.developerMode;
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
    
    public static int randomNumber(int min,int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static String secondsToTime(long seconds) {
        String time = "";
        long sec = seconds % 60;
        long min = (seconds / 60) % 60;
        long hours = (seconds / 3600) % 24;
        long days = seconds / (3600 * 24);
    
        if (days > 0) time += days + "d ";
        if (hours > 0) time += hours + "h ";
        if (min > 0) time += min + "m ";
        time += sec + "s";
    
        return time.trim();
    }
    public static String msToSeconds(long ms,int precision) {
        return Utils.round(ms/1000d,precision)+"s";
    }

    // Precision is # of decimal places
    public static String round(double value, int precision) {
        double scale = Math.pow(10, precision);
        double roundedValue = Math.round(value * scale) / scale;
        return String.format("%." + precision + "f", roundedValue);
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
     * Taken from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    public static void checkForDungeons() {
        if (inSkyblock) {
            List<String> scoreboard = ScoreboardUtil.getSidebarLines();
            for (String s : scoreboard) {
                if ((s.contains("The Catacombs") && !s.contains("Queue")) || s.contains("Cleared:")) {
                    inDungeons = true;
                    return;
                }
            }
        }
        inDungeons = false;
    }


    public static void sendMessage(String string) {
        if (Utils.GetMC().ingameGUI != null && Utils.GetMC().thePlayer != null) {
            Utils.GetMC().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(ChatFormatting.AQUA+"[SBF] "+ChatFormatting.RESET+string));
        }
    }

    public static void sendMessage(IChatComponent msg) {
        if (Utils.GetMC().ingameGUI != null || Utils.GetMC().thePlayer == null) {
            ChatComponentText prefix = new ChatComponentText(EnumChatFormatting.AQUA+"[SBF] "+EnumChatFormatting.RESET);
            Utils.GetMC().thePlayer.addChatMessage(new ChatComponentText("").appendSibling(prefix).appendSibling(msg));
        }
    }

    public static void playSound(String sound, double pitch) {
        mc.thePlayer.playSound(sound, 1, (float) pitch);
    }

    public static boolean isNPC(Entity entity) {
        if(entity instanceof EntityPlayer) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            return entity.getUniqueID().version() == 2 && entityLivingBase.getHealth() == 20.0F && !entityLivingBase.isPlayerSleeping() && Utils.inSkyblock;
        } else return false;
    }

    public static void setTimeout(Runnable code, int ms) {
        setTimeout(code,ms,false);
    }
    public static void setTimeout(Runnable code, int ms,boolean addToMinecraft) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(addToMinecraft) Utils.GetMC().addScheduledTask(code);
                else code.run();
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
}