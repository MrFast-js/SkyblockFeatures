package mrfast.sbf.features.termPractice

import com.mojang.realmsclient.gui.ChatFormatting
import mrfast.sbf.events.GuiContainerEvent.CloseWindowEvent
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent
import mrfast.sbf.events.SlotClickedEvent
import mrfast.sbf.utils.ItemUtils
import mrfast.sbf.utils.Utils
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

class TerminalManager {
    private fun handleTerminalClick(event: SlotClickedEvent) {
        if (event.chestName.contains(ChatFormatting.GOLD.toString() + "✯")) {
            if (event.chestName.contains("Correct Panes")) {
                CorrectPanes().slotClick(event)
            }
            if (event.chestName.contains("Click in order")) {
                ClickInOrder().slotClick(event)
            }
            if (event.chestName.contains("Change all to same color")) {
                ColorPanes().slotClick(event)
            }
            if (event.chestName.contains("What starts with")) {
                StartsWith().slotClick(event)
            }
        }
        if (event.chestName.contains("✯")) event.setCanceled(true)
    }

    private abstract class Terminal {
        var name = ""
        fun completed() {
            Utils.playSound("random.levelup", 1.0)
            Utils.sendMessage(ChatFormatting.GREEN.toString() + "You completed " + name + " in " + Utils.round((System.currentTimeMillis() - start) / 1000.0, 2) + "s")
            Utils.GetMC().thePlayer.closeScreen()
        }

        fun failed() {
            Utils.sendMessage(ChatFormatting.RED.toString() + "You failed " + name)
            Utils.GetMC().thePlayer.closeScreen()
        }
    }

    private class ClickInOrder : Terminal() {
        init {
            name = "Click in order"
        }

        fun open(): InventoryBasic {
            orderNumber = 1.0
            val terminal = InventoryBasic(ChatFormatting.GOLD.toString() + "✯ Click in order", true, 36)
            for (i in 0..35) {
                terminal.setInventorySlotContents(i, ItemUtils.menuGlass)
            }
            val numbers: List<Int> = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
            Collections.shuffle(numbers)
            var index = 0

            // Top Row
            for (i in 10..16) {
                val randNum = numbers[index]
                terminal.setInventorySlotContents(i, ItemStack(Blocks.stained_glass_pane, randNum, 14).setStackDisplayName(ChatFormatting.RESET.toString() + ""))
                index++
            }
            // Bottom Row
            for (i in 19..25) {
                val randNum = numbers[index]
                terminal.setInventorySlotContents(i, ItemStack(Blocks.stained_glass_pane, randNum, 14).setStackDisplayName(ChatFormatting.RESET.toString() + ""))
                index++
            }
            return terminal
        }

        fun slotClick(event: SlotClickedEvent) {
            if (event.item.unlocalizedName.contains("red")) {
                if (event.item.stackSize.toDouble() == orderNumber) {
                    if (orderNumber == 14.0) {
                        completed()
                        orderNumber = 1.0
                    }
                    event.inventory.setInventorySlotContents(event.slot.slotNumber, ItemStack(Blocks.stained_glass_pane, event.item.stackSize, 5).setStackDisplayName(ChatFormatting.RESET.toString() + ""))
                    Utils.playSound("note.pling", 2.0)
                    orderNumber++
                    if (start == 0L) {
                        start = System.currentTimeMillis()
                    }
                } else {
                    failed()
                }
            }
        }
    }

    private class ColorPanes : Terminal() {
        init {
            name = "Change all to same color"
        }

        fun open(): InventoryBasic {
            val terminal = InventoryBasic(ChatFormatting.GOLD.toString() + "✯ Change all to same color", true, 45)
            clicked.clear()
            for (i in 0..44) {
                terminal.setInventorySlotContents(i, ItemUtils.menuGlass)
            }
            for (slot in color3x3Slots) {
                // green -> blue -> red -> orange -> yellow
                val colors: List<Int> = mutableListOf(5, 3, 14, 1, 4)
                val selectedColor = colors[Utils.randomNumber(0, colors.size - 1)]
                terminal.setInventorySlotContents(slot, ItemStack(Blocks.stained_glass_pane, 1, selectedColor).setStackDisplayName(ChatFormatting.RESET.toString() + ""))
            }
            return terminal
        }

        fun slotClick(event: SlotClickedEvent) {
            // green -> blue -> red -> orange -> yellow
            val colors: List<Int> = mutableListOf(5, 3, 14, 1, 4)
            if (event.item == null) return
            val color = event.item.metadata
            if (!colors.contains(color)) return
            Utils.playSound("note.pling", 2.0)
            if (start == 0L) {
                start = System.currentTimeMillis()
            }
            var nextIndex = colors.indexOf(color) + 1
            if (nextIndex > colors.size - 1) {
                nextIndex = 0
            }
            val nextColor = colors[nextIndex]
            val stack = ItemStack(Blocks.stained_glass_pane, 1, nextColor)
                    .setStackDisplayName(ChatFormatting.RESET.toString() + "")
            event.inventory.setInventorySlotContents(event.slot.slotNumber, stack)
            val colorSet: MutableList<Int> = ArrayList()
            for (color3x3Slot in color3x3Slots) {
                colorSet.add(event.inventory.getStackInSlot(color3x3Slot).metadata)
            }
            var completeTerminal = true
            val first = colorSet[0]
            for (slot in colorSet) {
                if (first != slot) {
                    completeTerminal = false
                    break
                }
            }
            if (completeTerminal) {
                completed()
            }
        }
    }

    private class StartsWith : Terminal() {
        init {
            name = "What starts with"
        }

        fun open(): InventoryBasic {
            val allowedItems: List<Int> = mutableListOf(334, 44, 275, 301, 299, 298, 300, 147, 155, 327, 21, 153, 73, 16, 56, 14, 279, 293, 276, 3, 23, 175, 61, 20, 41, 266, 429, 65, 84, 257, 256, 69, 91, 17, 162, 378, 103, 335, 5, 392, 319, 86, 168, 411, 414, 66, 38, 40, 331, 6, 397, 332, 427, 280, 29, 274, 273, 272, 287, 50, 106, 326, 295, 111, 126, 35, 268)
            val itemsAdded: MutableList<Int> = ArrayList()
            for (paneSlot in itemSlots) {
                var itemId = allowedItems[Utils.randomNumber(0, allowedItems.size - 1)]
                while (itemsAdded.contains(itemId)) {
                    itemId = allowedItems[Utils.randomNumber(0, allowedItems.size - 1)]
                }
                itemsAdded.add(itemId)
            }
            val letterRep = HashMap<String, Int>()
            for (i in itemsAdded) {
                val item = Item.getItemById(i)
                val stack = ItemStack(item)
                val startingLetter = stack.displayName.substring(0, 1)
                letterRep.putIfAbsent(startingLetter, 0)
                letterRep[startingLetter] = letterRep[startingLetter]!! + 1
            }
            var mostRepeatedLetter: String? = null
            var maxCount = 0
            for ((key, value) in letterRep) {
                if (value > maxCount) {
                    maxCount = value
                    mostRepeatedLetter = key
                }
            }
            name = "What starts with '$mostRepeatedLetter'"
            val terminal = InventoryBasic(ChatFormatting.GOLD.toString() + "✯ What starts with '" + mostRepeatedLetter + "'", true, 54)
            for (i in 0 until terminal.sizeInventory) {
                terminal.setInventorySlotContents(i, ItemUtils.menuGlass)
            }
            for (i in itemsAdded.indices) {
                val slotId = itemSlots[i]
                val item = Item.getItemById(itemsAdded[i])
                val stack = ItemStack(item)
                terminal.setInventorySlotContents(slotId, stack)
            }
            return terminal
        }

        fun slotClick(event: SlotClickedEvent) {
            if (event.item.item === Item.getItemFromBlock(Blocks.stained_glass_pane)) return
            if (start == 0L) {
                start = System.currentTimeMillis()
            }
            val startingChar = event.chestName.split("'".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            if (event.item.displayName.startsWith(startingChar)) {
                if (!event.item.isItemEnchanted) {
                    event.item.addEnchantment(Enchantment.efficiency, 1)
                    Utils.playSound("note.pling", 2.0)
                }
            } else {
                failed()
            }
            var possible = 0
            var actual = 0
            for (i in 0..53) {
                val stack = event.inventory.getStackInSlot(i)
                if (stack.displayName.startsWith(startingChar)) {
                    possible++
                }
                if (stack.isItemEnchanted && stack.displayName.startsWith(startingChar)) {
                    actual++
                }
            }
            if (possible == actual) {
                completed()
            }
        }
    }

    private class CorrectPanes : Terminal() {
        fun open(): InventoryBasic {
            val terminal: InventoryBasic
            terminal = InventoryBasic(ChatFormatting.GOLD.toString() + "✯ Correct Panes", true, 45)
            clicked.clear()
            for (i in 0..44) {
                terminal.setInventorySlotContents(i, ItemUtils.menuGlass)
            }
            val startingSlot = Random().nextInt(paneSlots.size)
            terminal.setInventorySlotContents(paneSlots[startingSlot], ItemStack(Blocks.stained_glass_pane, 1, 5).setStackDisplayName(ChatFormatting.RESET.toString() + ""))
            for (slot in paneSlots) {
                if (paneSlots[startingSlot] == slot) continue
                terminal.setInventorySlotContents(slot, ItemStack(Blocks.stained_glass_pane, 1, 14).setStackDisplayName(ChatFormatting.RESET.toString() + ""))
            }
            return terminal
        }

        fun slotClick(event: SlotClickedEvent) {
            for (slot in paneSlots) {
                if (event.slot == null) continue
                if (event.slot.slotNumber == slot) {
                    if (event.item.unlocalizedName.contains("red")) {
                        Utils.playSound("note.pling", 2.0)
                        clicked.add(event.slot.slotNumber)
                        event.inventory.setInventorySlotContents(event.slot.slotNumber, ItemStack(Blocks.stained_glass_pane, 1, 5).setStackDisplayName(ChatFormatting.RESET.toString() + ""))
                        if (clicked.size == 14) {
                            Utils.sendMessage(ChatFormatting.GREEN.toString() + "You completed 'Correct all the panes!' in " + Utils.round((System.currentTimeMillis() - start) / 1000.0, 2) + "s")
                            Utils.GetMC().thePlayer.closeScreen()
                        }
                        if (start == 0L) {
                            start = System.currentTimeMillis()
                        }
                    }
                    if (event.item.unlocalizedName.contains("lime") && clicked.contains(event.slot.slotNumber)) {
                        Utils.playSound("note.pling", 2.0)
                        clicked.remove(event.slot.slotNumber)
                        event.inventory.setInventorySlotContents(event.slot.slotNumber, ItemStack(Blocks.stained_glass_pane, 1, 14).setStackDisplayName(ChatFormatting.RESET.toString() + ""))
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onTitleDrawn(event: TitleDrawnEvent) {
        if (event.gui !is GuiChest) return
        val gui = event.gui as GuiChest
        val chest = gui.inventorySlots as ContainerChest
        val inv = chest.lowerChestInventory
        var chestName = inv.displayName.unformattedText.trim { it <= ' ' }
        if (chestName.contains("✯")) {
            if (start != 0L) {
                chestName += " " + Utils.round((System.currentTimeMillis() - start) / 1000.0, 2) + "s"
            }
            Utils.GetMC().fontRendererObj.drawStringWithShadow(chestName, 8f, 6f, 0)
        }
    }

    @SubscribeEvent
    fun onGuiClose(event: CloseWindowEvent?) {
        start = 0
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickedEvent) {
        if (event.chestName.contains("✯")) {
            if (event.slot != null) handleTerminalClick(event)
        }
    }

    companion object {
        var clicked: MutableList<Int> = ArrayList()
        var start: Long = 0
        var paneSlots = intArrayOf(
                11, 12, 13, 14, 15,
                20, 21, 22, 23, 24,
                29, 30, 31, 32, 33
        )
        var itemSlots = intArrayOf(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        )
        var color3x3Slots = intArrayOf(
                12, 13, 14,
                21, 22, 23,
                30, 31, 32
        )
        var orderNumber = 1.0
        fun createTerminal(terminalId: Int): GuiChest? {
            var terminal: InventoryBasic? = null
            start = 0
            when (terminalId) {
                1 -> terminal = ColorPanes().open()
                2 -> terminal = CorrectPanes().open()
                3 -> terminal = ClickInOrder().open()
                4 -> terminal = StartsWith().open()
            }
            return if (terminal != null) {
                GuiChest(Utils.GetMC().thePlayer.inventory, terminal)
            } else null
        }
    }
}
