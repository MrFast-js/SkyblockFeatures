package mrfast.sbf.mixins;

import mrfast.sbf.features.misc.PlayerDiguiser;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> {

    @Redirect(method = "renderName", at=@At(value = "INVOKE", target =
            "Lnet/minecraft/entity/EntityLivingBase;getDisplayName()Lnet/minecraft/util/IChatComponent;"))
    public IChatComponent renderName_getDisplayName(EntityLivingBase entity) {
        if(PlayerDiguiser.tracker.get(entity)!=null) return new ChatComponentText("");
        return entity.getDisplayName();
        
    }
    
}