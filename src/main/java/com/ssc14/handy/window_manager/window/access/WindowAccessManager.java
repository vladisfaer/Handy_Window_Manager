package com.ssc14.handy.window_manager.window.access;

import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.api.BlockEntityUIProvider;
import com.ssc14.handy.window_manager.api.EntityUIProvider;
import com.ssc14.handy.window_manager.api.ItemUIProvider;
import com.ssc14.handy.window_manager.item.ServerItemRegistry;
import com.ssc14.handy.window_manager.server.ui.ServerUIManager;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Проверяет для каждого игрока и его открытых окон, имеет ли он доступ к окну
 *
 * <p>Если игрок не имеет доступ к открытому окну, закрывает его
 *
 * @author vladisfaer
 */
@EventBusSubscriber(modid = Handy.MOD_ID)
public final class WindowAccessManager {
    private static int tickCounter;

    private WindowAccessManager() {}

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        if ((++tickCounter & 1) != 0) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            // A snapshot is required because closeWindow removes the source entry.
            for (Source source : ServerUIManager.getSources(player)) {
                if (!hasAccess(player, source)) {
                    ServerUIManager.closeWindow(player, source);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        tickCounter = 0;
    }

    private static boolean hasAccess(ServerPlayer player, Source source) {
        return switch (source.getType()) {
            case ITEM_STACK -> hasItemStackAccess(player, source.getUuid());
            // Entity and BlockEntity checks will be implemented with their providers.
            case ENTITY -> hasEntityAccess(player, source.getUuid());
            case BLOCK_ENTITY -> hasBlockEntityAccess(player, source.getGlobalPos());
        };
    }

    private static boolean hasItemStackAccess(ServerPlayer player, UUID instanceId) {
        if (instanceId == null) return false;

        List<ItemStack> stacks = ServerItemRegistry.INSTANCE.get(instanceId);
        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof ItemUIProvider provider) {
                return provider.hasAccess(player, instanceId);
            }
        }
        return false;
    }

    private static boolean hasEntityAccess(ServerPlayer player, UUID id){
        if (id == null) return false;

        Entity entity = ServerUIManager.findEntityByUuidEverywhere(id);
        if (!(entity instanceof EntityUIProvider provider)) return false;
        return provider.hasAccess(player, entity);
    }

    private static boolean hasBlockEntityAccess(ServerPlayer player, GlobalPos globalPos){
        if (globalPos == null) return false;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return false;
        }
        BlockEntity blockEntity = Objects.requireNonNull(server.getLevel(globalPos.dimension())).getBlockEntity(globalPos.pos());
        if (!(blockEntity instanceof BlockEntityUIProvider provider)) return false;

        return provider.hasAccess(player, globalPos);
    }
}
