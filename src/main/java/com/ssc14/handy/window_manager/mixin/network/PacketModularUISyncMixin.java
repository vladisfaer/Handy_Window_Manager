package com.ssc14.handy.window_manager.mixin.network;

import com.lowdragmc.lowdraglib2.networking.both.PacketModularUISync;
import com.ssc14.handy.window_manager.server.ui.ServerUIManager;

import com.ssc14.handy.window_manager.window.WindowManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketModularUISync.class)
public abstract class PacketModularUISyncMixin {

    @Inject(
            method = "executeClient",
            at = @At("RETURN")
    )
    private static void handy$syncClientWithoutMenu(
            PacketModularUISync packet,
            IPayloadContext context,
            CallbackInfo ci
    ) {
        var player = context.player();

        if (player.containerMenu != player.inventoryMenu) {
            return;
        }

        byte[] data =
                ((PacketModularUISyncAccessor) packet)
                        .handy$getData();

        WindowManager.INSTANCE.handleSync(player, data);
    }

    @Inject(
            method = "executeServer",
            at = @At("RETURN")
    )
    private static void handy$syncServerWithoutMenu(
            PacketModularUISync packet,
            IPayloadContext context,
            CallbackInfo ci
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (player.containerMenu != player.inventoryMenu) {
            return;
        }

        byte[] data =
                ((PacketModularUISyncAccessor) packet)
                        .handy$getData();

        ServerUIManager.handleSync(player, data);
    }
}