package mrfast.sbf.features.render;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderFeatures {
    @SubscribeEvent
    public void onRender3d(RenderWorldLastEvent event) {
        if (Utils.GetMC().theWorld == null) return;

        if (SkyblockFeatures.config.advancedDragonHitbox) {
            for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
                if (entity instanceof EntityDragon) {
                    EntityDragon dragon = (EntityDragon) entity;
                    for (EntityDragonPart entityDragonPart : dragon.dragonPartArray) {
                        AxisAlignedBB aabb = new AxisAlignedBB(entityDragonPart.posX, entityDragonPart.posY, entityDragonPart.posZ, entityDragonPart.posX + entityDragonPart.width, entityDragonPart.posY + entityDragonPart.height, entityDragonPart.posZ + entityDragonPart.width);
                        RenderUtil.drawOutlinedFilledBoundingBox(aabb, SkyblockFeatures.config.advancedDragonHitboxColor, event.partialTicks);
                    }
                }
            }
        }
    }
}
