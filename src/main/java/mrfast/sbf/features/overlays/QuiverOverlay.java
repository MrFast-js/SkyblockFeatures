package mrfast.sbf.features.overlays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.ConfigManager;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.PacketEvent;
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
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class QuiverOverlay {
    public static Minecraft mc = Utils.GetMC();
    static {
        new quiverDisplay();
    }

    @SubscribeEvent
    public void onTitleDrawn(GuiContainerEvent.TitleDrawnEvent event) {
        if(!SkyblockFeatures.config.quiverOverlay) return;

        if(event.displayName.equals("Quiver") && event.chestInventory!=null) {
            int quiverArrows = 0;
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
            ConfigManager.saveConfig();
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.ReceiveEvent event) {
        if(!SkyblockFeatures.config.quiverOverlay|| Utils.GetMC().thePlayer==null) return;

        if(event.packet instanceof S29PacketSoundEffect) {
            S29PacketSoundEffect packet = (S29PacketSoundEffect) event.packet;
            ItemStack heldItem = Utils.GetMC().thePlayer.getHeldItem();
            if(heldItem==null) return;
            boolean holdingBow = (heldItem.getItem() instanceof ItemBow);
            if(packet.getSoundName().equals("random.bow") && holdingBow) {
                SkyblockFeatures.config.quiverOverlayCount--;
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
            if(SkyblockFeatures.config.quiverOverlayOnlyBow) {
                ItemStack held = Utils.GetMC().thePlayer.getHeldItem();
                if(held==null || !(held.getItem() instanceof ItemBow)) {
                    return;
                }
            }
            int quiverArrows = SkyblockFeatures.config.quiverOverlayCount;
            String display = quiverArrows!=0?"§r§7x"+Utils.nf.format(quiverArrows):"§cOpen Quiver";
            RenderUtil.renderItemStackOnScreen(new ItemStack(Items.arrow),0,0,12,12);
            GuiUtils.drawText(display,14,2, GuiUtils.TextStyle.DROP_SHADOW);
        }

        @Override
        public void drawElementExample() {
            int quiverArrows = SkyblockFeatures.config.quiverOverlayCount;
            String display = quiverArrows!=0?"§r§7x"+Utils.nf.format(quiverArrows):"§cOpen Quiver";
            RenderUtil.renderItemStackOnScreen(new ItemStack(Items.arrow),0,0,12,12);
            GuiUtils.drawText(display,14,2, GuiUtils.TextStyle.DROP_SHADOW);
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
            return 16+Utils.GetMC().fontRendererObj.getStringWidth("x2893");
        }
    }
}
