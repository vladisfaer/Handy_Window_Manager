package com.ssc14.handy.window_manager.window.access;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.UUID;

/**
 * Источник окна. Является способом идентифицировать окна и их источники между клиентом и сервером
 * {@link net.minecraft.world.item.ItemStack} и {@link net.minecraft.world.entity.Entity} идентифицируются с помощью {@link UUID}, а {@link net.minecraft.world.level.block.entity.BlockEntity} с помощью {@link GlobalPos}
 */
public final class Source {
    public static final StreamCodec<ByteBuf, Source> STREAM_CODEC =
            StreamCodec.of(Source::write, Source::read);

    private final SourceType type;
    private final UUID uuid;
    private final GlobalPos globalPos;

    private Source(SourceType type, UUID uuid, GlobalPos globalPos) {
        this.type = Objects.requireNonNull(type, "type");
        this.uuid = uuid;
        this.globalPos = globalPos;

        boolean usesUuid = type == SourceType.ITEM_STACK || type == SourceType.ENTITY;
        if (usesUuid && (uuid == null || globalPos != null)) {
            throw new IllegalArgumentException(type + " source requires a UUID only");
        }
        if (type == SourceType.BLOCK_ENTITY && (globalPos == null || uuid != null)) {
            throw new IllegalArgumentException("BLOCK_ENTITY source requires a GlobalPos only");
        }
    }

    /**
     * Получает источник окна из {@link net.minecraft.world.item.ItemStack}
     * @param uuid UUID предмета
     * @return источник окна
     */
    public static Source itemStack(UUID uuid) {
        return new Source(SourceType.ITEM_STACK, uuid, null);
    }

    /**
     * Получает источник окна из {@link net.minecraft.world.entity.Entity}
     * @param uuid UUID сущности
     * @return источник окна
     */
    public static Source entity(UUID uuid) {
        return new Source(SourceType.ENTITY, uuid, null);
    }

    /**
     * Получает источник окна из {@link net.minecraft.world.level.block.entity.BlockEntity}
     * @param pos позиция блока
     * @return источник окна
     */
    public static Source blockEntity(GlobalPos pos) {
        return new Source(SourceType.BLOCK_ENTITY, null, pos);
    }

    private static void write(ByteBuf buffer, Source source) {
        ByteBufCodecs.VAR_INT.encode(buffer, source.type.getId());
        switch (source.type) {
            case ITEM_STACK, ENTITY -> UUIDUtil.STREAM_CODEC.encode(buffer, source.uuid);
            case BLOCK_ENTITY -> GlobalPos.STREAM_CODEC.encode(buffer, source.globalPos);
        }
    }

    private static Source read(ByteBuf buffer) {
        SourceType type = SourceType.fromId(ByteBufCodecs.VAR_INT.decode(buffer));
        return switch (type) {
            case ITEM_STACK -> itemStack(UUIDUtil.STREAM_CODEC.decode(buffer));
            case ENTITY -> entity(UUIDUtil.STREAM_CODEC.decode(buffer));
            case BLOCK_ENTITY -> blockEntity(GlobalPos.STREAM_CODEC.decode(buffer));
        };
    }

    /**
     * Возвращает тип источника
     */
    public SourceType getType() {
        return type;
    }

    /**
     * Возвращает {@link UUID} источника
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Возвращает {@link GlobalPos} источника
     */
    public GlobalPos getGlobalPos() {
        return globalPos;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Source other)) return false;
        if(other.type!=this.type) return false;

        if (uuid != null && other.uuid != null) {
            return uuid.equals(other.uuid);
        }
        if (globalPos != null && other.globalPos != null) {
            return globalPos.equals(other.globalPos);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : globalPos.hashCode();
    }

    @Override
    public String toString() {
        return switch (type) {
            case ITEM_STACK -> "ITEM_STACK[" + uuid + "]";
            case ENTITY -> "ENTITY[" + uuid + "]";
            case BLOCK_ENTITY -> "BLOCK_ENTITY[" + globalPos + "]";
        };
    }
}
