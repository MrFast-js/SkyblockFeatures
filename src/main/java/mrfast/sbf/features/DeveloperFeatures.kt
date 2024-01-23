package mrfast.sbf.features

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.realmsclient.gui.ChatFormatting
import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.core.PricingData
import mrfast.sbf.events.GuiContainerEvent
import mrfast.sbf.utils.GuiUtils
import mrfast.sbf.utils.ItemUtils
import mrfast.sbf.utils.Utils
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

class DeveloperFeatures {
    // Draw slot id's making it easier to get specific slots
    @SubscribeEvent
    fun onDrawSlots(event: GuiContainerEvent.DrawSlotEvent.Post) {
        if (event.gui is GuiChest) {
            val gui = event.gui as GuiChest
            val chest = gui.inventorySlots as ContainerChest
            GlStateManager.translate(0f, 0f, 300f)
            if (Utils.isDeveloper()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    for (i in chest.inventorySlots.indices) {
                        val x = chest.inventorySlots[i].xDisplayPosition
                        val y = chest.inventorySlots[i].yDisplayPosition
                        GuiUtils.drawText(ChatFormatting.GREEN.toString() + i, (x + 6).toFloat(), (y + 6).toFloat(), GuiUtils.TextStyle.BLACK_OUTLINE)
                    }
                }
            }
            GlStateManager.translate(0f, 0f, -300f)
        }
    }

    var isCopyingData = false;
    @SubscribeEvent
    fun onTooltipLow(event: ItemTooltipEvent) {
        if (Utils.inSkyblock && SkyblockFeatures.config.showSkyblockID) {
            for (i in 0 until event.toolTip.size) {
                val line = Utils.cleanColor(event.toolTip[i])
                if (line.contains("minecraft:")) {
                    val skyblockId = ItemUtils.getSkyBlockItemID(event.itemStack)
                    val specialSkyblockId = PricingData.getIdentifier(event.itemStack)

                    if (specialSkyblockId != skyblockId) {
                        event.toolTip.add(i + 1, "${ChatFormatting.DARK_GRAY}PID: ${PricingData.getIdentifier(event.itemStack)}")
                    }
                    event.toolTip.add(i + 1, "${ChatFormatting.DARK_GRAY}ID: ${ItemUtils.getSkyBlockItemID(event.itemStack)}")

                    if (Utils.isDeveloper()) {
                        // Copy inventory
                        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_C) && !isCopyingData) {
                            doCopyCooldown()
                            val container = Utils.GetMC().thePlayer.openContainer ?: return
                            val cont = container as ContainerChest
                            val name = cont.lowerChestInventory.displayName.unformattedText.trim()
                            val inventoryContents = buildString {
                                append("Chest Name: $name\n")
                                for (i in 0 until container.inventory.size) {
                                    val stack = container.inventory[i] ?: continue;
                                    append(prettyPrintNBTtoString(stack.serializeNBT()))
                                }
                            }
                            Utils.sendMessage("Copied Inventory Contents!")
                            Utils.copyToClipboard(inventoryContents)
                            continue;
                        }
                        // Copy hovered stack
                        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_C) && !isCopyingData) {
                            doCopyCooldown()
                            Utils.sendMessage("Copied Hovered Stack!")
                            Utils.copyToClipboard(prettyPrintNBTtoString(event.itemStack.serializeNBT()))
                        }
                        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            val tag = ItemUtils.getExtraAttributes(event.itemStack)
                            if (tag != null) {
                                event.toolTip.add(i + 1, "${ChatFormatting.DARK_GRAY}DATA: ${prettyPrintNBTtoString(tag)}")
                            }
                        }
                    }
                    break
                }
            }
        }
    }

    fun doCopyCooldown() {
        isCopyingData = true
        Utils.setTimeout({
            isCopyingData = false;
        },500)
    }


    companion object {
        @JvmStatic
        fun prettyPrintNBTtoString(nbt: NBTTagCompound): String {
            val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
            return gson.toJson(convertNBTtoJSON(nbt))
        }

        private fun convertNBTtoJSON(nbt: NBTTagCompound): JsonObject {
            val jsonObject = JsonObject()
            for (key in nbt.keySet) {
                val tag = nbt.getTag(key)
                val jsonElement = when (tag) {
                    is NBTTagCompound -> convertNBTtoJSON(tag)
                    is NBTTagList -> convertNBTListToJSON(tag)
                    else -> JsonParser().parse(tag.toString())
                }
                jsonObject.add(key, jsonElement)
            }
            return jsonObject
        }

        private fun convertNBTListToJSON(nbtList: NBTTagList): JsonObject {
            val jsonArray = JsonObject()
            for (i in 0 until nbtList.tagCount()) {
                jsonArray.add(i.toString(), JsonParser().parse(prettyPrintNBTtoString(nbtList.getCompoundTagAt(i))))
            }
            return jsonArray
        }
    }
}
