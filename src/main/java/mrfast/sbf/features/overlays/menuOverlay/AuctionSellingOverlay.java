package mrfast.sbf.features.overlays.menuOverlay;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.gui.SideMenu.*;
import mrfast.sbf.mixins.transformers.GuiContainerAccessor;
import mrfast.sbf.mixins.transformers.GuiEditSignAccessor;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionSellingOverlay {
    static CustomElement priceInput;
    static String sellingItemName = "";
    static String sellingType = "";

    @SubscribeEvent
    public void onGuiBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!SkyblockFeatures.config.auctionGuis && !SkyblockFeatures.config.customCreateAuctionGui) return;

        if (event.gui instanceof GuiChest) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();

            if (chestName.startsWith("Auction Duration")) {
                CustomElement.lastMouseState = false;
            }

            if (chestName.contains("Create")) {
                sellingType = chestName.contains("BIN") ? "BIN" : "AUC";
                // Draw sidebar background
                if (SkyblockFeatures.config.customCreateAuctionGui) {
                    inv.setInventorySlotContents(48, ItemUtils.menuGlass);
                    inv.setInventorySlotContents(49, ItemUtils.menuGlass);
                }
                GuiContainerAccessor guiContainerAccessor = (GuiContainerAccessor) event.gui;
                int guiLeft = guiContainerAccessor.getGuiLeft();
                int guiTop = guiContainerAccessor.getGuiTop();
                int guiWidth = guiContainerAccessor.getWidth();
                int guiHeight = guiContainerAccessor.getHeight();

                for (Slot slot : gui.inventorySlots.inventorySlots) {
                    if (slot.getHasStack() && slot.getSlotIndex() == 13) {
                        ItemStack stack = slot.getStack();
                        String auctionIdentifier = PricingData.getIdentifier(stack);
                        Slot priceSlot = gui.inventorySlots.inventorySlots.get(31);
                        long currentSellingPrice = 0;
                        if (priceSlot.getHasStack()) {
                            currentSellingPrice = Long.parseLong(Utils.cleanColor(priceSlot.getStack().getDisplayName()).replaceAll("[^0-9]", ""));
                        }
                        if (SkyblockFeatures.config.auctionGuis && auctionIdentifier != null) {
                            MenuOverlayManager.Overlay overlay = MenuOverlayManager.getOrCreateMenuOverlay("Sell Auction Sidebar", event.gui);

                            Double lowestBin = PricingData.lowestBINs.get(auctionIdentifier);
                            Double avgBin = PricingData.averageLowestBINs.get(auctionIdentifier);
                            String avgBinString = ChatFormatting.RED + "Unknown";
                            if (avgBin != null) {
                                avgBinString = ChatFormatting.GOLD + Utils.nf.format(avgBin * stack.stackSize);
                            }

                            String lowestBinString = ChatFormatting.RED + "Unknown";
                            if (lowestBin != null) {
                                lowestBinString = ChatFormatting.GOLD + Utils.nf.format(lowestBin * stack.stackSize);
                            }

                            float priceToSellAt = 0f;
                            if (lowestBin != null && avgBin != null) {
                                priceToSellAt = (float) Math.round(((lowestBin * 0.6 + avgBin * 0.4)) * 0.99);
                            }

                            JsonObject auctionData = PricingData.getItemAuctionInfo(auctionIdentifier);
                            if (auctionData == null) continue;
                            int volume = auctionData.get("sales").getAsInt();
                            // Estimating time to sell based on the price and average sales
                            double estimatedTimeToSell = Math.max(((24d / volume) * (currentSellingPrice / lowestBin)) * 60 * 60, 20);

                            // Add button and text elements to the sidebar
                            overlay.content.addOrUpdateElement("1", createLowestBinElement(lowestBinString));
                            overlay.content.addOrUpdateElement("2", createAvgBinElement(avgBinString));
                            overlay.content.addOrUpdateElement("3", createSugListElement(priceToSellAt));
                            overlay.content.addOrUpdateElement("4", createSellTimeElement(Utils.secondsToTime((int) estimatedTimeToSell)));

                            overlay.content.render(guiLeft + guiWidth, guiTop, true, margin * 5, (GuiContainerAccessor) event.gui);
                        }
                        if (SkyblockFeatures.config.customCreateAuctionGui) {
                            MenuOverlayManager.Overlay overlay = MenuOverlayManager.getOrCreateMenuOverlay("Custom Sell Auction Menu", event.gui);

                            String actualItemName = getActualItemName(stack);
                            if (priceInput == null || !sellingItemName.equals(actualItemName)) {
                                priceInput = createSellInputElement(currentSellingPrice);
                                sellingItemName = actualItemName;
                            }

                            overlay.content.addOrUpdateElement("5", priceInput);
                            overlay.content.addOrUpdateElement("6", createSellButtonElement(priceSlot));
                            overlay.content.addOrUpdateElement("7", createItemNameElement((GuiContainerAccessor) event.gui, actualItemName));
                            overlay.content.addOrUpdateElement("8", createCreateElement(gui.inventorySlots.inventorySlots.get(29)));
                            overlay.content.addOrUpdateElement("9", createDurationElement(gui.inventorySlots.inventorySlots.get(33)));
                            overlay.content.addOrUpdateElement("10", createBackElement(gui.inventorySlots.inventorySlots.get(49)));
                            overlay.content.addOrUpdateElement("11", createAucTypeElement(gui.inventorySlots.inventorySlots.get(48)));
                            overlay.content.addOrUpdateElement("12", createMismatchElement((GuiContainerAccessor) event.gui, currentSellingPrice));

                            overlay.content.render(guiLeft, guiTop, false, margin * 5, (GuiContainerAccessor) event.gui);
                        }
                        break;
                    }
                }
            }
        }
    }

    private String getActualItemName(ItemStack stack) {
        if (ItemUtils.getItemLore(stack).size() > 1) {
            return ItemUtils.getItemLore(stack).get(1);
        }
        return "";
    }

    int margin = Utils.GetMC().fontRendererObj.FONT_HEIGHT + 1;

    private CustomElement createLowestBinElement(String lowestBin) {
        return new CustomTextElement(4, 4, "Lowest BIN: §6" + lowestBin, null, null);
    }

    private CustomElement createAvgBinElement(String avgBin) {
        return new CustomTextElement(4, 4 + margin, "Average BIN: §6" + avgBin, null, null);
    }

    private CustomElement createSugListElement(Float suggestedPrice) {
        return new CustomTextElement(4, 4 + margin * 2, "Sug. Listing Price: §b" + Utils.nf.format(suggestedPrice), "§cThis does not count item upgrades!", null);
    }

    private CustomElement createSellTimeElement(String time) {
        return new CustomTextElement(4, 4 + margin * 3, "Est. Time To Sell: §a" + time, null, null);
    }

    private CustomElement createSellInputElement(float sellPrice) {
        int x = 64 + 4, y = margin * 7 + 2, width = 40, height = 16;

        return new TextInputElement(x, y, Utils.shortenNumber(sellPrice), width, height);
    }

    private CustomElement createSellButtonElement(Slot slot) {
        int x = 59 + 4, y = margin * 9 + 1, width = 50, height = 16;

        String hoverText = null;
        if (slot.getStack() != null) {
            hoverText = slot.getStack().getDisplayName() + "\n" + String.join("\n", ItemUtils.getItemLore(slot.getStack()));
        }
        Runnable onClickAction = () -> {
            setAuctionPrice(((TextInputElement) priceInput).textField.getText());
        };
        return new CustomButtonElement(x, y, "§aSet Price", width, height, hoverText, onClickAction);
    }

    private CustomElement createCreateElement(Slot slot) {
        int x = 7 + 4, y = margin * 7 + 1, width = 50, height = 18;

        String hoverText = null;
        if (slot.getStack() != null) {
            hoverText = slot.getStack().getDisplayName() + "\n" + String.join("\n", ItemUtils.getItemLore(slot.getStack()));
        }
        Runnable onClickAction = () -> {
            Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, slot.slotNumber, 0, 4, Utils.GetMC().thePlayer);
        };
        return new CustomButtonElement(x, y, "§aCreate", width, height, hoverText, onClickAction);
    }

    private CustomElement createDurationElement(Slot slot) {
        int x = 111 + 4, y = 3 + margin * 6 + 8, width = 50, height = 18;

        String hoverText = null;
        String durationString = "Unknown";
        if (slot.getStack() != null) {
            durationString = Utils.cleanColor(slot.getStack().getDisplayName()).split(": ")[1];
            hoverText = slot.getStack().getDisplayName() + "\n" + String.join("\n", ItemUtils.getItemLore(slot.getStack()));
        }
        Runnable onClickAction = () -> {
            Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, slot.slotNumber, 0, 4, Utils.GetMC().thePlayer);
        };
        return new CustomButtonElement(x, y, "§b" + durationString, width, height, hoverText, onClickAction);
    }

    private CustomElement createAucTypeElement(Slot slot) {
        int x = 131 + 4, y = margin * 10 + 8, width = 30, height = 16;

        String hoverText = null;
        String otherSellType = sellingType.equals("AUC") ? "BIN" : "AUC";
        if (slot.getStack() != null) {
            hoverText = "§aSwap to " + otherSellType;
        }
        Runnable onClickAction = () -> {
            Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, slot.slotNumber, 0, 4, Utils.GetMC().thePlayer);
        };
        return new CustomButtonElement(x, y, "§e" + sellingType, width, height, hoverText, onClickAction);
    }

    private CustomElement createBackElement(Slot slot) {
        int x = 7 + 4, y = margin * 10 + 8, width = 30, height = 16;

        Runnable onClickAction = () -> {
            Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, slot.slotNumber, 0, 4, Utils.GetMC().thePlayer);
        };
        return new CustomButtonElement(x, y, "§cBack", width, height, null, onClickAction);
    }

    private CustomElement createItemNameElement(GuiContainerAccessor gui, String itemName) {
        int nameWidth = Utils.GetMC().fontRendererObj.getStringWidth(Utils.cleanColor(itemName));
        int x = (gui.getWidth() / 2) - ((nameWidth + 8) / 2), y = 20;

        return new CustomTextElement(x, y, itemName, null, null);
    }

    private CustomElement createMismatchElement(GuiContainerAccessor gui, long sellingPrice) {
        String warningText = "§c§lWarning! Unset price!";
        int textWidth = Utils.GetMC().fontRendererObj.getStringWidth(Utils.cleanColor(warningText));
        int x = (gui.getWidth() / 2) - ((textWidth + 8) / 2), y = margin * 6 - 3;
        String customPrice = ((TextInputElement) priceInput).textField.getText();
        String regex = "\\b\\d+(\\.\\d+)?[kmbt]?\\b";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(customPrice);
        if (matcher.find()) {
            // Check if price input: 10,000 10k == 10k  or    check if prices are 98% similar
            float customPriceNum = 0;
            try {
                customPriceNum = Float.parseFloat(customPrice.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {
            }
            if (Utils.shortenNumber(sellingPrice).equals(customPrice) || Utils.expandShortenedNumber(customPrice) == sellingPrice || sellingPrice == customPriceNum) {
                return new CustomTextElement(0, 0, "", null, null);
            }

            return new CustomTextElement(x, y, warningText, "§cClick on set price button to set the price!", null);
        }

        return new CustomTextElement(x, y, "§c§lWarning! Invalid price!", "§cYou incorrectly wrote out your items price!", null);
    }


    public void setAuctionPrice(String price) {
        new Thread(() -> {
            try {
                Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, 31, 0, 4, Utils.GetMC().thePlayer);
                // Wait for sign to popup
                while (!(Utils.GetMC().currentScreen instanceof GuiEditSign)) {
                    Thread.sleep(100);
                }
                GuiEditSignAccessor signGui = (GuiEditSignAccessor) Utils.GetMC().currentScreen;
                signGui.getTileSign().signText[0] = new ChatComponentText(price);
                signGui.getTileSign().markDirty();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
