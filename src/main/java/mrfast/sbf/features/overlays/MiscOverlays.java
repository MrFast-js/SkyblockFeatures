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
            GuiUtils.drawText("["+getTime()+"]",0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public void drawElementExample() {
            GuiUtils.drawText("["+getTime()+"]",0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.clock;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock;
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
            long time = Utils.GetMC().theWorld.getWorldTime();
            double timeDouble = (double) time /20/60/20;
            double day = (Math.round(timeDouble*100.0))/100.0;

            GuiUtils.drawText(ChatFormatting.GREEN+"Day "+day,0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public void drawElementExample() {
            GuiUtils.drawText(ChatFormatting.GREEN+"Day "+2,0,0, GuiUtils.TextStyle.BLACK_OUTLINE);

        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.dayTracker;
        }

        @Override
        public boolean getRequirement() {
            return CrystalHollowsMap.inCrystalHollows && Utils.inSkyblock;
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


}
