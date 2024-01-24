package mrfast.sbf.features.dungeons;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.UseItemAbilityEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FireFreezeHelper {
    String bossDialogue = "[BOSS] The Professor: Oh? You found my Guardians' one weakness?";

    static String display = "";
    @SubscribeEvent
    public void onPlayerInteract(UseItemAbilityEvent event) {
        if (!Utils.inDungeons || !SkyblockFeatures.config.fireFreezeHelper || !SkyblockFeatures.config.blockEarlyFireFreeze || !SkyblockInfo.localLocation.contains("3")) return;

        if(event.ability.itemId.equals("FIRE_FREEZE_STAFF")) {
            ItemStack held = Utils.GetMC().thePlayer.getHeldItem();
            if(!shouldFireFreeze && held != null && !DungeonsFeatures.dungeonStarted) {
                event.setCanceled(true);
            }
        }
    }
    static boolean shouldFireFreeze = false;
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || event.type == 2 || !SkyblockFeatures.config.fireFreezeHelper || !SkyblockFeatures.config.fireFreezeTimer || !SkyblockInfo.localLocation.contains("3")) return;
        String text = event.message.getUnformattedText();
        if(text.startsWith(bossDialogue)) {
            for(int i=1; i<=8;i++) {
                String count = i >= 6 ? "§aFire Freeze Now!" : "§cFire Freeze in " + (6 - i) + " seconds";

                int a = i;
                Utils.setTimeout(()->{
                    display = count;

                    if(a == 6) shouldFireFreeze = true;
                    if(a == 8) display = "";
                },i*880);
            }
        }
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        display = "";
        shouldFireFreeze = false;
    }

    static {
        new FireFreezeGUI();
    }

    public static class FireFreezeGUI extends UIElement {

        public FireFreezeGUI() {
            super("Fire Freeze Timer", new Point(0.37552083f, 0.6f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            float scale = 2f;

            GlStateManager.scale(scale, scale, 0);
            GuiUtils.drawText(display, 0, 0, GuiUtils.TextStyle.DROP_SHADOW);
            GlStateManager.scale(1 / scale, 1 / scale, 0);
        }

        @Override
        public void drawElementExample() {
            float scale = 2f;

            GlStateManager.scale(scale, scale, 0);
            GuiUtils.drawText("§cFire Freeze in 5 seconds!", 0, 0, GuiUtils.TextStyle.DROP_SHADOW);
            GlStateManager.scale(1/scale, 1/scale, 0);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.fireFreezeTimer;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inDungeons && Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*2;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("§6Fire Freeze in 5 seconds")*2;
        }
    }
}
