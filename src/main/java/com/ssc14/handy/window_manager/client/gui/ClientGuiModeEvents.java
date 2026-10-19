package com.ssc14.handy.window_manager.client.gui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUIClientAccess;
import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.registry.HandyKeybinds;
import com.ssc14.handy.window_manager.window.WindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(
        modid = Handy.MOD_ID,
        value = Dist.CLIENT
)
public final class ClientGuiModeEvents {
    private static final Map<Integer, MouseButtonInfo> GUI_PRESSES =
            new HashMap<>();

    private static double lastMouseX = Double.NaN;
    private static double lastMouseY = Double.NaN;

    private ClientGuiModeEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (HandyKeybinds.TOGGLE_GUI_MODE.consumeClick()) {
            GuiMode.toggle();
        }

        while (HandyKeybinds.DEBUG_UI.consumeClick()) {
            var ui = WindowManager.INSTANCE.getMainUI();

            if (ui != null) {
                ModularUIClientAccess.enableDebugger(
                        ui,
                        !ui.isDebugMode()
                );
            }
        }

        GuiMode.update();
        WindowManager.INSTANCE.tick();
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        int button = event.getButton();
        int action = event.getAction();

        // Не пропускаем отпускание, если режим уже выключен
        // после нажатия внутри GUI либо клика вне GUI.

        if(!GuiMode.isActive()) return;
        if (action == GLFW.GLFW_RELEASE) {
            event.setCanceled(true);
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null || mc.screen != null) {
                GuiMode.exit();
                return;
            }

            double mouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());
            double mouseY = mc.mouseHandler.getScaledYPos(mc.getWindow());

            var mouseEvent = new MouseButtonEvent(
                    mouseX,
                    mouseY,
                    event.getMouseButtonInfo()
            );

            WindowManager manager = WindowManager.INSTANCE;

            manager.mouseReleased(
                    mouseEvent
            );

            GUI_PRESSES.remove(button, event.getMouseButtonInfo());
            return;
        }

        if (action != GLFW.GLFW_PRESS) {
            return;
        }

        // Пока GuiMode активен, ни один клик не должен попасть в мир.

//        if(!GuiMode.isActive()) return;
        event.setCanceled(true);

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.screen != null) {
            GuiMode.exit();
            return;
        }

        double mouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());
        double mouseY = mc.mouseHandler.getScaledYPos(mc.getWindow());

        var mouseEvent = new MouseButtonEvent(
                mouseX,
                mouseY,
                event.getMouseButtonInfo()
        );

        WindowManager manager = WindowManager.INSTANCE;

        boolean handled = manager.mousePressed(
                mouseEvent
        );

        GUI_PRESSES.put(button, event.getMouseButtonInfo());

        if (!handled && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            GuiMode.exit();
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (GuiMode.isActive()) {
            event.setCanceled(true);
        }
    }

    public static void updateMouseMovement(double mouseX, double mouseY) {
        if (!GuiMode.isActive()) {
            lastMouseX = Double.NaN;
            lastMouseY = Double.NaN;
            return;
        }

        double dx = Double.isNaN(lastMouseX)
                ? 0 : mouseX - lastMouseX;
        double dy = Double.isNaN(lastMouseY)
                ? 0 : mouseY - lastMouseY;

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        WindowManager manager = WindowManager.INSTANCE;

        if (GUI_PRESSES.isEmpty()) {
            manager.mouseMoved(mouseX, mouseY);
        } else if (dx != 0 || dy != 0) {
            MouseButtonInfo buttonInfo =
                    GUI_PRESSES.values().iterator().next();

            manager.mouseDragged(
                    new MouseButtonEvent(mouseX, mouseY, buttonInfo),
                    dx,
                    dy
            );
        }
    }
}