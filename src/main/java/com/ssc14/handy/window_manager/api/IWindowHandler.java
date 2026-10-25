package com.ssc14.handy.window_manager.api;

import com.ssc14.handy.window_manager.window.Window;

import java.util.Objects;
import java.util.function.Supplier;

public interface IWindowHandler {
    void registerPersistentWindow(Supplier<? extends Window> factory);
}
