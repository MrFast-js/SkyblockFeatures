package mrfast.sbf.features.overlays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.ItemUtils.Inventory;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
public class BaitCounterOverlay {
    public static Integer seconds = 0;
    public static HashMap<String,Integer> typesOfBait = new HashMap<>();
    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(Utils.GetMC().thePlayer==null || !SkyblockFeatures.config.baitCounter) return;
        if(seconds==0) {
            reloadFishingBag();
        }
        seconds++;

        if(seconds==61) {
            seconds = 1;
            reloadFishingBag();
        }
    }

    public void reloadFishingBag() {
        typesOfBait.clear();
        new Thread(() -> {
                // Get UUID for Hypixel API requests
                String uuid = Utils.GetMC().thePlayer.getUniqueID().toString().replace("-","");
                String latestProfile = APIUtils.getLatestProfileID(uuid);
                if (latestProfile == null) return;

                String profileURL = "https://api.hypixel.net/skyblock/profile?profile=" + latestProfile+"#BaitDisplay";
                System.out.println("Fetching profile...");
                JsonObject profileResponse = APIUtils.getJSONResponse(profileURL);
                if(profileResponse.toString().equals("{}")) {
                    Utils.SendMessage(EnumChatFormatting.RED + "Hypixel API is having problems!");
                    return;
                }
                JsonObject playerResponse = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject();

                if(playerResponse.has("fishing_bag")) {
                    String inventoryBase64 = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("fishing_bag").getAsJsonObject().get("data").getAsString();
                    Inventory items = new Inventory(inventoryBase64);
                    List<ItemStack> a = ItemUtils.decodeItem(items,true);
                    for(ItemStack item: a) {
                        if(item==null) continue;
                        String name = item.getDisplayName();
                        Integer count = item.stackSize;
                        if(typesOfBait.containsKey(name)) {
                            typesOfBait.put(name, typesOfBait.get(name)+count);
                        } else {
                            typesOfBait.put(name, count);
                        }
                    }
                }
        }).start();
    }

    public static Minecraft mc = Utils.GetMC();
    static {
        new baitCounter();
    }   

    public static class baitCounter extends UIElement {
        public baitCounter() {
            super("baitCounter", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock || Utils.GetMC().theWorld==null || !SkyblockFeatures.config.baitCounter) return;
            List<String> lines = new ArrayList<>();

            lines.add(ChatFormatting.AQUA+"Bait: "+ChatFormatting.GRAY+"("+(61-seconds)+")");

            if(typesOfBait.isEmpty()) {
                lines.add(" "+ChatFormatting.RED+"Loading..");
            }
            for(String baitName:typesOfBait.keySet()) {
                lines.add(" "+baitName+ChatFormatting.DARK_GRAY+" x"+Utils.nf.format(typesOfBait.get(baitName)));
            }
            GuiUtils.drawTextLines(lines,0,0, GuiUtils.TextStyle.DROP_SHADOW);
        }

        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            String[] lines = {
                    ChatFormatting.AQUA+"Bait:",
                    ChatFormatting.WHITE+" Corrupted Bait "+ChatFormatting.DARK_GRAY+"x218",
                    ChatFormatting.GREEN+" Blessed Bait "+ChatFormatting.DARK_GRAY+"x381",
                    ChatFormatting.GREEN+" Shark Bait "+ChatFormatting.DARK_GRAY+"x313",
                    ChatFormatting.WHITE+" Corrupted Bait "+ChatFormatting.DARK_GRAY+"x831",
            };
            GuiUtils.drawTextLines(Arrays.asList(lines),0,0, GuiUtils.TextStyle.DROP_SHADOW);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.baitCounter;
        }

        @Override
        public int getHeight() {
            return (Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)*5;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth(ChatFormatting.WHITE+" Corrupted Bait "+ChatFormatting.DARK_GRAY+"x128   .");
        }
    }
}
