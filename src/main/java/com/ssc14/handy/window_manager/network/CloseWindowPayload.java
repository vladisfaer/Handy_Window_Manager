package com.ssc14.handy.window_manager.network;

import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.window.access.Source;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * Пакет, отправляемый сервером на клиентскую сторону при закрытии окна
 * @param source источник закрываемого окна
 */
public record CloseWindowPayload(Source source) implements CustomPacketPayload {
    public static final Type<CloseWindowPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Handy.MOD_ID, "close_window")
    );

    public static final StreamCodec<ByteBuf, CloseWindowPayload> STREAM_CODEC =
            StreamCodec.composite(Source.STREAM_CODEC, CloseWindowPayload::source, CloseWindowPayload::new);

    public CloseWindowPayload {
        Objects.requireNonNull(source, "source");
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
