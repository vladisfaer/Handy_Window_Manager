package com.ssc14.handy.window_manager.mixin.network;

import com.lowdragmc.lowdraglib2.networking.both.PacketModularUISync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PacketModularUISync.class)
public interface PacketModularUISyncAccessor {

    @Accessor("data")
    byte[] handy$getData();
}