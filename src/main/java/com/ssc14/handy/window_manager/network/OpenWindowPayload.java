package com.ssc14.handy.window_manager.network;

import com.ssc14.handy.window_manager.window.access.Source;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import com.ssc14.handy.window_manager.Handy;
import org.jspecify.annotations.NonNull;

/**
 * Пакет, отправляемый с клиента на сервер при открытии окна
 * @param source источник окна
 *
 * @author vladisfaer
 */
public record OpenWindowPayload(Source source)
        implements CustomPacketPayload {

    public static final Type<OpenWindowPayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            Handy.MOD_ID,
                            "open_item_ui"
                    )
            );

    public static final StreamCodec<ByteBuf, OpenWindowPayload>
            STREAM_CODEC = StreamCodec.composite(
            Source.STREAM_CODEC,
            OpenWindowPayload::source,
            OpenWindowPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}