package com.ssc14.handy.window_manager.registry;

import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.window.WindowManager;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(
        modid = Handy.MOD_ID,
        value = Dist.CLIENT
)
public class HandyGuiLayers {
    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(
                        Handy.MOD_ID,
                        "debug_main_ui"
                ),
                (graphics, _) ->
                        WindowManager.INSTANCE.render(graphics)
        );
    }
}
