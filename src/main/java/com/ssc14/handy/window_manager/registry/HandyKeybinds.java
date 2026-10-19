package com.ssc14.handy.window_manager.registry;

import com.ssc14.handy.window_manager.Handy;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
        modid = Handy.MOD_ID,
        value = Dist.CLIENT
)
public class HandyKeybinds {
    public static final KeyMapping TOGGLE_GUI_MODE =
            new KeyMapping(
                    "key.handy.toggle_gui_mode",
                    GLFW.GLFW_KEY_G,
                    KeyMapping.Category.MISC
            );

    public static final KeyMapping DEBUG_UI =
            new KeyMapping(
                    "key.handy.debug_ui",
                    GLFW.GLFW_KEY_F12,
                    KeyMapping.Category.MISC
            );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_GUI_MODE);
        event.register(DEBUG_UI);
    }
}