package mrfast.sbf.gui.components;

import java.awt.Color;

import com.mojang.realmsclient.gui.ChatFormatting;

import gg.essential.elementa.UIComponent;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.state.BasicState;
import gg.essential.elementa.state.State;
import gg.essential.universal.UMatrixStack;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class InventoryComponent extends UIComponent {
    public InventoryComponent(InventoryBasic stack,String inventoryName) {
        this(new BasicState<>(stack),new BasicState<>(inventoryName));
    }

    public InventoryComponent(State<InventoryBasic> state, State<String> invName) {
        InventoryBasic inventory = state.get();
        int verticalSlots = (int) Math.floor(inventory.getSizeInventory()/9);
        int horizontalSlots = 9;

        if(getWidth()==0) setWidth(new PixelConstraint(horizontalSlots*17f));
        if(getHeight()==0) setHeight(new PixelConstraint(verticalSlots*17f));

        new UIText(ChatFormatting.YELLOW+invName.get()).setX(new CenterConstraint()).setChildOf(this);
        boolean stopped = false;
        for(int y=0;y<verticalSlots;y++) {
            if(stopped) break;
            for(int x=0;x<horizontalSlots;x++) {
                ItemStack item = inventory.getStackInSlot((y*9)+x);
                int topMargin = (int) Math.floor(y/5)*4;
                if(item!=null && item.getUnlocalizedName().equals("tile.barrier")) {
                    this.setHeight(new PixelConstraint(18f*y));
                    stopped = true;
                    break;
                }

                UIComponent backgroundSlot = new UIRoundedRectangle(3f)
                    .setHeight(new PixelConstraint(16f))
                    .setWidth(new PixelConstraint(16f))
                    .setX(new PixelConstraint(x*17f))
                    .setY(new PixelConstraint(y*17f+10+topMargin))
                    .setColor(new Color(100,100,100,200));
                
                new ItemStackComponent(item)
                    .setChildOf(backgroundSlot)
                    .setX(new CenterConstraint())
                    .setY(new CenterConstraint());

                this.addChild(backgroundSlot);
            }
        }
    }
    UIComponent hovered = null;

    @Override
    public void draw(UMatrixStack matrixStack) {
        beforeDraw(matrixStack);
        super.draw(matrixStack);
    }
}