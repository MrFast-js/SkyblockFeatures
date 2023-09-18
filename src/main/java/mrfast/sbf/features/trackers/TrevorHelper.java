package mrfast.sbf.features.trackers;

import java.awt.Color;
import java.util.HashMap;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.TabListUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TrevorHelper {
    Entity tracking = null;
    HashMap<String,BlockPos> biomeLocations = new HashMap<>();
    boolean animalKilled = false;
    @SubscribeEvent
    public void onWorldChange(SecondPassedEvent event) {
        if(!SkyblockFeatures.config.trevorHelper || biomeLocations.size()>0 || !Utils.inSkyblock) return;
        tracking = null;
        biomeLocations.put("Settlement", new BlockPos(167,76,-377));
        biomeLocations.put("Gorge", new BlockPos(282,46,-488));
        biomeLocations.put("Overgrown", new BlockPos(248,54,-381));
        biomeLocations.put("Glowing", new BlockPos(204,41,-520));
        biomeLocations.put("Mountain", new BlockPos(236,142,-492));
        biomeLocations.put("Oasis", new BlockPos(141,77,-495));
        animalKilled = false;
    }
    String location = "";
    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {
        if(!Utils.inSkyblock || SkyblockInfo.getInstance().getLocation()==null || !SkyblockFeatures.config.trevorHelper) return;
        if(!SkyblockInfo.getInstance().map.contains("The Farming Islands")) return;

        String msg = event.message.getUnformattedText();
        if(msg.contains("[NPC] Trevor")) {
            tracking = null;
            animalKilled = false;
        }
        if(msg.contains("animal near the ")) {
            Utils.setTimeout(()->{
                Utils.SendMessage(ChatFormatting.GOLD+"Biome location marked with beacon.");
                GuiManager.createTitle(ChatFormatting.GOLD+"Marked Biome", 20);
                Utils.setTimeout(()->{
                    if(location.contains("Oasis") || location.contains("Settlement")) {
                        ChatComponentText message = new ChatComponentText(EnumChatFormatting.GREEN+""+EnumChatFormatting.BOLD + " [WARP]");
    
                        message.setChatStyle(message.getChatStyle()
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp desert"))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN+"/warp desert"))));
                        
                        Utils.SendMessage(
                            new ChatComponentText("")
                            .appendText(ChatFormatting.GOLD+"Location Near Spawn!")
                            .appendSibling(message)
                        );
                    }
                }, 1000);
            }, 100);
        }
        if(msg.contains("Return to the Trapper soon to get a new animal to hunt")) {
            animalKilled = true;
            ChatComponentText message = new ChatComponentText(EnumChatFormatting.GREEN+""+EnumChatFormatting.BOLD + " [WARP]");

            message.setChatStyle(message.getChatStyle()
            .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp trapper"))
            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN+"/warp trapper"))));
            
            Utils.SendMessage(
                new ChatComponentText("")
                .appendText(ChatFormatting.GOLD+"Marked Trevor!")
                .appendSibling(message)
            );

            GuiManager.createTitle(ChatFormatting.GOLD+"Marked Trevor", 20);
        }
    }

    @SubscribeEvent
    public void onTick(RenderWorldLastEvent event) {
        if(!Utils.inSkyblock || SkyblockInfo.getInstance().getLocation()==null || !SkyblockFeatures.config.trevorHelper) return;
        if(!SkyblockInfo.getInstance().map.contains("The Farming Islands")) return;
        
        for (NetworkPlayerInfo pi : TabListUtils.getTabEntries()) {
            String name = Utils.GetMC().ingameGUI.getTabList().getPlayerName(pi);
            for(String loc:biomeLocations.keySet()) {
                if(name.contains(loc)) {
                    location = Utils.cleanColor(name);
                }
            }
        }
        for(Entity entity:Utils.GetMC().theWorld.loadedEntityList) {
            try {
                if(entity instanceof EntityArmorStand) {
                    if(entity==null || !entity.hasCustomName()) continue;
                    String n = entity.getName();
                    if(n.contains("Trackable") || n.contains("Undetected") || n.contains("Untrackable") || n.contains("Endangered") || n.contains("Elusive")) {
                        for(Entity entity2:Utils.GetMC().theWorld.loadedEntityList) {
                            if((entity2 instanceof EntityPig || entity2 instanceof EntitySheep || entity2 instanceof EntityRabbit || entity2 instanceof EntityCow || entity2 instanceof EntityHorse || entity2 instanceof EntityPig || entity2 instanceof EntityChicken)) {
                                if(entity.getDistanceToEntity(entity2)<3) {
                                    tracking = entity2;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        for(String loc:biomeLocations.keySet()) {
            if(location.contains(loc) && !animalKilled) {
                BlockPos pos = biomeLocations.get(loc);
                GlStateManager.disableDepth();
                RenderUtil.drawWaypoint(pos, new Color(0x00FFFF),ChatFormatting.GOLD+loc, event.partialTicks);
                GlStateManager.enableDepth();
            }
        }
        if(tracking!=null && !animalKilled) {
            AxisAlignedBB box = new AxisAlignedBB(tracking.posX-0.5, tracking.posY, tracking.posZ-0.5, tracking.posX+0.5, tracking.posY+1.5, tracking.posZ+0.5);
            RenderUtil.drawOutlinedFilledBoundingBox(box, new Color(0xBB00FF), event.partialTicks);
            box = new AxisAlignedBB(tracking.posX-0.1, tracking.posY, tracking.posZ-0.1, tracking.posX+0.1, tracking.posY+15, tracking.posZ+0.1);
            RenderUtil.drawOutlinedFilledBoundingBox(box, new Color(0xBB00FF), event.partialTicks);
            if(tracking.isDead) tracking = null;
        }
        if(animalKilled) {
            BlockPos pos = new BlockPos(287,101,-571);
            GlStateManager.disableDepth();
            RenderUtil.drawWaypoint(pos, new Color(0x00FFFF),ChatFormatting.GOLD+"Trevor", event.partialTicks);
            GlStateManager.enableDepth();
        }
    }
}
