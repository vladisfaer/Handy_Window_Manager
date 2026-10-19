package com.ssc14.handy.window_manager.mixin;

import com.ssc14.handy.window_manager.api.ItemStackLifecycle;
import com.ssc14.handy.window_manager.item.ServerItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.EffectiveSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин, используемый {@link ServerItemRegistry} для отслеживания {@link ItemStack}
 *
 * @author vladisfaer
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(
            method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
            at = @At("RETURN")
    )
    private void handy$onCreated(
            Holder<Item> item,
            int count,
            PatchedDataComponentMap components,
            CallbackInfo ci
    ) {
        if (item.value() instanceof ItemStackLifecycle lifecycle) {
            ItemStack stack = (ItemStack) (Object) this;
            lifecycle.handy$onItemStackCreated(stack);
        }
    }

    @Inject(
            method = "setCount",
            at = @At("HEAD"),
            require = 1
    )
    private void handy$onCountSetToZero(int count, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getCount() > 0 && count <= 0) {
            Item item = stack.getItem();

            if (item instanceof ItemStackLifecycle listener) {
                listener.handy$onItemStackDestroyed(stack);
            }
        }
    }

    @Inject(method = "setCount", at = @At("RETURN"))
    private void handy$onCountChanged(int count, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!(stack.getItem() instanceof ItemStackLifecycle)) {
            return;
        }

        if (!EffectiveSide.get().isServer()) {
            return;
        }

        if (stack.isEmpty()) {
            ServerItemRegistry.INSTANCE.unregister(stack);
        } else {
            ServerItemRegistry.INSTANCE.register(stack);
        }
    }
}