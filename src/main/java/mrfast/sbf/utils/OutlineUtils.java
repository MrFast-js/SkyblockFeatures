package mrfast.sbf.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.util.HashMap;

public class OutlineUtils {
    public static HashMap<Entity,EntityOutline> entityOutlines = new HashMap<>();
    public static class EntityOutline {
        public Color outlineColor;
        public Entity entity;
        public boolean throughWalls;
        public boolean renderNow;
        public int renderCount = 0;

        public EntityOutline(Entity e,Color c,boolean throughWalls) {
            this.outlineColor=c;
            this.entity=e;
            this.throughWalls = throughWalls;
            this.renderNow = true;
        }
    }

    public static void renderOutline(Entity entity, Color outlineColor, boolean throughWalls) {
        entityOutlines.put(entity,new EntityOutline(entity,outlineColor,throughWalls));

        entityOutlines.get(entity).renderNow = true;
        entityOutlines.get(entity).renderCount = 0;
    }

    public static EntityOutline getOutline(Entity entity) {
        return entityOutlines.get(entity);
    }
}
