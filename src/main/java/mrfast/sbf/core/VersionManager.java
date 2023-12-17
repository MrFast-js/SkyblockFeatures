package mrfast.sbf.core;

import com.mojang.realmsclient.gui.ChatFormatting;
import moe.nea.libautoupdate.*;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class VersionManager {
    static PotentialUpdate potentialUpdate;
    private static final UpdateContext context = new UpdateContext(
            UpdateSource.githubUpdateSource("MrFast-js", "SkyblockFeatures"),
            UpdateTarget.deleteAndSaveInTheSameFolder(VersionManager.class),
            CurrentVersion.ofTag(SkyblockFeatures.VERSION),
            SkyblockFeatures.MODID
            );

    public static void silentUpdateCheck() {
        context.checkUpdate(getUpdatePreference()).thenAcceptAsync((update)->{
            if (update != null) {
                potentialUpdate = update;
                if(isClientOutdated()) {
                    String updatePreference = getUpdatePreference();
                    String updateVersion = "v"+potentialUpdate.getUpdate().getVersionName().split("v")[1].trim();
                    IChatComponent notificationText = new ChatComponentText(
                            ChatFormatting.GREEN+"Version "+EnumChatFormatting.GOLD+EnumChatFormatting.BOLD+ updateVersion+ChatFormatting.RESET+
                                    ChatFormatting.GREEN+" is available. "
                                    +ChatFormatting.YELLOW+"Click to update!")
                            .setChatStyle(new ChatStyle()
                                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sf update " + updatePreference))
                                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatFormatting.GREEN+"Click to update"))));

                    Utils.setTimeout(()->{
                        Utils.playSound("random.orb", 0.1);
                        Utils.sendMessage(notificationText);
                    },1000);
                }
            }
        });
    }

    public static String getUpdatePreference() {
        int type = SkyblockFeatures.config.updateCheckType;
        if(type==0) return "full";
        else return "pre";
    }
    public static boolean isClientOutdated() {
        double currentVersionValue = getVersionValue(SkyblockFeatures.VERSION);
        String updateVersionName = potentialUpdate.getUpdate().getVersionName().split("v")[1];
        double updateVersionValue = getVersionValue(updateVersionName);

        return currentVersionValue<updateVersionValue;
    }

    public static void checkForUpdates(String updateType) {
        checkForUpdates(updateType,false);
    }

    public static void checkForUpdates(String updateType,boolean force) {
        // pre full
        Utils.sendMessage(ChatFormatting.YELLOW + "Checking for updates...");
        context.checkUpdate(updateType).thenAcceptAsync((update)->{
            if (update != null) {
                potentialUpdate = update;
                if(checkIfNeedUpdate() || force) {
                    doUpdate();
                }

            } else {
                Utils.sendMessage("No updates available. You are already using the latest version.");
            }
        });
    }

    private static double getVersionValue(String version) {
        String part1 = version.split("-")[0];
        int num1 = Integer.parseInt(part1.replaceAll("[^0-9]", ""));

        if(version.contains("BETA")) {
            String part2 = version.split(part1)[1];

            int num2 = Integer.parseInt(part2.replaceAll("[^0-9]", ""));

            return num1 + ((double) num2 / 100);
        }
        return num1;
    }

    public static boolean checkIfNeedUpdate() {
        double currentVersionValue = getVersionValue(SkyblockFeatures.VERSION);
        if (potentialUpdate == null) {
            Utils.sendMessage(ChatFormatting.RED + "Unable to check for updates. Please try again later.");
            return false;
        }

        String updateVersionName = potentialUpdate.getUpdate().getVersionName().split("v")[1];
        double updateVersionValue = getVersionValue(updateVersionName);

        Utils.sendMessage(ChatFormatting.GREEN + "Current version: " + ChatFormatting.AQUA + SkyblockFeatures.VERSION);
        Utils.sendMessage(ChatFormatting.GREEN + "Latest version available: " + ChatFormatting.AQUA + updateVersionName);

        if (currentVersionValue > updateVersionValue) {
            Utils.sendMessage(ChatFormatting.GREEN + "You are using an more recent version. No update needed.");
            return false;
        } else if (currentVersionValue == updateVersionValue) {
            Utils.sendMessage(ChatFormatting.GREEN + "You are already using the latest version.");
            return false;
        } else {
            Utils.sendMessage(ChatFormatting.YELLOW + "You are using an outdated version. Updating to version "
                    + ChatFormatting.AQUA + updateVersionName + ChatFormatting.YELLOW + "...");
            return true;
        }
    }

    public static void doUpdate() {
        CompletableFuture.supplyAsync(() -> {
            try {
                Utils.sendMessage(ChatFormatting.YELLOW + "Preparing update...");
                potentialUpdate.prepareUpdate();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }).thenAcceptAsync((arg) -> {
            try {
                Utils.sendMessage(ChatFormatting.GREEN + "Downloading update: " + ChatFormatting.AQUA + potentialUpdate.getUpdate().getVersionName());
                potentialUpdate.executeUpdate();

                IChatComponent notificationText = new ChatComponentText(
                        ChatFormatting.GREEN + "Update downloaded successfully! Skyblock Features will update when you close the game. ");

                IChatComponent closeGame = new ChatComponentText(
                        ChatFormatting.RED.toString() + ChatFormatting.BOLD + "[CLOSE GAME]")
                        .setChatStyle(new ChatStyle()
                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sf update close"))
                                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new ChatComponentText(ChatFormatting.RED + "Click to close the game"))));

                notificationText.appendSibling(closeGame);
                Utils.sendMessage(notificationText);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void closeMinecraft() {
        Utils.sendMessage(ChatFormatting.RED + "Closing Minecraft..");
        Utils.setTimeout(() -> {
            FMLCommonHandler.instance().handleExit(-1);
            FMLCommonHandler.instance().expectServerStopped();
        }, 2000);
    }

}
