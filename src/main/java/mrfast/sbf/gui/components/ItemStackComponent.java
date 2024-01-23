package mrfast.sbf.gui.components;

import gg.essential.elementa.UIComponent;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.state.BasicState;
import gg.essential.elementa.state.State;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerGui;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.item.ItemStack;

public class ItemStackComponent extends UIComponent {
    private final State<ItemStack> state;

    public ItemStackComponent(ItemStack stack) {
        this(new BasicState<>(stack));
    }

    public ItemStackComponent(State<ItemStack> state) {
        this.state = state;
    }

    @Override
    public void draw(UMatrixStack matrixStack) {
        ItemStack item = this.state.get();
        if (item == null) return;

        beforeDraw(matrixStack);
        super.draw(matrixStack);
        int screenHeight = Utils.GetMC().currentScreen.height;
        float fontScale = (float) (screenHeight / 540d);
        if (getWidth() == 0) setWidth(new PixelConstraint(16f*fontScale));
        if (getHeight() == 0) setHeight(new PixelConstraint(16f*fontScale));

        matrixStack.push();

        matrixStack.translate(getLeft(), getTop(), 100f);
        matrixStack.scale(getWidth() / (16f*fontScale), getHeight() / (16f*fontScale), 1f);
        UGraphics.color4f(1f, 1f, 1f, 1f);
        matrixStack.runWithGlobalState(() -> {
            RenderUtil.renderItemStackOnScreen(item, 0, 0, (16f*fontScale), (16f*fontScale));
        });
        matrixStack.pop();
        UGraphics.disableLighting();


        if (!item.hasDisplayName()) return;
        if (item.getDisplayName().trim().isEmpty()) return;

        if (this.isHovered()) {
            ProfileViewerGui.renderTooltip = item.getTooltip(Utils.GetMC().thePlayer, false);
        }
    }


}