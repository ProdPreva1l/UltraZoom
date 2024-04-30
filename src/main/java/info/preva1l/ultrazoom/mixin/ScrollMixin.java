package info.preva1l.ultrazoom.mixin;

import info.preva1l.ultrazoom.UltraZoom;
import info.preva1l.ultrazoom.UltraZoomBinds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public class ScrollMixin {
    @Inject(method = "onScroll",  at = @At("HEAD"), cancellable = true)
    private void ultraZoomOnScroll(long l, double d, double e, CallbackInfo ci) {
        if (e == 0) return;
        if(!UltraZoomBinds.ZOOM_KEY.isDown()) return;

        UltraZoom.adjustZoomLevel(e < 0);
        ci.cancel();
    }
}