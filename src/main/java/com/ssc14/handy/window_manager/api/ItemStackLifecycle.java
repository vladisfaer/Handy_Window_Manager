package com.ssc14.handy.window_manager.api;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ItemStackLifecycle {

    void handy$onItemStackCreated(ItemStack stack);

    void handy$onItemStackDestroyed(ItemStack stack);
}