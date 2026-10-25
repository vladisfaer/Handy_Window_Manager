package com.ssc14.handy.window_manager.event;

import com.ssc14.handy.window_manager.api.IWindowHandler;
import net.neoforged.bus.api.Event;

import java.util.Objects;

/**
 * Событие, вызываемое в момент регистрации постоянного окна
 */
public final class RegisterPersistentWindowsEvent extends Event {
    private final IWindowHandler manager;

    public RegisterPersistentWindowsEvent(IWindowHandler manager) {
        this.manager = Objects.requireNonNull(manager);
    }

    /**
     * Возвращает клиентское или серверное представление оконного менеджера для регистрации
     */
    public IWindowHandler getManager() {
        return manager;
    }
}