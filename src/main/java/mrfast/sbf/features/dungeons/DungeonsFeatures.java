package mrfast.sbf.features.dungeons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import mrfast.sbf.utils.GuiUtils;
import org.lwjgl.input.Mouse;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.BlockSkull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DungeonsFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static Entity livid = null;

    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        if(!Utils.inDungeons) return;
        if(SkyblockFeatures.config.highlightBats) {
            for(Entity entity:mc.theWorld.loadedEntityList) {
                if(entity instanceof EntityBat && !entity.isInvisible()) {
                    RenderUtil.drawOutlinedFilledBoundingBox(entity.getEntityBoundingBox(),SkyblockFeatures.config.highlightBatColor,event.partialTicks);
                }
            }
        }

        if (mc.theWorld != null && SkyblockFeatures.config.highlightDoors) {
            for(TileEntity entity:mc.theWorld.loadedTileEntityList) {
                if (entity instanceof TileEntitySkull) {
                    TileEntitySkull skull = (TileEntitySkull) entity;
                    BlockPos pos = entity.getPos();
                    if(Utils.GetMC().thePlayer.getDistanceSq(pos)>30*30) continue;
                    NBTTagCompound entityData = new NBTTagCompound();
                    skull.writeToNBT(entityData);
                    Boolean witherSkull = entityData.toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2JjYmJmOTRkNjAzNzQzYTFlNzE0NzAyNmUxYzEyNDBiZDk4ZmU4N2NjNGVmMDRkY2FiNTFhMzFjMzA5MTRmZCJ9fX0");
                    Boolean bloodSkull = entityData.toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5ZDgwYjc5NDQyY2YxYTNhZmVhYTIzN2JkNmFkYWFhY2FiMGMyODgzMGZiMzZiNTcwNGNmNGQ5ZjU5MzdjNCJ9fX0");
                    if(!witherSkull && !bloodSkull) continue;
                    
                    if(mc.theWorld.getBlockState(pos.add(4, 0, 4)).getBlock() instanceof BlockSkull) {
                        Color c = witherSkull?Color.black:new Color(255,65,65);
                        GlStateManager.disableDepth();
                        AxisAlignedBB aabb = new AxisAlignedBB(pos.getX()+1, pos.getY()-1, pos.getZ()+1, pos.getX()+1+3, pos.getY()-1+4, pos.getZ()+1+3);
                        RenderUtil.drawOutlinedFilledBoundingBox(aabb, c, event.partialTicks);
                        GlStateManager.enableDepth();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.ReceiveEvent event) {
        if(event.packet instanceof S29PacketSoundEffect && bloodguy!=null && Utils.inDungeons && SkyblockFeatures.config.stopBloodMusic) {
            S29PacketSoundEffect packet = (S29PacketSoundEffect) event.packet;
            if(packet.getSoundName().contains("note")) event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onWorldChanges(WorldEvent.Load event) {
        count = 0;
        bloodguy = null;
        blessings.clear();
        livid = null;
    }

    String delimiter = EnumChatFormatting.AQUA + EnumChatFormatting.STRIKETHROUGH.toString() + EnumChatFormatting.BOLD + "--------------------------------------";
    int count = 0;
    public static EntityPlayer bloodguy;
    static Map<String,Integer> blessings = new HashMap<String,Integer>();


    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChatMesaage(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || event.type == 2) return;
        String text = event.message.getUnformattedText();
        if(text.endsWith("has obtained Blood Key!")) {
            for (Map.Entry<EntityPlayer,String> entry : Nametags.players.entrySet()) {
                if(text.contains(entry.getKey().getName())) {
                    bloodguy = entry.getKey();
                }
            }
            if(bloodguy==null) {
                bloodguy = Utils.GetMC().thePlayer;
            }
        }

        if(text.contains("Granted you ") && text.contains("and") && SkyblockFeatures.config.blessingViewer) {
            int stat1 = 0;
            try {
                stat1 = Integer.parseInt(text.split(" ")[2]);
            } catch (Exception e) {
                // TODO: handle exception
            }
            String stat1Type = text.split(" ")[3];
            if(blessings.get(stat1Type) == null) blessings.put(stat1Type, stat1);
            else {
                blessings.replace(stat1Type, blessings.get(stat1Type), blessings.get(stat1Type)+stat1);
            }
            int stat2 = 0;
            try {
                stat2 = Integer.parseInt(text.split(" ")[6]);
            } catch (Exception e) {
                // TODO: handle exception
            }
            String stat2Type = text.split(" ")[7];
            if(blessings.get(stat2Type) == null) blessings.put(stat2Type, stat2);
            else {
                blessings.replace(stat2Type, blessings.get(stat2Type), blessings.get(stat2Type)+stat2);
            }
        }

        if(!SkyblockFeatures.config.quickStart) return;
        
        if (text.contains("§6> §e§lEXTRA STATS §6<")) {
            count=1;
        }
        if (text.equals("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
            if(count == 1) {
                ChatComponentText message = new ChatComponentText(EnumChatFormatting.AQUA+"[SBF] "+EnumChatFormatting.GOLD + "Dungeon finished! ");
                ChatComponentText warpout = new ChatComponentText(EnumChatFormatting.GREEN+""+EnumChatFormatting.BOLD + " [WARP-OUT]  ");
                ChatComponentText frag = new ChatComponentText(EnumChatFormatting.GREEN+""+EnumChatFormatting.BOLD + "[REPARTY]");
    
                frag.setChatStyle(frag.getChatStyle()
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rp"))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN+"Reparty Group"))));
    
                warpout.setChatStyle(warpout.getChatStyle()
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp dungeon_hub"))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN+"Warp out of the dungeon"))));
    
                Utils.GetMC().thePlayer.addChatMessage(new ChatComponentText(ChatFormatting.GREEN+"▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    
                Utils.GetMC().thePlayer.addChatMessage(
                    new ChatComponentText(delimiter)
                    .appendText("\n")
                    .appendSibling(message)
                    .appendSibling(warpout)
                    .appendSibling(frag)
                    .appendText("\n")
                    .appendSibling(new ChatComponentText(delimiter))
                );
    
                count = 0;
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onDrawSlots(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if (!Utils.inSkyblock) return;
        if(!event.slot.getHasStack()) return;
        ItemStack stack = event.slot.getStack();
        int x = event.slot.xDisplayPosition;
        int y = event.slot.yDisplayPosition;
        String n = ItemUtils.getSkyBlockItemID(stack);
        String i = Utils.cleanColor(stack.getDisplayName());

        if(SkyblockFeatures.config.highlightTrash && n != null) {
            if(n.equals("CRYPT_DREADLORD_SWORD")||n.equals("MACHINE_GUN_BOW")||i.contains("Healing VIII")||n.equals("DUNGEON_LORE_PAPER")||n.equals("ENCHANTED_BONE")||n.equals("CRYPT_BOW")||n.contains("ZOMBIE_SOLDIER")||n.contains("SKELETON_SOLDIER")||n.contains("SKELETON_MASTER")||n.contains("SUPER_HEAVY")||n.contains("INFLATABLE_JERRY")||n.contains("DUNGEON_TRAP")||n.contains("SKELETOR")||n.contains("PREMIUM_FLESH")||n.contains("TRAINING")||n.contains("CONJURING_SWORD")||n.contains("FEL_PEARL")||n.contains("ZOMBIE_KNIGHT")||n.contains("ENCHANTED_ROTTEN_FLESH")) {
                Gui.drawRect(x, y, x + 16, y + 1, new Color(255, 0, 0, 255).getRGB());
                Gui.drawRect(x, y, x + 1, y + 16, new Color(255, 0, 0, 255).getRGB());
                Gui.drawRect(x+15, y, x+16, y + 16, new Color(255, 0, 0, 255).getRGB());
                Gui.drawRect(x, y+15, x + 16, y + 16, new Color(255, 85, 0, 255).getRGB());
            }
        }
    }

    static {
        new BlessingViewer();
    }
    
    public static class BlessingViewer extends UIElement {
  
        public BlessingViewer() {
            super("Blessings Viewer", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }
  
        @Override
        public void drawElement() {
            if(Utils.inDungeons && getToggled()) {
                int i = 0;
                GuiPlayerTabOverlay tabList = Minecraft.getMinecraft().ingameGUI.getTabList();
                String footer = tabList.footer.getFormattedText();
                GuiUtils.drawText("§d§lBlessings",0,0);
                i++;
                for (String line : new ArrayList<>(Arrays.asList(footer.split("\n")))) {
                    if(line.contains("Blessing")) {
                        GuiUtils.drawText("§d"+Utils.cleanColor(line), 0, i * Utils.GetMC().fontRendererObj.FONT_HEIGHT);
                        i++;
                    }
                }
            }
        }
  
        @Override
        public void drawElementExample() {
            GuiUtils.drawText("§d§lBlessings",0,0);
            GuiUtils.drawText("§dBlessing of Power XI", 0, 1 * Utils.GetMC().fontRendererObj.FONT_HEIGHT);
            GuiUtils.drawText("§dBlessing of Life XIII", 0, 2 * Utils.GetMC().fontRendererObj.FONT_HEIGHT);
            GuiUtils.drawText("§dBlessing of Wisdom V", 0, 3 * Utils.GetMC().fontRendererObj.FONT_HEIGHT);
            GuiUtils.drawText("§dBlessing of Stone VII", 0, 4 * Utils.GetMC().fontRendererObj.FONT_HEIGHT);
        }
  
        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.blessingViewer && Utils.inDungeons;
        }
  
        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*5;
        }
  
        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("§dBlessing of Life XIII")+12;
        }
    }

    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent keyboardInputEvent) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!SkyblockFeatures.config.quickCloseChest || !Utils.inDungeons) return;

        if (screen instanceof GuiChest){
            ContainerChest ch = (ContainerChest) ((GuiChest)screen).inventorySlots;
            if (!("Large Chest".equals(ch.getLowerChestInventory().getName()) || "Chest".equals(ch.getLowerChestInventory().getName()))) return;

            Minecraft.getMinecraft().thePlayer.closeScreen();
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!SkyblockFeatures.config.quickCloseChest || !Utils.inDungeons) return;
        if (Mouse.getEventButton() == -1) return;

        if (screen instanceof GuiChest){
            ContainerChest ch = (ContainerChest) ((GuiChest)screen).inventorySlots;
            if (!("Large Chest".equals(ch.getLowerChestInventory().getName()) || "Chest".equals(ch.getLowerChestInventory().getName()))) return;

            Minecraft.getMinecraft().thePlayer.closeScreen();
        }
    }
}
