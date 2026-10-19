package com.ssc14.handy.window_manager.registry;

import com.ssc14.handy.window_manager.network.CloseWindowPayload;
import com.ssc14.handy.window_manager.network.OpenWindowHandler;
import com.ssc14.handy.window_manager.network.OpenWindowPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class HandyNetwork {

    private HandyNetwork() {
    }

    public static void registerPayloads(
            RegisterPayloadHandlersEvent event
    ) {
        event.registrar("1").playToServer(
                OpenWindowPayload.TYPE,
                OpenWindowPayload.STREAM_CODEC,
                OpenWindowHandler::handle
        );
        event.registrar("1").playToClient(
                CloseWindowPayload.TYPE,
                CloseWindowPayload.STREAM_CODEC
        );
    }
}