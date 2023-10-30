package mrfast.sbf.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mrfast.sbf.features.misc.PlayerDiguiser;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> {

    @Redirect(method = "renderName", at=@At(value = "INVOKE", target =
            "Lnet/minecraft/entity/EntityLivingBase;getDisplayName()Lnet/minecraft/util/IChatComponent;"))
    public IChatComponent renderName_getDisplayName(EntityLivingBase entity) {
        if(PlayerDiguiser.tracker.get(entity)!=null) return new ChatComponentText("");
        return entity.getDisplayName();
        
    }

    // @Inject(method = "getColorMultiplier(Lnet/minecraft/entity/EntityLivingBase;FF)I", at = @At("RETURN"), cancellable = true)
    // private void customColorMultiplier(EntityLivingBase entity, float lightBrightness, float partialTickTime, CallbackInfoReturnable<Integer> callbackInfo) {
    //     int customColor = 0x00FFFF; // Example: Red color (replace with your logic)
    //     System.out.println("Set red Color of "+entity.getName());
    //     // Return the custom color
    //     GlStateManager.color(0, 255, 255, 255);
    //     GL11.glColor3f(0, 255, 255);
    //     callbackInfo.setReturnValue(customColor);
    // }

    // @Redirect(method = "setBrightness", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;hurtTime:I", opcode = Opcodes.GETFIELD))
    // private int changeHurtTime(EntityLivingBase entity) {
    //     return 1;
    // }
}