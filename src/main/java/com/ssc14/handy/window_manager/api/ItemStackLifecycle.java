package com.ssc14.handy.window_manager.api;

import net.minecraft.world.item.ItemStack;

public interface ItemStackLifecycle {

    void handy$onItemStackCreated(ItemStack stack);

    void handy$onItemStackDestroyed(ItemStack stack);
}