package mrfast.sbf.features.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.commands.DebugCommand;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class HideGlass {
    public static Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!SkyblockFeatures.config.timestamps || event.type == 2) return;
        String timestamp = new SimpleDateFormat("hh:mm").format(new Date());
        event.message = new ChatComponentText("")
                .appendText(ChatFormatting.DARK_GRAY + "[" + timestamp + "] ")
                .appendSibling(event.message);
    }

    @SubscribeEvent
    public void onTooltipLow(ItemTooltipEvent event) {
        if (Utils.inSkyblock && SkyblockFeatures.config.showSkyblockID) {
            for (int i = 0; i < event.toolTip.size(); i++) {
                String line = Utils.cleanColor(event.toolTip.get(i));
                if (line.contains("minecraft:")) {
                    String skyblockId = ItemUtils.getSkyBlockItemID(event.itemStack);
                    String specialSkyblockId = PricingData.getIdentifier(event.itemStack);

                    if (!Objects.equals(specialSkyblockId, skyblockId)) {
                        event.toolTip.add(i + 1, ChatFormatting.DARK_GRAY + "PID: " + PricingData.getIdentifier(event.itemStack));
                    }
                    event.toolTip.add(i + 1, ChatFormatting.DARK_GRAY + "ID: " + ItemUtils.getSkyBlockItemID(event.itemStack));

                    if (Utils.isDeveloper()) {
                        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            NBTTagCompound tag = event.itemStack.getTagCompound();
                            if (tag != null) {
                                event.toolTip.add(i + 1, ChatFormatting.DARK_GRAY + "DATA: " + DebugCommand.prettyPrintNBT(tag));
                            }
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            NBTTagCompound tag = ItemUtils.getExtraAttributes(event.itemStack);
                            if (tag != null) {
                                event.toolTip.add(i + 1, ChatFormatting.DARK_GRAY + "DATA: " + DebugCommand.prettyPrintNBT(tag));
                            }
                        }
                    }
                    break;
                }
            }
        }

        if (Utils.inSkyblock && isEmptyGlassPane(event.itemStack) && SkyblockFeatures.config.hideMenuGlass) {
            event.toolTip.clear();
        }
    }

    public static boolean isEmptyGlassPane(ItemStack itemStack) {
        return itemStack != null &&
                (itemStack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) || itemStack.getItem() == Item.getItemFromBlock(Blocks.glass_pane)) &&
                itemStack.hasDisplayName() && itemStack.getMetadata() == 15 && Utils.cleanColor(itemStack.getDisplayName().trim()).isEmpty();
    }

    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if (event.slot != null && Utils.inSkyblock && isEmptyGlassPane(event.slot.getStack()) && SkyblockFeatures.config.hideMenuGlass) {
            event.setCanceled(true);
        }

        if (Utils.isDeveloper()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                event.setCanceled(true);
                System.out.println("-====================================-");
                for (String itemLore : ItemUtils.getItemLore(event.slot.getStack())) {
                    System.out.println(itemLore);
                }
                System.out.println("-====================================-");
            }
        }
    }

    public static int getPixelColor() {
        try {
            // Define the path to the chest texture
            ResourceLocation chestTextureLocation = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

            // Load the texture as an image
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(chestTextureLocation);
            BufferedImage chestImage = ImageIO.read(resource.getInputStream());

            return chestImage.getRGB(5, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1; // Return -1 if an error occurs
    }

    int pixelColor = -1;

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        // Reload the pixel color when textures are stitched
        pixelColor = getPixelColor();
    }

    // Debug Mode to see all the slots ids
    @SubscribeEvent
    public void onDrawSlots(GuiContainerEvent.DrawSlotEvent.Post event) {
        if (event.gui instanceof GuiChest) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            GlStateManager.translate(0f, 0f, 300f);
            if (isEmptyGlassPane(event.slot.getStack()) && SkyblockFeatures.config.hideMenuGlass) {
                GlStateManager.pushMatrix();
                GL11.glColor4f(1f, 1f, 1f, 1f);
                Gui.drawRect(
                        event.slot.xDisplayPosition - 1,
                        event.slot.yDisplayPosition - 1,
                        event.slot.xDisplayPosition + 16 + 1,
                        event.slot.yDisplayPosition + 16 + 1, pixelColor);
                GlStateManager.popMatrix();
            }
            if (Utils.isDeveloper()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    for (int i = 0; i < chest.inventorySlots.size(); i++) {
                        int x = chest.inventorySlots.get(i).xDisplayPosition;
                        int y = chest.inventorySlots.get(i).yDisplayPosition;
                        GuiUtils.drawText(ChatFormatting.GREEN + "" + i, x + 6, y + 6, GuiUtils.TextStyle.BLACK_OUTLINE);
                    }
                }
            }
            GlStateManager.translate(0f, 0f, -300f);
        }
    }
}
