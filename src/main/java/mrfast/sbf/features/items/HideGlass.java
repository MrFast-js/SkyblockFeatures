package mrfast.sbf.features.items;

import java.text.SimpleDateFormat;
import java.util.Date;

import mrfast.sbf.utils.GuiUtils;
import org.lwjgl.input.Keyboard;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.commands.TerminalCommand;
import mrfast.sbf.commands.getNbtCommand;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HideGlass {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static boolean on = true;

    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {
        if(!SkyblockFeatures.config.timestamps || event.type == 2) return;
        String timestamp = new SimpleDateFormat("hh:mm").format(new Date());
        event.message = new ChatComponentText("")
                .appendText(ChatFormatting.DARK_GRAY+"["+timestamp+"] ")
                .appendSibling(event.message);
    }

    @SubscribeEvent
    public void onTooltipLow(ItemTooltipEvent event) {
        if(Utils.inSkyblock && SkyblockFeatures.config.showSkyblockID) {
            for(int i = 0; i < event.toolTip.size(); i++) {
                String line = Utils.cleanColor(event.toolTip.get(i));
                if(line.contains("minecraft:")) {
                    event.toolTip.add(i+1,ChatFormatting.DARK_GRAY+"ID: "+ItemUtils.getSkyBlockItemID(event.itemStack));
                    if(Utils.isDeveloper()) {
                        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)&&Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            NBTTagCompound tag = event.itemStack.getTagCompound();
                            if(tag!=null) {
                                event.toolTip.add(i+1,ChatFormatting.DARK_GRAY+"DATA: "+getNbtCommand.prettyPrintNBT(tag));
                            }
                        } else if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            NBTTagCompound tag = ItemUtils.getExtraAttributes(event.itemStack);
                            if(tag!=null) {
                                event.toolTip.add(i+1,ChatFormatting.DARK_GRAY+"DATA: "+getNbtCommand.prettyPrintNBT(tag));
                            }
                        }
                    }
                    break;
                }
            }
        }

        if(Utils.inSkyblock && isEmptyGlassPane(event.itemStack)) {
            event.toolTip.clear();
        }
    }

    public static boolean isEmptyGlassPane(ItemStack itemStack) {
        return itemStack != null && (itemStack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)
                || itemStack.getItem() == Item.getItemFromBlock(Blocks.glass_pane)) && itemStack.hasDisplayName() && Utils.cleanColor(itemStack.getDisplayName().trim()).isEmpty();
    }

    @SubscribeEvent
    public void onGuiClose(GuiContainerEvent.CloseWindowEvent event) {
        TerminalCommand.start = 0;
        TerminalCommand.mazeIndex = 1;
    }
    
    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if(Utils.isDeveloper()) {
            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)&&Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                event.setCanceled(true);
                System.out.println("-====================================-");
                for (String itemLore : ItemUtils.getItemLore(event.slot.getStack())) {
                    System.out.println(itemLore);
                }
                System.out.println("-====================================-");
            }
        }
        if(event.inventoryName.contains(ChatFormatting.GREEN+"✯")) {
            if(event.inventoryName.contains("Correct Panes")) {
                for(int slot : TerminalCommand.paneSlots) {
                    if(event.slot==null) continue;
                    if(event.slot.slotNumber == slot) {
                        if(event.item.getUnlocalizedName().contains("red")) {
                            Utils.playSound("note.pling", 2);
                            TerminalCommand.clicked.add(event.slot.slotNumber);
                            event.inventory.setInventorySlotContents(event.slot.slotNumber, new ItemStack(Blocks.stained_glass_pane, 1, 5).setStackDisplayName(ChatFormatting.RESET+""));
                            if(TerminalCommand.clicked.size() == 14) {
                                Utils.SendMessage(ChatFormatting.GREEN+"You completed 'Correct all the panes!' in "+Utils.round(System.currentTimeMillis()-TerminalCommand.start,2)+"s");
                                mc.thePlayer.closeScreen();
                            }
                            if(TerminalCommand.start == 0) {
                                TerminalCommand.start = System.currentTimeMillis();
                            }
                        }
                        if(event.item.getUnlocalizedName().contains("lime") && TerminalCommand.clicked.contains(event.slot.slotNumber)) {
                            Utils.playSound("note.pling", 2);
                            TerminalCommand.clicked.remove((Integer) event.slot.slotNumber);
                            event.inventory.setInventorySlotContents(event.slot.slotNumber, new ItemStack(Blocks.stained_glass_pane, 1, 14).setStackDisplayName(ChatFormatting.RESET+""));
                        }
                    }
                }
            }
            if(event.inventoryName.contains("Maze")) {
                assert event.slot != null;
                if(event.slot.slotNumber == TerminalCommand.mazeSlots[TerminalCommand.mazeSlots.length-(int) TerminalCommand.mazeIndex]) {
                    if(event.item.getUnlocalizedName().contains("white")) {
                        Utils.playSound("note.pling", 2);
                        TerminalCommand.clicked.add(event.slot.slotNumber);
                        event.inventory.setInventorySlotContents(event.slot.slotNumber, new ItemStack(Blocks.stained_glass_pane, 1, 5).setStackDisplayName(ChatFormatting.RESET+""));
                        if(TerminalCommand.clicked.size() == TerminalCommand.mazeSlots.length) {
                            Utils.SendMessage(ChatFormatting.GREEN+"You completed 'Maze!' in "+Utils.round(System.currentTimeMillis()-TerminalCommand.start,2)+"s");
                            mc.thePlayer.closeScreen();
                            TerminalCommand.mazeIndex = 0;
                        }
                        if(TerminalCommand.start == 0) {
                            TerminalCommand.start = System.currentTimeMillis();
                        }
                    
                        TerminalCommand.mazeIndex++;
                    }
                }
            }
            if(event.inventoryName.contains("Click in order") && event.item.getUnlocalizedName().contains("red")) {
                if(event.item.stackSize==TerminalCommand.orderNumber) {
                    if(TerminalCommand.orderNumber==14) {
                        Utils.SendMessage(ChatFormatting.GREEN+"You completed 'Click in order!' in "+Utils.round(System.currentTimeMillis()-TerminalCommand.start,2)+"s");
                        mc.thePlayer.closeScreen();
                        TerminalCommand.orderNumber = 1;
                    }
				    event.inventory.setInventorySlotContents(event.slot.slotNumber, new ItemStack(Blocks.stained_glass_pane, event.item.stackSize, 5).setStackDisplayName(ChatFormatting.RESET+""));
                    Utils.playSound("note.pling", 2);
                    TerminalCommand.orderNumber++;
                    if(TerminalCommand.start == 0) {
                        TerminalCommand.start = System.currentTimeMillis();
                    }
                } else {
                    mc.thePlayer.closeScreen();
                    Utils.SendMessage(ChatFormatting.RED+"You failed 'Click in order!'");
                }
            }
        }
        try {
            if(event.inventoryName.contains("✯")) event.setCanceled(true);
        } catch (Exception e) {
            //TODO: handle exception
        }
    }

    @SubscribeEvent
    public void onTitleDrawn(TitleDrawnEvent event) {
        if(!(event.gui instanceof GuiChest)) return;
        GuiChest gui = (GuiChest) event.gui;
        ContainerChest chest = (ContainerChest) gui.inventorySlots;
        IInventory inv = chest.getLowerChestInventory();
        String chestName = inv.getDisplayName().getUnformattedText().trim();
        if(chestName.contains("✯") && TerminalCommand.start!=0) {
            Utils.GetMC().fontRendererObj.drawString(chestName+" "+(Utils.round(System.currentTimeMillis() - TerminalCommand.start,2))+"s", 8, 6, 0);
        }
    }

    // Debug Mode to see all the slots ids
    @SubscribeEvent
    public void onDrawSlots(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if (event.gui instanceof GuiChest ) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            if(Utils.isDeveloper()) {
                if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)&&Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    for(int i=0;i<chest.inventorySlots.size();i++) {
                        
                        int x = chest.inventorySlots.get(i).xDisplayPosition;
                        int y = chest.inventorySlots.get(i).yDisplayPosition;
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0, 0, 700);
                        GuiUtils.drawText(ChatFormatting.GREEN+""+i,x+6, y+6, GuiUtils.TextStyle.BLACK_OUTLINE);
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    }
}
