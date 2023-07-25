package mrfast.skyblockfeatures.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class Point {

    private static ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
    public float x = 0;
    public float y = 0;

    public Point(float x, float y) {
        this(x, y, false);
    }

    public Point(float x, float y,boolean raw) {
        if(!raw) {
            this.x = x*scaledResolution.getScaledWidth();
            this.y = y*scaledResolution.getScaledHeight();
        } else {
            this.x = x;
            this.y = y;
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
