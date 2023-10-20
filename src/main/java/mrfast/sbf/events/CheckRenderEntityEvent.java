package mrfast.sbf.events;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Taken from Skytils 0.x under GNU Affero General Public License v3.0
 * https://github.com/Skytils/SkytilsMod/tree/0.x
 *
 * @author Sychic
 */
@Cancelable
public class CheckRenderEntityEvent<T extends Entity> extends Event {

    public T entity;
    public ICamera camera;
    public double camX, camY, camZ;

    public CheckRenderEntityEvent(T entity, ICamera camera, double camX, double camY, double camZ) {
        this.entity = entity;
        this.camera = camera;
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;
    }

}
