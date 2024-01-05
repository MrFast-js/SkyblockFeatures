package mrfast.sbf.features.overlays;

import com.google.gson.JsonObject;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.DataManager;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.events.ProfileSwapEvent;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import scala.Int;

import java.util.HashMap;

public class QuiverOverlay {
    public static Minecraft mc = Utils.GetMC();
    public static String selectedArrowType = "";
    static JsonObject arrowCounts = new JsonObject();
    static boolean loadedData = false;
    static {
        new quiverDisplay();
    }

    @SubscribeEvent
    public void onProfileSwap(ProfileSwapEvent event) {
        arrowCounts = (JsonObject) DataManager.getProfileDataDefault("arrows", new JsonObject());
        selectedArrowType = (String) DataManager.getProfileDataDefault("selectedArrowType", "§fFlint Arrow");
        loadedData = true;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        readArrowFromSwapper = false;
        loadedData = false;
        sentLowArrows = false;
    }

    boolean readArrowFromSwapper = false;

    @SubscribeEvent
    public void onSlotDraw(GuiContainerEvent.DrawSlotEvent event) {
        if (!SkyblockFeatures.config.quiverOverlay) return;

        if (event.slot.getStack() != null && !readArrowFromSwapper) {
            String id = ItemUtils.getSkyBlockItemID(event.slot.getStack());
            if (id != null && id.equals("ARROW_SWAPPER")) {
                for (String s : ItemUtils.getItemLore(event.slot.getStack())) {
                    if (s.startsWith("§aSelected: ")) {
                        readArrowFromSwapper = true;
                        selectedArrowType = s.split("§aSelected: ")[1];
                        DataManager.saveProfileData("selectedArrowType", selectedArrowType);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!SkyblockFeatures.config.quiverOverlay) return;

        String clean = event.message.getUnformattedText();
        if (clean.startsWith("You set your selected arrow type to")) {
            selectedArrowType = event.message.getFormattedText().split("You set your selected arrow type to §r")[1].replace("§r§a!§r", "");
            DataManager.saveProfileData("selectedArrowType", selectedArrowType);
        }
        if (clean.equals("Cleared your quiver!") || clean.equals("Your quiver is empty!") || clean.startsWith("You don't have any more arrows left in your Quiver!")) {
            arrowCounts = new JsonObject();
            DataManager.saveProfileData("arrows", arrowCounts);
        }
        if (clean.startsWith("You filled your quiver with")) {
            double old = Integer.parseInt(clean.replaceAll("[^0-9]", ""));
            if (arrowCounts.has("§fFlint Arrow")) {
                old += arrowCounts.get("§fFlint Arrow").getAsDouble();
            }
            arrowCounts.addProperty("§fFlint Arrow", old);
            DataManager.saveProfileData("arrows", arrowCounts);
        }

        if (clean.startsWith("Jax forged")) {
            String ArrowType = event.message.getFormattedText().split("Jax forged §r")[1].split("§r§8 x")[0];
            int arrowCount = Integer.parseInt(clean.split(" x")[1].split(" ")[0].replaceAll("[^0-9]", ""));
            Utils.sendMessage(ArrowType + " " + arrowCount);
            Double oldCount = (double) arrowCount;
            if (arrowCounts.has(ArrowType)) {
                oldCount += arrowCounts.get(ArrowType).getAsDouble();
            }
            arrowCounts.addProperty(ArrowType, oldCount);
            DataManager.saveProfileData("arrows", arrowCounts);
        }
    }

    @SubscribeEvent
    public void onTitleDrawn(GuiContainerEvent.TitleDrawnEvent event) {
        if (!SkyblockFeatures.config.quiverOverlay) return;

        if (event.displayName.equals("Quiver") && event.chestInventory != null) {
            JsonObject newArrowCount = new JsonObject();
            for (int i = 0; i < event.chestInventory.getSizeInventory(); i++) {
                ItemStack stack = event.chestInventory.getStackInSlot(i);
                String id = ItemUtils.getSkyBlockItemID(stack);
                if (stack != null && stack.getItem() != null && id != null) {
                    if (stack.getItem().equals(Items.arrow) && id.contains("ARROW")) {
                        newArrowCount.addProperty(stack.getDisplayName(), newArrowCount.get(stack.getDisplayName()).getAsDouble() + stack.stackSize);
                    }
                }
            }
            arrowCounts = newArrowCount;
            DataManager.saveProfileData("arrows", arrowCounts);
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.ReceiveEvent event) {
        if (!SkyblockFeatures.config.quiverOverlay || Utils.GetMC().thePlayer == null) return;

        if (event.packet instanceof S29PacketSoundEffect) {
            S29PacketSoundEffect packet = (S29PacketSoundEffect) event.packet;
            ItemStack heldItem = Utils.GetMC().thePlayer.getHeldItem();
            if (heldItem == null) return;
            boolean holdingBow = (heldItem.getItem() instanceof ItemBow);
            if (packet.getSoundName().equals("random.bow") && holdingBow) {
                arrowShot();
            }
        }
    }

    boolean sentLowArrows = false;

    public void arrowShot() {
        ItemStack held = Utils.GetMC().thePlayer.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemBow) || ItemUtils.getSkyBlockItemID(held) == null || !loadedData) {
            return;
        }
        double countToRemove = 1;

        NBTTagCompound enchants = ItemUtils.getExtraAttributes(held).getCompoundTag("enchantments");
        if (enchants.hasKey("infinite_quiver")) {
            int level = enchants.getInteger("infinite_quiver");
            countToRemove = (1 - level * 0.03);
        }

        arrowCounts.addProperty(selectedArrowType, arrowCounts.get(selectedArrowType).getAsDouble() - countToRemove);
        if (!sentLowArrows && SkyblockFeatures.config.quiverOverlayLowArrowNotification) {
            if (arrowCounts.get(selectedArrowType).getAsDouble() < 128) {
                sentLowArrows = true;
                GuiManager.createTitle("§cRefill Quiver", 20);
            }
        }
        DataManager.saveProfileData("arrows", arrowCounts);
    }

    public static class quiverDisplay extends UIElement {
        public quiverDisplay() {
            super("quiverDisplay", new Point(0.47239587f, 0.8342593f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if (SkyblockFeatures.config.quiverOverlayOnlyBow) {
                ItemStack held = Utils.GetMC().thePlayer.getHeldItem();
                if (held == null || !(held.getItem() instanceof ItemBow)) {
                    return;
                }
            }
            drawQuiverOverlay();
        }

        @Override
        public void drawElementExample() {
            drawQuiverOverlay();
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.quiverOverlay;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return (Utils.GetMC().fontRendererObj.FONT_HEIGHT + 2);
        }

        @Override
        public int getWidth() {
            return 16 + Utils.GetMC().fontRendererObj.getStringWidth(getDisplay());
        }
    }

    public static String getDisplay() {
        double quiverArrows = -1;
        if (arrowCounts.has(selectedArrowType)) {
            quiverArrows = arrowCounts.get(selectedArrowType).getAsDouble();
        }
        quiverArrows = Math.floor(quiverArrows);

        String display = quiverArrows > 0? "§r§7x" + Utils.nf.format(quiverArrows) : "§cEmpty Quiver";
        if (selectedArrowType == null) {
            display = "§cFind Arrow Swapper";
        } else {
            if (quiverArrows != -1 && SkyblockFeatures.config.quiverOverlayType) {
                display += " " + (selectedArrowType);
            }
            if (selectedArrowType.equals("§fNone")) {
                display = "§cNo Arrow Selected";
            }
        }
        return display;
    }

    public static void drawQuiverOverlay() {
        RenderUtil.renderItemStackOnScreen(new ItemStack(Items.arrow), 0, 0, 12, 12);
        GuiUtils.drawText(getDisplay(), 14, 2, GuiUtils.TextStyle.DROP_SHADOW);
    }
}
