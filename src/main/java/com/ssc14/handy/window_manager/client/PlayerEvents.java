package com.ssc14.handy.window_manager.client;

import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.window.WindowManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(
        modid = Handy.MOD_ID,
        value = Dist.CLIENT
)
public final class PlayerEvents {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        WindowManager.INSTANCE.ensureReady();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event){
        WindowManager.INSTANCE.discard();
    }
}