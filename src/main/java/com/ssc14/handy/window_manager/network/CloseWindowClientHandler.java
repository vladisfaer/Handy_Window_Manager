package com.ssc14.handy.window_manager.network;

import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.window.WindowManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(
        modid = Handy.MOD_ID,
        value = Dist.CLIENT
)
public final class CloseWindowClientHandler {
    private CloseWindowClientHandler() {}

    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(
                CloseWindowPayload.TYPE,
                (payload, _) -> WindowManager.INSTANCE.closeWindow(payload.source())
        );
    }
}
