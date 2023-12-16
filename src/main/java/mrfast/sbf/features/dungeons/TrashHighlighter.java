package mrfast.sbf.features.dungeons;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.io.*;
import java.nio.file.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;

public class TrashHighlighter {
    static File trashFile;
    static List<String> trashList = new ArrayList<>();
    public static void openTrashFile() {
        if (Desktop.isDesktopSupported() && trashFile != null) {
            try {
                Desktop.getDesktop().open(trashFile);
            } catch (IOException e) {
                e.printStackTrace();  // Handle the exception according to your needs
            }
        }
    }
    public static void initTrashFile() {
        trashFile = new File(SkyblockFeatures.modDir, "trash.txt");
        if (!trashFile.exists()) {
            try {
                trashFile.createNewFile();
                // Default values
                writeTextToFile("CRYPT_DREADLORD_SWORD\n" +
                                "MACHINE_GUN_BOW\n" +
                                "Healing VIII\n" +
                                "DUNGEON_LORE_PAPER\n" +
                                "ENCHANTED_BONE\n" +
                                "CRYPT_BOW\n" +
                                "ZOMBIE_SOLDIER\n" +
                                "SKELETON_SOLDIER\n" +
                                "SKELETON_MASTER\n" +
                                "SUPER_HEAVY\n" +
                                "INFLATABLE_JERRY\n" +
                                "DUNGEON_TRAP\n" +
                                "SKELETOR\n" +
                                "PREMIUM_FLESH\n" +
                                "TRAINING\n" +
                                "CONJURING_SWORD\n" +
                                "FEL_PEARL\n" +
                                "ZOMBIE_KNIGHT\n" +
                                "ENCHANTED_ROTTEN_FLESH\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            refreshTrashList();
            watchFileForChanges();
        }
    }

    public static void refreshTrashList() {
        trashList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(trashFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                trashList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void watchFileForChanges() {
        try {
            Path path = Paths.get(trashFile.getAbsolutePath()).toAbsolutePath().getParent();
            WatchService watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            Thread watchThread = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                // File modified, refresh the array
                                refreshTrashList();
                            }
                        }
                        key.reset();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            watchThread.setDaemon(true);
            watchThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeTextToFile(String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(trashFile))) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();  // Handle the exception according to your needs
        }
    }

    @SubscribeEvent
    public void onDrawSlots(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if (!Utils.inSkyblock) return;
        if(!event.slot.getHasStack()) return;
        ItemStack stack = event.slot.getStack();
        int x = event.slot.xDisplayPosition;
        int y = event.slot.yDisplayPosition;
        String n = ItemUtils.getSkyBlockItemID(stack);

        if(SkyblockFeatures.config.highlightTrash && n != null) {
            boolean trash = false;
            for (String s : trashList) {
                if (n.contains(s)) {
                    trash = true;
                    break;
                }
            }
            if(trash) {
                Gui.drawRect(x, y, x + 16, y + 1, new Color(255, 0, 0, 255).getRGB());
                Gui.drawRect(x, y, x + 1, y + 16, new Color(255, 0, 0, 255).getRGB());
                Gui.drawRect(x+15, y, x+16, y + 16, new Color(255, 0, 0, 255).getRGB());
                Gui.drawRect(x, y+15, x + 16, y + 16, new Color(255, 85, 0, 255).getRGB());
            }
        }
    }
}
