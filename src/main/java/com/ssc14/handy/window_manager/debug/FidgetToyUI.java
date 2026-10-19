package com.ssc14.handy.window_manager.debug;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;

import com.lowdragmc.lowdraglib2.utils.XmlUtils;

import com.ssc14.handy.window_manager.item.ServerItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public final class FidgetToyUI {

    private static final Identifier UI_LOCATION =
            Identifier.parse("handy:ui/fidget_toy.xml");

    private FidgetToyUI() {
    }

    public static ModularUI create(
            UUID id,
            Player player
    ) {
        var xml = XmlUtils.loadXml(UI_LOCATION);

        if (xml == null) {
            throw new IllegalStateException(
                    "Failed to load " + UI_LOCATION
            );
        }

        var ui = UI.of(xml);

        Label counter = ui.selectId("counter", Label.class)
                .findFirst()
                .orElseThrow();

        Button button = ui.selectId("click_button", Button.class)
                .findFirst()
                .orElseThrow();

        counter.bind(
                DataBindingBuilder.componentS2C(
                        () -> Component.literal(
                                "Clicks: " + getClicks(id)
                        )
                ).build()
        );

        button.setOnServerClick(_ ->
                ServerItemRegistry.INSTANCE.execute(id, FidgetToyItem::incrementClicks)
        );

        return ModularUI.of(ui, player);
    }

    private static int getClicks(
            UUID id
    ) {
        List<ItemStack> stacks = ServerItemRegistry.INSTANCE.get(id);

        if (stacks.isEmpty()) {
            return 0;
        }

        return FidgetToyItem.getClicks(stacks.getFirst());
    }
}