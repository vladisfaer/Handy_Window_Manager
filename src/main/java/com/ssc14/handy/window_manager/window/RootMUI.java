package com.ssc14.handy.window_manager.window;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.ssc14.handy.window_manager.Handy;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class RootMUI {
    public static final Identifier START_UI =
            Identifier.fromNamespaceAndPath(Handy.MOD_ID, "start_ui");

    public static void registerUI() {
        PlayerUIMenuType.register(
                START_UI,
                _ -> RootMUI::createUI
        );
    }

    public static ModularUI createUI(Player player) {
        Screen screen = Minecraft.getInstance().screen;
        assert screen != null;
        var root = new UIElement()
                .layout(layout -> layout
                        .display(TaffyDisplay.FLEX)
                        .widthPercent(100)
                        .heightPercent(100)
                );

        return ModularUI.of(UI.of(root), player);
    }
}
