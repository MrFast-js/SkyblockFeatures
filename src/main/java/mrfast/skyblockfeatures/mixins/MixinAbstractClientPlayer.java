package mrfast.skyblockfeatures.mixins;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.CapeUtils;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer {
    @Shadow
    @Nullable
    protected abstract NetworkPlayerInfo getPlayerInfo();
    
    @Inject(method={"getLocationCape"}, at={@At(value="HEAD")}, cancellable=true)
    public void getLocationCape(CallbackInfoReturnable<ResourceLocation> callbackInfoReturnable) {
        NetworkPlayerInfo info = this.getPlayerInfo();
        if(info != null) {
            if (CapeUtils.is_name_valid(info.getGameProfile().getName())) {
                callbackInfoReturnable.setReturnValue(CapeUtils.getCape(info.getGameProfile().getName()));
            }
        }
    }
}

