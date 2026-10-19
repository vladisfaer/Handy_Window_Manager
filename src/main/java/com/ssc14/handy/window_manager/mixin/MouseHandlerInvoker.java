package com.ssc14.handy.window_manager.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Миксин, используемый для управления мышью при включении/выключении GuiMode
 *
 * @author vladisfaer
 */
@Mixin(MouseHandler.class)
public interface MouseHandlerInvoker {
    @Invoker("releaseMouse")
    void handy$releaseMouse();

    @Invoker("grabMouse")
    void handy$grabMouse();

    @Invoker("onButton")
    void handy$onButton(
            long handle,
            MouseButtonInfo buttonInfo,
            int action
    );
}