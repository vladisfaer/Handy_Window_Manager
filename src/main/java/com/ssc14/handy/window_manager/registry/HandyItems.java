package com.ssc14.handy.window_manager.registry;

import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.debug.FidgetToyItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HandyItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Handy.MOD_ID);

    public static final DeferredItem<FidgetToyItem> FIDGET_TOY =
            ITEMS.registerItem(
                    "fidget_toy",
                    FidgetToyItem::new
            );

    private HandyItems() {}
}