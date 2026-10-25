package com.ssc14.handy.window_manager.client;

import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.event.RegisterPersistentWindowsEvent;
import com.ssc14.handy.window_manager.window.WindowManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(
        modid = Handy.MOD_ID,
        value = Dist.CLIENT
)
public class ResourceLoadHandler {
    @SubscribeEvent
    public static void onResourcesLoaded(ClientResourceLoadFinishedEvent event) {
        if (!event.isInitial()) {
            return;
        }
        NeoForge.EVENT_BUS.post(
                new RegisterPersistentWindowsEvent(WindowManager.INSTANCE)
        );
    }
}
