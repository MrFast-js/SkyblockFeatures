package mrfast.sbf.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class Point {

    public float x;
    public float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
