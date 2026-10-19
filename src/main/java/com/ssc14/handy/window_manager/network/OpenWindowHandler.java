package com.ssc14.handy.window_manager.network;

import com.ssc14.handy.window_manager.server.ui.ServerUIManager;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class OpenWindowHandler {

    private OpenWindowHandler() {
    }

    public static void handle(
            OpenWindowPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        ServerUIManager.openUI(
                player,
                payload.source()
        );
    }
}