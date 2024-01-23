package mrfast.sbf.features.items

import com.mojang.realmsclient.gui.ChatFormatting
import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.events.GuiContainerEvent.DrawSlotEvent
import mrfast.sbf.events.SlotClickedEvent
import mrfast.sbf.utils.GuiUtils
import mrfast.sbf.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.io.IOException
import javax.imageio.ImageIO

class HideMenuGlass {
    @SubscribeEvent
    fun onTooltipLow(event: ItemTooltipEvent) {
        if (Utils.inSkyblock && isMenuGlassPane(event.itemStack) && SkyblockFeatures.config.hideMenuGlass) {
            event.toolTip.clear()
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickedEvent) {
        if (event.slot != null && Utils.inSkyblock && isMenuGlassPane(event.slot.stack) && SkyblockFeatures.config.hideMenuGlass) {
            event.setCanceled(true)
        }
    }

    var chestBackgroundColor = -1
    @SubscribeEvent
    fun onTextureStitchPre(event: TextureStitchEvent.Pre?) {
        // Set the pixel color when textures are stitched
        chestBackgroundColor = getPixelColor()
    }

    @SubscribeEvent
    fun onDrawSlots(event: DrawSlotEvent.Post) {
        if (event.gui is GuiChest) {
            GlStateManager.translate(0f, 0f, 300f)
            if (isMenuGlassPane(event.slot.stack) && SkyblockFeatures.config.hideMenuGlass) {
                GlStateManager.pushMatrix()
                GL11.glColor4f(1f, 1f, 1f, 1f)
                Gui.drawRect(
                        event.slot.xDisplayPosition - 1,
                        event.slot.yDisplayPosition - 1,
                        event.slot.xDisplayPosition + 16 + 1,
                        event.slot.yDisplayPosition + 16 + 1, chestBackgroundColor)
                GlStateManager.popMatrix()
            }
            GlStateManager.translate(0f, 0f, -300f)
        }
    }

fun getPixelColor(): Int {
            try {
                // Define the path to the chest texture
                val chestTextureLocation = ResourceLocation("minecraft", "textures/gui/container/generic_54.png")

                // Load the texture as an image
                val resource = Utils.GetMC().resourceManager.getResource(chestTextureLocation)
                val chestImage = ImageIO.read(resource.inputStream)
                return chestImage.getRGB(5, 5)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return -1 // Return -1 if an error occurs
        }

    companion object {
        fun isMenuGlassPane(itemStack: ItemStack?): Boolean {
            return itemStack != null &&
                    (itemStack.item === Item.getItemFromBlock(Blocks.stained_glass_pane) || itemStack.item === Item.getItemFromBlock(Blocks.glass_pane)) &&
                    itemStack.hasDisplayName() && itemStack.metadata == 15 && Utils.cleanColor(itemStack.displayName.trim { it <= ' ' }).isEmpty()
        }
    }
}
