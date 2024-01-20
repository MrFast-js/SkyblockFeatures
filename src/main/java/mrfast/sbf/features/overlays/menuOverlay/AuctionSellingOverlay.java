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
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AuctionSellingOverlay {
    static CustomElement priceInput;
    static String sellingItemName = "";
    static String sellingType = "";

    @SubscribeEvent
    public void onGuiBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!SkyblockFeatures.config.auctionGuis && !SkyblockFeatures.config.customCreateAuctionGui) return;

        if (event.gui instanceof GuiChest) {
            SideMenu sidebar = SideMenuManager.getOrCreateSidebar((GuiContainer) event.gui);
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
                if(SkyblockFeatures.config.customCreateAuctionGui) {
                    inv.setInventorySlotContents(48, ItemUtils.menuGlass);
                    inv.setInventorySlotContents(49, ItemUtils.menuGlass);
                }

                sidebar.render(4, 0, 150, margin * 5, (GuiContainerAccessor) event.gui);

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
                            double salesPerHour = Math.max(((24d / volume) * (currentSellingPrice / lowestBin)) * 60 * 60, 20);

                            // Add button and text elements to the sidebar
                            sidebar.addOrUpdateElement("1", createLowestBinElement(lowestBinString));
                            sidebar.addOrUpdateElement("2", createAvgBinElement(avgBinString));
                            sidebar.addOrUpdateElement("3", createSugListElement(priceToSellAt));
                            sidebar.addOrUpdateElement("4", createSellTimeElement(Utils.secondsToTime((int) salesPerHour)));
                        }
                        if (SkyblockFeatures.config.customCreateAuctionGui) {
                            String actualItemName = getActualItemName(stack);
                            if (priceInput == null || !sellingItemName.equals(actualItemName)) {
                                priceInput = createSellInputElement(currentSellingPrice);
                                sellingItemName = actualItemName;
                            }

                            sidebar.addOrUpdateElement("5", priceInput);
                            sidebar.addOrUpdateElement("6", createSellButtonElement(priceSlot));
                            sidebar.addOrUpdateElement("7", createItemNameElement((GuiContainerAccessor) event.gui, actualItemName));
                            sidebar.addOrUpdateElement("8", createCreateElement(gui.inventorySlots.inventorySlots.get(29)));
                            sidebar.addOrUpdateElement("9", createDurationElement(gui.inventorySlots.inventorySlots.get(33)));
                            sidebar.addOrUpdateElement("10", createBackElement(gui.inventorySlots.inventorySlots.get(49)));
                            sidebar.addOrUpdateElement("11", createAucTypeElement(gui.inventorySlots.inventorySlots.get(48)));
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
        int x = -112, y = 4 + margin * 5 + 18, width = 40, height = 16;

        return new TextInputElement(x, y, Utils.shortenNumber(sellPrice), width, height);
    }

    private CustomElement createSellButtonElement(Slot slot) {
        int x = -117, y = 5 + margin * 5 + 18 + 18, width = 50, height = 16;

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
        int x = -160 - 10 + 1, y = 3 + margin * 5 + 18, width = 50, height = 18;

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
        int x = -75 + 10, y = 3 + margin * 5 + 18, width = 50, height = 18;

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
        int x = -75 + 10 + 20, y = 3 + margin * 5 + 18 + 1 + 18 + 18, width = 30, height = 16;

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
        int x = -160 - 10, y = 3 + margin * 5 + 18 + 1 + 18 + 18, width = 30, height = 16;

        Runnable onClickAction = () -> {
            Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, slot.slotNumber, 0, 4, Utils.GetMC().thePlayer);
        };
        return new CustomButtonElement(x, y, "§cBack", width, height, null, onClickAction);
    }

    private CustomElement createItemNameElement(GuiContainerAccessor gui, String itemName) {
        int nameWidth = Utils.GetMC().fontRendererObj.getStringWidth(Utils.cleanColor(itemName));
        int x = (-gui.getWidth() / 2) - ((nameWidth + 8) / 2) - 4, y = 20, width = nameWidth + 8, height = 10;

        return new CustomTextElement(x, y, itemName, null, null);
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
