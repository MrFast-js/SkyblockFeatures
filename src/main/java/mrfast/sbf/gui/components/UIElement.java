package mrfast.sbf.gui.components;

import mrfast.sbf.gui.GuiManager;

public abstract class UIElement {
    String name;
    Point pos;

    public UIElement(String name, Point fp) {
        this.name = name;
        this.pos = GuiManager.GuiPositions.getOrDefault(name, fp);
    }

    public abstract void drawElement();

    public abstract void drawElementExample();

    public abstract boolean getToggled();

    public void setPos(Point newPos) {
        this.pos = newPos;
    }

    public Point getPos() {
        return this.pos;
    }

    public void setPos(float x, float y) {
        this.pos = new Point(x, y);
    }

    public String getName() {
        return this.name;
    }

    public float getX() {
        return getPos().getX();
    }

    public float getY() {
        return getPos().getY();
    }

    public abstract int getHeight();

    public abstract int getWidth();
}
