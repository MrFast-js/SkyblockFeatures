package mrfast.sbf.features.overlays;

import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.commands.InventoryCommand;
import mrfast.sbf.commands.InventoryCommand.Inventory;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.APIUtils;
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
                String key = SkyblockFeatures.config.apiKey;
                if (key.equals("")) return;
                
                // Get UUID for Hypixel API requests
                String uuid = APIUtils.getUUID(Utils.GetMC().thePlayer.getName());;
                String latestProfile = APIUtils.getLatestProfileID(uuid, key);
                if (latestProfile == null) return;

                String profileURL = "https://api.hypixel.net/skyblock/profile?profile=" + latestProfile+"#BaitDisplay";
                System.out.println("Fetching profile...");
                JsonObject profileResponse = APIUtils.getJSONResponse(profileURL);
                if(profileResponse.toString().equals("{}")) {
                    Utils.SendMessage(EnumChatFormatting.RED + "Hypixel API is having problems!");
                    return;
                }

                if(profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().has("fishing_bag")) {
                    String inventoryBase64 = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("fishing_bag").getAsJsonObject().get("data").getAsString();
                    Inventory items = new Inventory(inventoryBase64);
                    List<ItemStack> a = InventoryCommand.decodeItem(items,true);
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
            super("baitCounter", new Point(0.6125f, 0.675f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock || Utils.GetMC().theWorld==null || !SkyblockFeatures.config.baitCounter) return;
            Utils.drawTextWithStyle3(ChatFormatting.AQUA+"Bait: "+ChatFormatting.GRAY+"("+(61-seconds)+")", 0, 0);
            int index = 0;
            if(typesOfBait.size()==0) {
                Utils.drawTextWithStyle3(" "+ChatFormatting.RED+"Loading..", 0, index*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10);
            }
            for(String baitName:typesOfBait.keySet()) {
                Utils.drawTextWithStyle3(" "+baitName+ChatFormatting.DARK_GRAY+" x"+Utils.nf.format(typesOfBait.get(baitName)), 0, index*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10);
                index++;
            }
        }

        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            Utils.drawTextWithStyle3(ChatFormatting.AQUA+"Bait:", 0, 0);
            Utils.drawTextWithStyle3(ChatFormatting.WHITE+" Corrupted Bait "+ChatFormatting.DARK_GRAY+"x218", 0, 1*(10)+10);
            Utils.drawTextWithStyle3(ChatFormatting.GREEN+" Blessed Bait "+ChatFormatting.DARK_GRAY+"x381", 0, 2*(10)+10);
            Utils.drawTextWithStyle3(ChatFormatting.GREEN+" Shark Bait "+ChatFormatting.DARK_GRAY+"x313", 0, 3*(10)+10);
            Utils.drawTextWithStyle3(ChatFormatting.WHITE+" Corrupted Bait "+ChatFormatting.DARK_GRAY+"x831", 0, 4*(10)+10);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.baitCounter;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*5;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth(ChatFormatting.WHITE+" Corrupted Bait "+ChatFormatting.DARK_GRAY+"x128   .");
        }
    }
}
