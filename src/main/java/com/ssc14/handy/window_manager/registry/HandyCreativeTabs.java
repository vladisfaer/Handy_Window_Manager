package com.ssc14.handy.window_manager.registry;

import com.ssc14.handy.window_manager.Handy;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HandyCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    Handy.MOD_ID
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
            TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.handy.main"))
                    .icon(() -> new ItemStack(Items.REDSTONE))
                    .displayItems((_, output) ->
                            output.accept(HandyItems.FIDGET_TOY.get())
                    )
                    .build());

    private HandyCreativeTabs() {}
}