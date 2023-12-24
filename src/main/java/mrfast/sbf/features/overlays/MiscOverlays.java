package mrfast.sbf.features.overlays;

import java.text.SimpleDateFormat;
import java.util.*;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.features.overlays.maps.CrystalHollowsMap;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MiscOverlays {
    public static Minecraft mc = Utils.GetMC();
    static {
        new timeOverlay();
        new dayCounter();
        new quiverDisplay();
    }

    @SubscribeEvent
    public void renderHealth(RenderGameOverlayEvent.Pre event) {
        if(Utils.inSkyblock) {
            if (event.type == RenderGameOverlayEvent.ElementType.FOOD && SkyblockFeatures.config.hideHungerBar) {
                event.setCanceled(true);
            }
            if (event.type == RenderGameOverlayEvent.ElementType.HEALTH && SkyblockFeatures.config.hideHealthHearts) {
                event.setCanceled(true);
            }
            if (event.type == RenderGameOverlayEvent.ElementType.ARMOR && SkyblockFeatures.config.hideArmorBar) {
                event.setCanceled(true);
            }
        }
    }
    public static String getTime() {
        return new SimpleDateFormat("hh:mm:ss").format(new Date());
    }
    public static class timeOverlay extends UIElement {
        public timeOverlay() {
            super("timeOverlay", new Point(0f,0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                GuiUtils.drawText("["+getTime()+"]",0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
            }
        }

        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            GuiUtils.drawText("["+getTime()+"]",0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.clock;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("["+getTime()+"]");
        }
    }

    public static class dayCounter extends UIElement {
        public dayCounter() {
            super("dayCounter", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock || Utils.GetMC().theWorld==null || SkyblockInfo.getLocation()==null) return;
            if (CrystalHollowsMap.inCrystalHollows && SkyblockFeatures.config.dayTracker) {
                long time = Utils.GetMC().theWorld.getWorldTime();
                double timeDouble = (double) time /20/60/20;
                double day = (Math.round(timeDouble*100.0))/100.0;

                GuiUtils.drawText(ChatFormatting.GREEN+"Day "+day,0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
            }
        }

        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            GuiUtils.drawText(ChatFormatting.GREEN+"Day "+2,0,0, GuiUtils.TextStyle.BLACK_OUTLINE);

        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.dayTracker && CrystalHollowsMap.inCrystalHollows && Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("["+getTime()+"]");
        }
    }

    static int quiverArrows = SkyblockFeatures.config.quiverOverlayCount;

    @SubscribeEvent
    public void onTitleDrawn(GuiContainerEvent.TitleDrawnEvent event) {
        if(!SkyblockFeatures.config.quiverOverlay) return;

        if(event.displayName.equals("Quiver") && event.chestInventory!=null) {
            quiverArrows = 0;
            for (int i = 0; i < event.chestInventory.getSizeInventory(); i++) {
                ItemStack stack = event.chestInventory.getStackInSlot(i);
                String id = ItemUtils.getSkyBlockItemID(stack);
                if(stack!=null && stack.getItem()!=null && id!=null) {
                    if(stack.getItem().equals(Items.arrow) && id.contains("ARROW")) {
                        quiverArrows += stack.stackSize;
                    }
                }
            }
            SkyblockFeatures.config.quiverOverlayCount = quiverArrows;
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.ReceiveEvent event) {
        if(!SkyblockFeatures.config.quiverOverlay||Utils.GetMC().thePlayer==null) return;

        if(event.packet instanceof S29PacketSoundEffect) {
            S29PacketSoundEffect packet = (S29PacketSoundEffect) event.packet;
            ItemStack heldItem = Utils.GetMC().thePlayer.getHeldItem();
            if(heldItem==null) return;
            boolean holdingBow = (heldItem.getItem() instanceof ItemBow);
            if(packet.getSoundName().equals("random.bow") && holdingBow) {
                quiverArrows--;
            }
        }
    }

    public static class quiverDisplay extends UIElement {
        public quiverDisplay() {
            super("quiverDisplay", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock || Utils.GetMC().theWorld==null) return;
            if(SkyblockFeatures.config.quiverOverlayOnlyBow) {
                ItemStack held = Utils.GetMC().thePlayer.getHeldItem();
                if(held==null || !(held.getItem() instanceof ItemBow)) {
                    return;
                }
            }
            String display = quiverArrows!=0?"§r§7x"+Utils.nf.format(quiverArrows):"§cOpen Quiver";
            RenderUtil.renderItemStackOnScreen(new ItemStack(Items.arrow),0,0,12,12);
            GuiUtils.drawText(display,14,2, GuiUtils.TextStyle.DROP_SHADOW);
        }

        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            String display = quiverArrows!=0?"§r§7x"+Utils.nf.format(quiverArrows):"§cOpen Quiver";
            RenderUtil.renderItemStackOnScreen(new ItemStack(Items.arrow),0,0,12,12);
            GuiUtils.drawText(display,14,2, GuiUtils.TextStyle.DROP_SHADOW);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.quiverOverlay;
        }

        @Override
        public int getHeight() {
            return (Utils.GetMC().fontRendererObj.FONT_HEIGHT + 2);
        }

        @Override
        public int getWidth() {
            return 16+Utils.GetMC().fontRendererObj.getStringWidth("x2893");
        }
    }
}
