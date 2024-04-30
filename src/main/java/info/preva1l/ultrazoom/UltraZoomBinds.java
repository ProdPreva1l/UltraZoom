package info.preva1l.ultrazoom;

import lombok.experimental.UtilityClass;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@UtilityClass
public class UltraZoomBinds {
    public final String ZOOM_CATEGORY = "key.ultrazoom.category";
    public final KeyMapping ZOOM_KEY = new KeyMapping("key.ultrazoom.zoom", GLFW.GLFW_KEY_C, ZOOM_CATEGORY);
    public final KeyMapping ZOOM_RESET_KEY = new KeyMapping("key.ultrazoom.reset", GLFW.GLFW_MOUSE_BUTTON_MIDDLE, ZOOM_CATEGORY);
}
