package com.ssc14.handy.window_manager;

import com.mojang.logging.LogUtils;
import com.ssc14.handy.window_manager.registry.*;
import com.ssc14.handy.window_manager.registry.HandyComponents;
import com.ssc14.handy.window_manager.registry.HandyCreativeTabs;
import com.ssc14.handy.window_manager.registry.HandyItems;
import com.ssc14.handy.window_manager.registry.HandyNetwork;
import com.ssc14.handy.window_manager.window.RootMUI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;

@Mod(Handy.MOD_ID)
public final class Handy {
    public static final String MOD_ID = "handy_wm";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Handy(IEventBus modEventBus, ModContainer modContainer) {
        HandyItems.ITEMS.register(modEventBus);
        HandyComponents.REGISTRAR.register(modEventBus);
        HandyCreativeTabs.TABS.register(modEventBus);

        modEventBus.addListener(HandyNetwork::registerPayloads);
        RootMUI.registerUI();
    }
}