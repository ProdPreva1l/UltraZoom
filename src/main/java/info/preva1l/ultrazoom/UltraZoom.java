package info.preva1l.ultrazoom;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class UltraZoom implements ClientModInitializer {
    @Getter private static final double DEFAULT_ZOOM_LEVEL = 0.13; // The default level to zoom to, and to reset to when the reset button is clicked

    // Async stuff to make the zooming smooth like lunars zoom module
    // If i did the button pressed logic in the mixin it would be limited to being as smooth as 20 fps very sad :(
    // So this is my workaround, im assuming lunars code looks similar (deffo alot cleaner as this is very rushed)
    private final ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final Minecraft mc = Minecraft.getInstance();

    // Track this otherwise weird poop
    private static boolean originalSmoothCameraEnabled = false;

    @Getter @Setter private static double targetZoomLevel = DEFAULT_ZOOM_LEVEL;
    @Getter @Setter private static double currentZoomLevel = 1.00;
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(UltraZoomBinds.ZOOM_KEY);
        KeyBindingHelper.registerKeyBinding(UltraZoomBinds.ZOOM_RESET_KEY);
        schedule.scheduleAtFixedRate(()-> {
            if (UltraZoomBinds.ZOOM_RESET_KEY.isDown()) {
                setTargetZoomLevel(DEFAULT_ZOOM_LEVEL);
            }

            if (UltraZoomBinds.ZOOM_KEY.isDown()) {
                smoothAdjust(getTargetZoomLevel());
                originalSmoothCameraEnabled = mc.options.smoothCamera;
                mc.options.smoothCamera = true;
            } else {
                smoothAdjust(1.00);
                mc.options.smoothCamera = originalSmoothCameraEnabled;
            }

        }, 1, 10, TimeUnit.MILLISECONDS);
    }

    public static void adjustZoomLevel(boolean zoomOut) {
        if (zoomOut) {
            if ((getTargetZoomLevel() - 0.08) >= 0.08) return;

            executorService.submit(() -> {
                double toSet = getTargetZoomLevel() + 0.08;
                long lastIterationTime = System.currentTimeMillis();
                while (toSet < 1.08) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - lastIterationTime;
                    if (elapsedTime < 2) {
                        continue;
                    }
                    toSet += toSet - 0.01;
                    setTargetZoomLevel(toSet);
                    lastIterationTime = currentTime;
                }
            });
        } else {
            if ((getTargetZoomLevel() - 0.08) <= 0.08) return;

            executorService.submit(() -> {
                double toSet = getTargetZoomLevel() - 0.08;
                long lastIterationTime = System.currentTimeMillis();
                while (toSet > 0.08) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - lastIterationTime;
                    if (elapsedTime < 2) {
                        continue;
                    }
                    toSet += toSet - 0.01;
                    setTargetZoomLevel(toSet);
                    lastIterationTime = currentTime;
                }
            });
        }
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
