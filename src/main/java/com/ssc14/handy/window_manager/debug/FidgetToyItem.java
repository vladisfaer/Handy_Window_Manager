package com.ssc14.handy.window_manager.debug;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.ssc14.handy.window_manager.api.ItemUIProvider;
import com.ssc14.handy.window_manager.client.gui.GuiMode;
import com.ssc14.handy.window_manager.registry.HandyComponents;
import com.ssc14.handy.window_manager.window.WindowProperties;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class FidgetToyItem extends ItemUIProvider {

    public FidgetToyItem(Properties properties) {
        super(properties.stacksTo(1).component(HandyComponents.CLICKS, 0));
    }

    @Override
    public @NonNull InteractionResult use(Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (level.isClientSide()) {
            openUI(player.getItemInHand(hand), player);
            GuiMode.enter();
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public ModularUI getUI(UUID id, Player player) {
        return FidgetToyUI.create(id, player);
    }

    public static int getClicks(ItemStack stack) {
        return stack.getOrDefault(HandyComponents.CLICKS.get(), -2);
    }

    public static void setClicks(ItemStack stack, int clicks) {
        if (clicks < 0) {
            throw new IllegalArgumentException("Clicks cannot be negative");
        }

        stack.set(HandyComponents.CLICKS.get(), clicks);
    }

    public static void incrementClicks(ItemStack stack) {
        int clicks = getClicks(stack) + 1;
        setClicks(stack, clicks);
    }

    @Override
    public WindowProperties getWindowProperties(){
        return new WindowProperties()
                .height(80)
                .width(80)
                .minHeight(60)
                .minWidth(60);
    }
}