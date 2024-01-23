package mrfast.sbf.gui.ProfileViewer.Pages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIImage;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.constraints.*;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerGui;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerUtils;
import mrfast.sbf.gui.components.InventoryComponent;
import mrfast.sbf.gui.components.ItemStackComponent;
import mrfast.sbf.utils.ItemRarity;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.NetworkUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RiftPage extends ProfileViewerGui.ProfileViewerPage {
    @Override
    public void loadPage() {
        if (ProfileViewerGui.ProfilePlayerResponse == null) return;

        JsonObject riftObj = null;
        try {
            riftObj = ProfileViewerGui.ProfilePlayerResponse.get("rift").getAsJsonObject();
        } catch (Exception ignored) {
        }
        JsonArray trophiesArray = null;
        try {
            assert riftObj != null;
            trophiesArray = riftObj.get("gallery").getAsJsonObject().get("secured_trophies").getAsJsonArray();
        } catch (Exception ignored) {
        }

        UIComponent topContainer = new UIBlock(new Color(0, 0, 0, 0))
                .setWidth(new RelativeConstraint(1f))
                .setChildOf(this.mainComponent)
                .setHeight(new RelativeConstraint(0.65f));
        UIComponent bottomContainer = new UIBlock(new Color(0, 0, 0, 0))
                .setWidth(new RelativeConstraint(1f))
                .setChildOf(this.mainComponent)
                .setY(new SiblingConstraint(0f))
                .setHeight(new RelativeConstraint(0.4f));
        // Stats
        if (ProfileViewerGui.ProfilePlayerResponse.has("stats")) {
            JsonObject statsObj = ProfileViewerGui.ProfilePlayerResponse.get("stats").getAsJsonObject();

            int motesPurse = Utils.safeGetInt(ProfileViewerGui.ProfilePlayerResponse, "motes_purse");
            int lifeTimeMotes = Utils.safeGetInt(statsObj, "rift_lifetime_motes_earned");
            int moteOrbsCollected = Utils.safeGetInt(statsObj, "rift_motes_orb_pickup");
            boolean crazyKloonCompleted = false;
            boolean mirrorverseCompleted = false;

            int secondsSitting = 0;
            int foundEnigmaSouls = 0;
            int eyesUnlocked = 0;
            int burgerStacks = 0;
            JsonObject katObj = null;

            try {
                foundEnigmaSouls = riftObj.get("enigma").getAsJsonObject().get("found_souls").getAsJsonArray().size();
            } catch (Exception ignored) {
            }
            try {
                burgerStacks = riftObj.get("castle").getAsJsonObject().get("grubber_stacks").getAsInt();
            } catch (Exception ignored) {
            }
            try {
                secondsSitting = riftObj.get("village_plaza").getAsJsonObject().get("lonely").getAsJsonObject().get("seconds_sitting").getAsInt();
            } catch (Exception ignored) {
            }
            try {
                katObj = riftObj.get("west_village").getAsJsonObject().get("kat_house").getAsJsonObject();
            } catch (Exception ignored) {
            }
            try {
                eyesUnlocked = riftObj.get("wither_cage").getAsJsonObject().get("killed_eyes").getAsJsonArray().size();
            } catch (Exception ignored) {
            }
            try {
                crazyKloonCompleted = riftObj.get("west_village").getAsJsonObject().get("crazy_kloon").getAsJsonObject().get("quest_complete").getAsBoolean();
            } catch (Exception ignored) {
            }
            try {
                mirrorverseCompleted = riftObj.get("west_village").getAsJsonObject().get("mirrorverse").getAsJsonObject().get("claimed_reward").getAsBoolean();
            } catch (Exception ignored) {
            }

            UIComponent left = new UIBlock(new Color(0, 0, 0, 0))
                    .setWidth(new RelativeConstraint(0.3f))
                    .setY(new PixelConstraint(4f))
                    .setChildOf(topContainer)
                    .setHeight(new RelativeConstraint(1f));

            new UIText(ChatFormatting.YELLOW + "" + ChatFormatting.BOLD + "Rift Stats").setChildOf(left).setY(new SiblingConstraint(4f)).setX(new CenterConstraint());
            new UIText(ChatFormatting.GRAY + "Motes: " + ChatFormatting.DARK_PURPLE + ChatFormatting.BOLD + Utils.nf.format(motesPurse)).setY(new SiblingConstraint(2f)).setChildOf(left);
            new UIText(ChatFormatting.GRAY + "Lifetime Motes: " + ChatFormatting.DARK_PURPLE + ChatFormatting.BOLD + Utils.nf.format(lifeTimeMotes)).setY(new SiblingConstraint(2f)).setChildOf(left);
            new UIText(ChatFormatting.GRAY + "Mote Orbs Collected: " + ChatFormatting.YELLOW + ChatFormatting.BOLD + Utils.nf.format(moteOrbsCollected)).setY(new SiblingConstraint(2f)).setChildOf(left);

            new UIText(ChatFormatting.GOLD + "Enigma Souls: " + ChatFormatting.LIGHT_PURPLE + ChatFormatting.BOLD + foundEnigmaSouls + " / 42").setY(new SiblingConstraint(10f)).setChildOf(left);
            new UIText(ChatFormatting.GOLD + "Burgers: " + ChatFormatting.LIGHT_PURPLE + ChatFormatting.BOLD + burgerStacks + "/5").setY(new SiblingConstraint(2f)).setChildOf(left);
            new UIText(ChatFormatting.GOLD + "Seconds Sitting : " + ChatFormatting.YELLOW + Utils.secondsToTime(secondsSitting)).setY(new SiblingConstraint(2f)).setChildOf(left);
            new UIText(ChatFormatting.GOLD + "Porhtal Eyes Unlocked: " + ChatFormatting.YELLOW + eyesUnlocked).setY(new SiblingConstraint(2f)).setChildOf(left);

            new UIText(ChatFormatting.YELLOW + "Crazy Kloon: " + (crazyKloonCompleted ? ChatFormatting.GREEN.toString() + ChatFormatting.BOLD + "Completed" : ChatFormatting.RED + "Incomplete")).setY(new SiblingConstraint(10f)).setChildOf(left);
            new UIText(ChatFormatting.YELLOW + "Mirrorverse: " + (mirrorverseCompleted ? ChatFormatting.GREEN.toString() + ChatFormatting.BOLD + "Completed" : ChatFormatting.RED + "Incomplete")).setY(new SiblingConstraint(2f)).setChildOf(left);
            if (katObj != null && katObj.has("bin_collected_spider")) {
                new UIText(ChatFormatting.DARK_PURPLE + "Kat House ").setY(new SiblingConstraint(12f)).setChildOf(left);
                new UIText(ChatFormatting.GRAY + " Spiders Collected: " + ChatFormatting.AQUA + ChatFormatting.BOLD + katObj.get("bin_collected_spider").getAsInt()).setY(new SiblingConstraint(2f)).setChildOf(left);
                new UIText(ChatFormatting.GRAY + " Mosquito Collected: " + ChatFormatting.AQUA + ChatFormatting.BOLD + katObj.get("bin_collected_mosquito").getAsInt()).setY(new SiblingConstraint(2f)).setChildOf(left);
                new UIText(ChatFormatting.GRAY + " Silverfish Collected: " + ChatFormatting.AQUA + ChatFormatting.BOLD + katObj.get("bin_collected_silverfish").getAsInt()).setY(new SiblingConstraint(2f)).setChildOf(left);
            }
        }
        // Inventories
        {
            UIComponent right = new UIBlock(new Color(0, 0, 0, 0))
                    .setWidth(new RelativeConstraint(0.65f))
                    .setY(new PixelConstraint(5f))
                    .setX(new PixelConstraint(5f, true))
                    .setChildOf(topContainer)
                    .setHeight(new RelativeConstraint(1f));

            InventoryBasic inv = new InventoryBasic("Test#1", true, 36);
            InventoryBasic enderChest = new InventoryBasic("Test#1", true, 45);
            InventoryBasic enderChest2 = new InventoryBasic("Test#1", true, 45);

            JsonObject inventory = null;
            if (riftObj != null && riftObj.has("inventory")) {
                inventory = riftObj.get("inventory").getAsJsonObject();
                if (inventory.has("inv_contents")) {
                    String inventoryBase64 = inventory.get("inv_contents").getAsJsonObject().get("data").getAsString();
                    ItemUtils.Inventory items = new ItemUtils.Inventory(inventoryBase64);
                    List<ItemStack> a = ItemUtils.decodeInventory(items, true);

                    int index = 0;
                    for (ItemStack item : a) {
                        inv.setInventorySlotContents(index, item);
                        index++;
                    }
                }
                if (inventory.has("ender_chest_contents")) {
                    String inventoryBase64 = inventory.get("ender_chest_contents").getAsJsonObject().get("data").getAsString();
                    ItemUtils.Inventory items = new ItemUtils.Inventory(inventoryBase64);
                    List<ItemStack> a = ItemUtils.decodeInventory(items, false);

                    int index = 0;
                    // Handle differently because rift is weird af
                    for (ItemStack item : a) {
                        if (index < 45) {
                            enderChest.setInventorySlotContents(index, item);
                        } else {
                            int newIndex = index - 45;
                            enderChest2.setInventorySlotContents(newIndex, item);
                        }
                        index++;
                    }
                }

            }

            UIComponent inventoryContainer = new UIBlock(new Color(0, 0, 0, 0))
                    .setWidth(new RelativeConstraint(0.5f))
                    .setX(new PixelConstraint(0f))
                    .setY(new PixelConstraint(0f))
                    .setChildOf(right)
                    .setHeight(new RelativeConstraint(0.4f));

            new InventoryComponent(inv, "Inventory")
                    .setChildOf(inventoryContainer)
                    .setX(new PixelConstraint(0f));

            UIComponent enderChestsContainer = new UIBlock(new Color(0, 0, 0, 0))
                    .setWidth(new RelativeConstraint(1f))
                    .setX(new PixelConstraint(0f))
                    .setY(new SiblingConstraint(0f))
                    .setChildOf(right)
                    .setHeight(new RelativeConstraint(0.4f));

            UIComponent enderChestContainer = new UIBlock(new Color(0, 0, 0, 0))
                    .setWidth(new RelativeConstraint(0.5f))
                    .setX(new PixelConstraint(0f))
                    .setY(new PixelConstraint(0f))
                    .setChildOf(enderChestsContainer)
                    .setHeight(new RelativeConstraint(0.4f));

            new InventoryComponent(enderChest, "Ender Chest Page 1")
                    .setChildOf(enderChestContainer)
                    .setX(new PixelConstraint(0f));

            UIComponent enderChestContainer2 = new UIBlock(new Color(0, 0, 0, 0))
                    .setChildOf(enderChestsContainer)
                    .setWidth(new RelativeConstraint(0.5f))
                    .setX(new SiblingConstraint(2f))
                    .setY(new PixelConstraint(0f))
                    .setHeight(new RelativeConstraint(0.4f));

            new InventoryComponent(enderChest2, "Ender Chest Page 2")
                    .setChildOf(enderChestContainer2)
                    .setX(new PixelConstraint(0f));

            {
                UIComponent armorEquipContainer = new UIBlock(new Color(0, 0, 0, 0))
                        .setX(new RelativeConstraint(0.5f))
                        .setY(new PixelConstraint(12f))
                        .setWidth(new RelativeConstraint(0.5f))
                        .setHeight(new RelativeConstraint(0.5f));

                UIComponent armorComponent = new UIBlock(new Color(0, 0, 0, 0))
                        .setX(new CenterConstraint())
                        .setY(new PixelConstraint(8f))
                        .setWidth(new PixelConstraint(4 * 21f))
                        .setHeight(new PixelConstraint(20f));

                if (inventory != null && inventory.has("inv_armor")) {
                    String inventoryBase64 = inventory.get("inv_armor").getAsJsonObject().get("data").getAsString();
                    ItemUtils.Inventory items = new ItemUtils.Inventory(inventoryBase64);
                    List<ItemStack> a = ItemUtils.decodeInventory(items, true);
                    List<ItemStack> b = new ArrayList<>(Arrays.asList(null, null, null, null));

                    int index = 0;
                    for (ItemStack item : a) {
                        index++;
                        // Leggings
                        if (index == 1) b.set(2, item);
                        // Chestplate
                        if (index == 2) b.set(1, item);
                        // Helmet
                        if (index == 3) b.set(0, item);
                        // Boots
                        if (index == 4) b.set(3, item);
                    }
                    for (ItemStack item : b) {
                        UIComponent backgroundSlot = new UIRoundedRectangle(3f)
                                .setHeight(new PixelConstraint(20f))
                                .setWidth(new PixelConstraint(20f))
                                .setX(new SiblingConstraint(1f))
                                .setColor(new Color(100, 100, 100, 200));

                        backgroundSlot.addChild(new ItemStackComponent(item)
                                .setHeight(new PixelConstraint(20f))
                                .setWidth(new PixelConstraint(20f))
                                .setX(new CenterConstraint())
                                .setY(new CenterConstraint()));
                        armorComponent.addChild(backgroundSlot);
                    }
                }

                UIComponent equipmentComponent = new UIBlock(new Color(0, 0, 0, 0))
                        .setX(new CenterConstraint())
                        .setY(new SiblingConstraint(2f))
                        .setWidth(new PixelConstraint(4 * 17f))
                        .setHeight(new PixelConstraint(16f));

                if (inventory != null && inventory.has("equippment_contents")) {
                    String inventoryBase64 = inventory.get("equippment_contents").getAsJsonObject().get("data").getAsString();
                    ItemUtils.Inventory items = new ItemUtils.Inventory(inventoryBase64);
                    List<ItemStack> a = ItemUtils.decodeInventory(items, false);

                    for (ItemStack item : a) {
                        UIComponent backgroundSlot = new UIRoundedRectangle(3f)
                                .setHeight(new PixelConstraint(16f))
                                .setWidth(new PixelConstraint(16f))
                                .setX(new SiblingConstraint(1f))
                                .setColor(new Color(100, 100, 100, 200));

                        backgroundSlot.addChild(new ItemStackComponent(item)
                                .setX(new CenterConstraint())
                                .setY(new CenterConstraint()));
                        equipmentComponent.addChild(backgroundSlot);
                    }
                }

                armorEquipContainer.addChild(armorComponent);
                armorEquipContainer.addChild(equipmentComponent);

                right.addChild(armorEquipContainer);
            }
        }
        // Time Charms
        {
            JsonObject surpremeTimecharm = ProfileViewerUtils.getTimecharm("wyldly_supreme", trophiesArray);
            JsonObject mirrorverseTimecharm = ProfileViewerUtils.getTimecharm("mirrored", trophiesArray);
            JsonObject chickenNEggTimecharm = ProfileViewerUtils.getTimecharm("chicken_n_egg", trophiesArray);
            JsonObject citizenTimecharm = ProfileViewerUtils.getTimecharm("citizen", trophiesArray);
            JsonObject livingTimecharm = ProfileViewerUtils.getTimecharm("lazy_living", trophiesArray);
            JsonObject globulateTimecharm = ProfileViewerUtils.getTimecharm("slime", trophiesArray);
            JsonObject vampiricTimecharm = ProfileViewerUtils.getTimecharm("vampiric", trophiesArray);

            ItemStack spruceLeaves = new ItemStack(Item.getItemById(18), 1, 1);

            new UIText(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD + "Timecharms")
                    .setChildOf(bottomContainer)
                    .setTextScale(new PixelConstraint((float) (2f)))
                    .setX(new PixelConstraint(0f))
                    .setY(new PixelConstraint(0f));

            UIComponent timecharmContainer = new UIBlock(new Color(0, 0, 0, 0))
                    .setChildOf(bottomContainer)
                    .setWidth(new RelativeConstraint(1f))
                    .setHeight(new RelativeConstraint(0.2f))
                    .setX(new PixelConstraint(0f))
                    .setY(new SiblingConstraint(2f));

            ProfileViewerUtils.createTimecharm(spruceLeaves, "Supreme Timecharm", surpremeTimecharm, timecharmContainer, this.hoverables);
            ProfileViewerUtils.createTimecharm(new ItemStack(Item.getItemFromBlock(Blocks.soul_sand)), "Chicken N Egg Timecharm", chickenNEggTimecharm, timecharmContainer, this.hoverables);
            ProfileViewerUtils.createTimecharm(new ItemStack(Item.getItemFromBlock(Blocks.glass)), "mrahcemiT esrevrorriM", mirrorverseTimecharm, timecharmContainer, this.hoverables);

            UIComponent timecharmContainer2 = new UIBlock(new Color(0, 0, 0, 0))
                    .setChildOf(bottomContainer)
                    .setWidth(new RelativeConstraint(1f))
                    .setHeight(new RelativeConstraint(0.2f))
                    .setX(new PixelConstraint(0f))
                    .setY(new SiblingConstraint(2f));

            ProfileViewerUtils.createTimecharm(new ItemStack(Item.getItemFromBlock(Blocks.jukebox)), "SkyBlock Citizen Timecharm", citizenTimecharm, timecharmContainer2, this.hoverables);
            ProfileViewerUtils.createTimecharm(new ItemStack(Item.getItemFromBlock(Blocks.lapis_ore)), "Living Timecharm", livingTimecharm, timecharmContainer2, this.hoverables);
            ProfileViewerUtils.createTimecharm(new ItemStack(Item.getItemFromBlock(Blocks.slime_block)), "Globulate Timecharm", globulateTimecharm, timecharmContainer2, this.hoverables);
            UIComponent timecharmContainer3 = new UIBlock(new Color(0, 0, 0, 0))
                    .setChildOf(bottomContainer)
                    .setWidth(new RelativeConstraint(1f))
                    .setHeight(new RelativeConstraint(0.2f))
                    .setX(new PixelConstraint(0f))
                    .setY(new SiblingConstraint(2f));

            ProfileViewerUtils.createTimecharm(new ItemStack(Item.getItemFromBlock(Blocks.redstone_block)), "Vampiric Timecharm", vampiricTimecharm, timecharmContainer3, this.hoverables);
        }
    }

    public RiftPage(UIComponent main) {
        super(main);
    }
}
