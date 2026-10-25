package com.ssc14.handy.window_manager.api;

import com.ssc14.handy.window_manager.window.Window;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IWindowHandler {
    void registerPersistentWindow(Supplier<? extends Window> factory);
    void registerPersistentWindow(Function<Player, ? extends Window> factory);
}
