package mrfast.skyblockfeatures.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mrfast.skyblockfeatures.events.GuiContainerEvent;
import mrfast.skyblockfeatures.events.SlotClickedEvent;
import mrfast.skyblockfeatures.utils.Utils;
/**
 * Original code was taken from Skytils under GNU Affero General Public License v3.0 and modified by MrFast
 *
 * @author Skytils Team
 * @link https://github.com/Skytils/SkytilsMod/blob/1.x/LICENSE.md
 */
@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen {

    @Shadow public Container inventorySlots;

    private final GuiContainer that = (GuiContainer) (Object) this;

    @Inject(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V", shift = At.Shift.BEFORE))
    private void closeWindowPressed(CallbackInfo ci) {
        try {
            MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.CloseWindowEvent(that, inventorySlots));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V", ordinal = 0, shift = At.Shift.AFTER))
    private void titlePostDrawn(int mouseX, int mouseY, float partialTicks,CallbackInfo ci) {
        try {
            if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
            GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
            ContainerChest cont = (ContainerChest) chest.inventorySlots;
            String name = cont.getLowerChestInventory().getName();
            
            MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.TitleDrawnEvent(that, inventorySlots, mouseX, mouseY, partialTicks,name));
        } catch (Throwable e) {
            // e.printStackTrace();
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(Slot slot, CallbackInfo ci) {
        try {
            if (MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawSlotEvent.Pre(that, inventorySlots, slot))) ci.cancel();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Inject(
        method = "handleMouseClick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSlotClick(Slot slotIn, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        GuiContainer container = (GuiContainer) (Object) this;
        Slot slot = slotId < 0 ? null : container.inventorySlots.getSlot(slotId);
        SlotClickedEvent event = new SlotClickedEvent(container, slot, slotId);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlot", at = @At("RETURN"), cancellable = true)
    private void onDrawSlotPost(Slot slot, CallbackInfo ci) {
        try {
            MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawSlotEvent.Post(that, inventorySlots, slot));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}