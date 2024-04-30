package info.preva1l.ultrazoom.mixin;

import info.preva1l.ultrazoom.UltraZoom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class RenderMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    public void getZoomLevel(CallbackInfoReturnable<Double> callbackInfo) {
        callbackInfo.setReturnValue(callbackInfo.getReturnValue() * UltraZoom.getCurrentZoomLevel());
    }
}