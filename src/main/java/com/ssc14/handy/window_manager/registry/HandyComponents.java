package com.ssc14.handy.window_manager.registry;

import com.mojang.serialization.Codec;
import com.ssc14.handy.window_manager.Handy;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;
import java.util.function.Supplier;

public final class HandyComponents {
    public static final DeferredRegister.DataComponents REGISTRAR =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    Handy.MOD_ID
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> INSTANCE_ID =
            REGISTRAR.registerComponentType(
                    "instance_id",
                    builder -> builder
                            .persistent(UUIDUtil.CODEC)
                            .networkSynchronized(UUIDUtil.STREAM_CODEC)
            );

    public static final Supplier<DataComponentType<Integer>> CLICKS =
            REGISTRAR.registerComponentType(
                    "clicks",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
            );
}