package mrfast.sbf.features.overlays.menuOverlay;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.DataManager;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.ProfileSwapEvent;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MinionOverlay {
    static JsonObject minions = new JsonObject();
    @SubscribeEvent
    public void onProfileSwap(ProfileSwapEvent event) {
        minions = (JsonObject) DataManager.getProfileDataDefault("minions", new JsonObject());
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        minions = (JsonObject) DataManager.getProfileDataDefault("minions", new JsonObject());
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if(event.message.getUnformattedText().startsWith("You picked up a minion! You currently have")) {
            if(minions.has(closestMinion.getPosition().toString())) {
                minions.remove(closestMinion.getPosition().toString());
            }
        }
    }

    @SubscribeEvent
    public void onDrawContainerTitle(TitleDrawnEvent event) {
        if (event.gui instanceof GuiChest && SkyblockFeatures.config.showMinionsTotalCoinsPerDay) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            double totalValue = 0d;

            String chestName = inv.getDisplayName().getUnformattedText().trim();
            if(chestName.contains(" Minion ") && !chestName.contains("Recipe")) {
                double secondsPerAction = 0;
                ItemStack generating = null;
                for(int slotId = 0;slotId<inv.getSizeInventory();slotId++) {
                    if(inv.getStackInSlot(slotId)==null) continue;

                    ItemStack stack = inv.getStackInSlot(slotId);
                    if (slotId == 4) {
                        List<String> lore = ItemUtils.getItemLore(stack);
                        for (String s : lore) {
                            String line = Utils.cleanColor(s);
                            if (line.contains("Actions:")) {
                                secondsPerAction = Double.parseDouble(line.replaceAll("[^0-9.]", ""));
                            }
                        }
                    }
                    if(isSlotFromMinion(slotId)) {
                        String identifier = PricingData.getIdentifier(stack);
                        if(identifier!=null) {
                            Double sellPrice = PricingData.bazaarPrices.get(identifier);
                            if(sellPrice!=null) totalValue += (sellPrice*stack.stackSize);
                        }
                        if(stack.getDisplayName().contains("Block") && !stack.getDisplayName().contains("Snow")) {
                            continue;
                        }
                        if(generating == null && ItemUtils.getRarity(stack) == ItemRarity.COMMON) {
                            generating = stack;
                        }
                    }
                }
                if(generating != null && ItemUtils.getRarity(generating) == ItemRarity.COMMON) {
                    String identifier = PricingData.getIdentifier(generating);
                    String[] lines;
                    if(identifier==null && minions.has(closestMinion.getPosition().toString())) {
                        identifier = minions.get(closestMinion.getPosition().toString()).getAsJsonObject().get("generating").getAsString();
                    }
                    if (identifier != null) {
                        Double sellPrice = PricingData.bazaarPrices.get(identifier);
                        if(sellPrice != null) {
                            String duration = "Unknown";
                            JsonObject minion = new JsonObject();
                            if(minions.has(closestMinion.getPosition().toString())) {
                                minion = minions.get(closestMinion.getPosition().toString()).getAsJsonObject();
                            }
                            long lastCollected = System.currentTimeMillis();

                            if(minion.has("lastCollected")) {
                                lastCollected = minion.get("lastCollected").getAsLong();
                            }

                            Double perHour = -1d;
                            if(closestMinion != null) {
                                long timeElapsed = (System.currentTimeMillis()-lastCollected)/1000L;
                                perHour = Math.floor((totalValue/timeElapsed)*60d*60d); // coins per second
                                duration = Utils.secondsToTime(timeElapsed);
                            }

                            String fuelRunsOut = "Unlimited";
                            for(String line:ItemUtils.getItemLore(inv.getStackInSlot(19))) {
                                line = Utils.cleanColor(line);
                                if(line.startsWith("Time Remaining")) {
                                    fuelRunsOut=line.split(": ")[1];
                                    String shortTime = fuelRunsOut.split(" ")[1].substring(0,1);
                                    fuelRunsOut = fuelRunsOut.split(" ")[0]+shortTime;
                                }
                            }
                            lines = new String[]{
                                    ChatFormatting.LIGHT_PURPLE + chestName,
                                    ChatFormatting.WHITE + " • Time Between Actions: " + ChatFormatting.GREEN + secondsPerAction + "s",
                                    ChatFormatting.WHITE + " • Fuel Duration: " + ChatFormatting.YELLOW + fuelRunsOut,
                                    ChatFormatting.WHITE + " • Coins Per Hour: " + ChatFormatting.GOLD + Utils.nf.format(perHour),
                                    ChatFormatting.WHITE + " • Total Value: " + ChatFormatting.GOLD + Utils.shortenNumber(totalValue),
                                    ChatFormatting.WHITE + " • Last Collected: " + ChatFormatting.AQUA + duration,
                            };
                            minion.addProperty("generating",identifier);
                            minion.addProperty("lastCollected",lastCollected);
                            minion.addProperty("coinsPerHour",perHour);

                            minions.add(closestMinion.getPosition().toString(), minion);
                            DataManager.saveProfileData("minions",minions);
                        } else {
                            lines = new String[]{
                                    ChatFormatting.RED + "Unable to get item price!",
                                    ChatFormatting.RED + "Minion Generates: " + identifier
                            };
                        }
                    } else {
                        lines = new String[]{
                                ChatFormatting.RED + "Unable to get item id!",
                                ChatFormatting.RED + "Minion Generates: " + null
                        };
                    }
                    GuiUtils.drawSideMenu(Arrays.asList(lines), GuiUtils.TextStyle.DROP_SHADOW);
                }
            }
        }
    }
    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if(SkyblockFeatures.config.showMinionsTotalCoinsPerDay) {
            GuiChest gui = event.chest;
            ContainerChest chest;
            if(gui!=null && gui.inventorySlots!=null) {
                chest = (ContainerChest) gui.inventorySlots;
            } else {
                return;
            }
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            try {
                if(chestName.contains(" Minion ") && !chestName.contains("Recipe")) {
                    if(event.slot.getHasStack()) {
                        String nameOfItem = Utils.cleanColor(event.slot.getStack().getDisplayName());
                        if(nameOfItem.startsWith("Collect All") || isSlotFromMinion(event.slot.slotNumber)) {
                            Utils.setTimeout(()-> {
                                if (closestMinion != null && (isMinionCollected(inv)|| nameOfItem.startsWith("Collect All"))) {
                                    if (minions.has(closestMinion.getPosition().toString())) {
                                        minions.get(closestMinion.getPosition().toString()).getAsJsonObject().addProperty("lastCollected", System.currentTimeMillis());
                                        DataManager.saveProfileData("minions", minions);
                                    }
                                }
                            },400);
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    List<Integer> minionSlots = Arrays.asList(21,22,23,24,25,
                                              30,31,32,33,34,
                                              39,40,41,42,43);
    private boolean isSlotFromMinion(int i) {
        return minionSlots.contains(i);
    }
    private boolean isMinionCollected(IInventory inv) {
        for (Integer minionSlot : minionSlots) {
            if(inv.getStackInSlot(minionSlot)!=null) {
                String nameOfItem = Utils.cleanColor(inv.getStackInSlot(minionSlot).getDisplayName());
                if(!nameOfItem.startsWith("Storage unlocked at tier")) {
                    return false;
                }
            }
        }
        return true;
    }
    Entity closestMinion = null;
    @SubscribeEvent
    public void onRecievePacket(RenderWorldLastEvent event) {
        if(Utils.inSkyblock && SkyblockInfo.localLocation.equals("Your Island") && (SkyblockFeatures.config.showMinionsTotalCoinsPerDay ||SkyblockFeatures.config.minionLastCollected)) {
            for(Entity e : Utils.GetMC().theWorld.loadedEntityList){
                if(e instanceof EntityArmorStand) {
                    if(isMinion((EntityArmorStand) e)) {
                        if(SkyblockFeatures.config.minionLastCollected && minions.has(e.getPosition().toString()) && Utils.GetMC().thePlayer.getDistanceToEntity(e)<8) {
                            JsonObject minion = minions.get(e.getPosition().toString()).getAsJsonObject();
                            long timeElapsed = (System.currentTimeMillis()- minion.get("lastCollected").getAsLong())/1000L;
                            String duration = Utils.secondsToTime(timeElapsed);
                            RenderUtil.draw3DStringWithShadow(e.getPositionVector().add(new Vec3(0,1.5,0)),ChatFormatting.YELLOW+"Last Collected: "+ChatFormatting.AQUA+duration,event.partialTicks);
                        }

                        if(closestMinion==null) {
                            closestMinion = e;
                            continue;
                        }

                        if(Utils.GetMC().thePlayer.getDistanceToEntity(e)<Utils.GetMC().thePlayer.getDistanceToEntity(closestMinion)) {
                            closestMinion = e;
                        }
                    }
                }
            }

        }
    }

    public boolean isMinion(EntityArmorStand e) {
        for (int i = 0; i <= 3; i++) {
            if (e.getCurrentArmor(i) == null) return false;
        }

        return (Item.getIdFromItem(e.getCurrentArmor(0).getItem()) == 301 &&
                Item.getIdFromItem(e.getCurrentArmor(1).getItem()) == 300 &&
                Item.getIdFromItem(e.getCurrentArmor(2).getItem()) == 299 &&
                Item.getIdFromItem(e.getCurrentArmor(3).getItem()) == 397);
    }

    static {
        new MinionCoinsPerDay();
    }

    static String display = "§aTotal Minion Coins Per Day: §6";
    public static class MinionCoinsPerDay extends UIElement {
        public MinionCoinsPerDay() {
            super("Minion Coins Per Day Display", new Point(0.40972087f, 0.1964221f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            drawCoinsPerDay();
        }
        @Override
        public void drawElementExample() {
            drawCoinsPerDay();
        }

        private void drawCoinsPerDay() {
            long estimatedCoinsPerDay = 0;
            for (Map.Entry<String, JsonElement> stringJsonElementEntry : minions.entrySet()) {
                JsonObject minion = stringJsonElementEntry.getValue().getAsJsonObject();
                if(minion.has("coinsPerHour") && !minion.get("coinsPerHour").isJsonNull()) {
                    try {
                        estimatedCoinsPerDay += minion.get("coinsPerHour").getAsLong() * 24;
                    } catch (Exception ignored){}
                }
            }
            display = "§aTotal Minion Coins Per Day: §6"+ Utils.nf.format(estimatedCoinsPerDay);

            GuiUtils.drawText(display, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.showMinionsTotalCoinsPerDay;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock && SkyblockInfo.localLocation.equals("Your Island");
        }
        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth(display);
        }
    }
}
