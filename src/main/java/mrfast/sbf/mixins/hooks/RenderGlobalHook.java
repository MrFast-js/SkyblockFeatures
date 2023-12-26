package mrfast.sbf.mixins.hooks;

import mrfast.sbf.utils.EntityOutlineRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3i;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adapted from Skyhanni under GNU LGPL v2.1 license
 * @link https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE
 */
public class RenderGlobalHook {

    public Vec3i exactLocation(Entity entity, float partialTicks) {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        return new Vec3i(x, y, z);
    }

    public boolean renderEntitiesOutlines(ICamera camera, float partialTicks) {
        double x = exactLocation(Minecraft.getMinecraft().getRenderViewEntity(), partialTicks).getX();
        double y = exactLocation(Minecraft.getMinecraft().getRenderViewEntity(), partialTicks).getY();
        double z = exactLocation(Minecraft.getMinecraft().getRenderViewEntity(), partialTicks).getZ();

        return EntityOutlineRenderer.renderEntityOutlines(camera, partialTicks, x, y, z);
    }

    public void shouldRenderEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        if (EntityOutlineRenderer.shouldRenderEntityOutlines()) {
            cir.setReturnValue(true);
        }
    }
}
