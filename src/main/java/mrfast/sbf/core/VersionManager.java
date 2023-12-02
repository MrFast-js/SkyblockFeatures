package mrfast.sbf.core;

import com.mojang.realmsclient.gui.ChatFormatting;
import moe.nea.libautoupdate.*;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.CompletableFuture;

public class VersionManager {
    static PotentialUpdate potentialUpdate;
    static boolean betaTester = true;
    private static final UpdateContext context = new UpdateContext(
            UpdateSource.githubUpdateSource("MrFast-js", "SkyblockFeatures"),
            UpdateTarget.deleteAndSaveInTheSameFolder(VersionManager.class),
            CurrentVersion.ofTag(SkyblockFeatures.VERSION),
            SkyblockFeatures.MODID
            );
    public static void checkForUpdates() {
        // pre full
        Utils.sendMessage(ChatFormatting.YELLOW + "Checking for updates...");
        context.checkUpdate("pre").thenAcceptAsync((update)->{
            if (update != null) {
                potentialUpdate = update;
                checkIfNeedUpdate();
            } else {
                Utils.sendMessage("No updates available. You are already using the latest version.");
            }
        });
    }
    private static double getVersionValue(String version) {
        String part1 = version.substring(0, 6);
        int num1 = Integer.parseInt(part1.replaceAll("[^0-9]", ""));

        if(version.length()>7) {
            String part2 = version.split(part1)[1];

            int num2 = Integer.parseInt(part2.replaceAll("[^0-9]", ""));

            return num1 + ((double) num2 / 100);
        }
        return num1;
    }

    public static void checkIfNeedUpdate() {
        double currentVersionValue = getVersionValue(SkyblockFeatures.VERSION);
        if (potentialUpdate == null) {
            Utils.sendMessage(ChatFormatting.RED + "Unable to check for updates. Please try again later.");
            return;
        }

        String updateVersionName = potentialUpdate.getUpdate().getVersionName().split("v")[1];
        double updateVersionValue = getVersionValue(updateVersionName);

        Utils.sendMessage(ChatFormatting.GREEN + "Current version: " + ChatFormatting.AQUA + SkyblockFeatures.VERSION);
        Utils.sendMessage(ChatFormatting.GREEN + "Latest version available: " + ChatFormatting.AQUA + updateVersionName);

        if (currentVersionValue > updateVersionValue) {
            Utils.sendMessage(ChatFormatting.GREEN + "You are using an unreleased version. No update needed.");
        } else if (currentVersionValue == updateVersionValue) {
            Utils.sendMessage(ChatFormatting.GREEN + "You are already using the latest version.");
        } else {
            Utils.sendMessage(ChatFormatting.YELLOW + "You are using an outdated version. Updating to version "
                    + ChatFormatting.AQUA + updateVersionName + ChatFormatting.YELLOW + "...");
            doUpdate();
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
                Utils.sendMessage(ChatFormatting.GREEN + "Update downloaded successfully! Your game will now restart.");

                // Close Minecraft
                Utils.setTimeout(() -> {
                    FMLCommonHandler.instance().handleExit(-1);
                    FMLCommonHandler.instance().expectServerStopped();
                }, 2000);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
