package mrfast.sbf.events;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class GuiContainerEvent extends Event {

    public GuiContainer gui;
    public Container container;

    public GuiContainerEvent(GuiContainer gui, Container container) {
        this.gui = gui;
        this.container = container;
    }

    public static class TitleDrawnEvent extends GuiContainerEvent {
        public int mouseX, mouseY;
        public float partialTicks;
        public String displayName;

        public TitleDrawnEvent(GuiContainer gui, Container container, int mouseX, int mouseY, float partialTicks,String displayName) {
            super(gui, container);
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.displayName = displayName;
            this.partialTicks = partialTicks;
        }
    }

    public static class CloseWindowEvent extends GuiContainerEvent {
        public CloseWindowEvent(GuiContainer gui, Container container) {
            super(gui, container);
        }
    }

    public static class DrawSlotEvent extends GuiContainerEvent {

        public Slot slot;

        public DrawSlotEvent(GuiContainer gui, Container container, Slot slot) {
            super(gui, container);
            this.slot = slot;
        }

        @Cancelable
        public static class Pre extends DrawSlotEvent {
            public Pre(GuiContainer gui, Container container, Slot slot) {
                super(gui, container, slot);
            }
        }

        public static class Post extends DrawSlotEvent {
            public Post(GuiContainer gui, Container container, Slot slot) {
                super(gui, container, slot);
            }
        }
    }
}
