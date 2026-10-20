package com.ssc14.handy.window_manager.client.gui;

import com.ssc14.handy.window_manager.mixin.MouseHandlerInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonInfo;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

/**
 * Класс, отвечающий за работу с GUI режимом
 *
 * <p>GUI режим - это режим работы графического интерфейса, когда игрок может взаимодействовать с окнами,
 * но теряет некоторые способы взаимодействия с миром (например клик по блоку)
 *
 * @author vladisfaer
 */
public final class GuiMode {
    private static boolean active = false;

    private GuiMode() {}

    /**
     * Проверяет, активен ли GUI режим
     */
    public static boolean isActive() {
        return active;
    }

    /**
     * Включает GUI режим
     */
    public static void enter() {
        setActive(true);
    }

    /**
     * Выключает GUI режим
     */
    public static void exit() {
        setActive(false);
    }

    /**
     * Переключает GUI режим
     */
    public static void toggle() {
        setActive(!active);
    }

    @ApiStatus.Internal
    public static void update() {
        Minecraft mc = Minecraft.getInstance();

        if (active && (
                mc.player == null ||
                        mc.level == null ||
                        mc.screen != null
        )) {
            setActive(false);
        }
    }

    private static void setActive(boolean value) {
        Minecraft mc = Minecraft.getInstance();

        if (value && (
                mc.player == null ||
                        mc.level == null ||
                        mc.screen != null
        )) {
            return;
        }

        if (active == value) {
            return;
        }

        if (value) {
            releaseHeldMouseButtons(mc);
        }

        active = value;

        var mouse = (MouseHandlerInvoker) mc.mouseHandler;

        if (value) {
            mouse.handy$releaseMouse();
        } else if (mc.player != null && mc.screen == null) {
            mouse.handy$grabMouse();
        }
    }

    private static void releaseHeldMouseButtons(Minecraft mc) {
        var window = mc.getWindow();
        long handle = window.handle();

        var mouse = (MouseHandlerInvoker) mc.mouseHandler;

        for (int button = 0;
             button <= GLFW.GLFW_MOUSE_BUTTON_LAST;
             button++) {

            if (GLFW.glfwGetMouseButton(handle, button)
                    != GLFW.GLFW_PRESS) {
                continue;
            }

            mouse.handy$onButton(
                    handle,
                    new MouseButtonInfo(button, 0),
                    GLFW.GLFW_RELEASE
            );
        }
    }
}