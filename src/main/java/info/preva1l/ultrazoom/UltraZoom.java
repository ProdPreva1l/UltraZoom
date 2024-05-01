package info.preva1l.ultrazoom;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;

import java.util.concurrent.*;

public class UltraZoom implements ClientModInitializer {
    private static final double DEFAULT_ZOOM_LEVEL = 0.13; // The default level to zoom to, and to reset to when the reset button is clicked
    private static final double MAX_ZOOM_LEVEL = 0.05; // The default level to zoom to, and to reset to when the reset button is clicked
    private static final double MIN_ZOOM_LEVEL = 1.00; // The default level to zoom to, and to reset to when the reset button is clicked


    // Async stuff to make the zooming smooth like lunars zoom module
    // If i did the button pressed logic in the mixin it would be limited to being as smooth as 20 fps very sad :(
    // So this is my workaround, im assuming lunars code looks similar (deffo alot cleaner as this is very rushed)
    private final ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final Minecraft mc = Minecraft.getInstance();

    @Getter @Setter private static double targetZoomLevel = DEFAULT_ZOOM_LEVEL;
    @Getter @Setter private static double currentZoomLevel = 1.00;
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(UltraZoomBinds.ZOOM_KEY);
        KeyBindingHelper.registerKeyBinding(UltraZoomBinds.ZOOM_RESET_KEY);
        schedule.scheduleAtFixedRate(()-> {
            if (UltraZoomBinds.ZOOM_KEY.isDown()) {
                if (UltraZoomBinds.ZOOM_RESET_KEY.isDown()) {
                    setTargetZoomLevel(DEFAULT_ZOOM_LEVEL);
                }
                smoothAdjust(getTargetZoomLevel());

                // I must admit the cine camera is jank but oh well
                mc.execute(()-> mc.options.smoothCamera = true);
            } else {
                smoothAdjust(1.00);
                mc.execute(()-> mc.options.smoothCamera = false);
            }

        }, 1, 1, TimeUnit.MILLISECONDS);
    }

    public static void adjustZoomLevel(boolean zoomOut) {
        double toSet;
        if (zoomOut) {
            toSet = getTargetZoomLevel() + 0.08;
            if (toSet > MIN_ZOOM_LEVEL) toSet = MIN_ZOOM_LEVEL;
        } else {
            toSet = getTargetZoomLevel() - 0.08;
            if (toSet < MAX_ZOOM_LEVEL) toSet = MAX_ZOOM_LEVEL;
        }
        setTargetZoomLevel(toSet);
    }

    public static void smoothAdjust(double targetValue) {
        executorService.submit(() -> {
            double initialValue = getCurrentZoomLevel();
            double currentValue = initialValue;
            long lastIterationTime = System.currentTimeMillis();
            double increment = (targetValue > initialValue) ? 0.01 : -0.01;

            while (Math.abs(currentValue - targetValue) >= 0.01) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastIterationTime;
                if (elapsedTime < 2) {
                    continue;
                }
                currentValue += increment;
                setCurrentZoomLevel(currentValue);
                lastIterationTime = currentTime;
            }
            setCurrentZoomLevel(targetValue);
        });
    }
}
