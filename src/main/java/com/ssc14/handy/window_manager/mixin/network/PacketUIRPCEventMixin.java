package com.ssc14.handy.window_manager.mixin.network;

import com.lowdragmc.lowdraglib2.networking.both.PacketUIRPCEvent;
import com.ssc14.handy.window_manager.server.ui.ServerUIManager;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketUIRPCEvent.class)
public abstract class PacketUIRPCEventMixin {

    @Inject(
            method = "execute",
            at = @At("RETURN")
    )
    private static void handy$handleWithoutMenu(
            PacketUIRPCEvent packet,
            IPayloadContext context,
            CallbackInfo ci
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (player.containerMenu != player.inventoryMenu) {
            return;
        }

        ServerUIManager.handleRPC(player, packet.eventData);
    }
}