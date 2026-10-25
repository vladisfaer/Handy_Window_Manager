package com.ssc14.handy.window_manager.server.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.utils.ByteBufUtil;
import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.api.BlockEntityUIProvider;
import com.ssc14.handy.window_manager.api.EntityUIProvider;
import com.ssc14.handy.window_manager.api.IWindowHandler;
import com.ssc14.handy.window_manager.api.ItemUIProvider;
import com.ssc14.handy.window_manager.event.RegisterPersistentWindowsEvent;
import com.ssc14.handy.window_manager.item.ServerItemRegistry;
import com.ssc14.handy.window_manager.network.CloseWindowPayload;
import com.ssc14.handy.window_manager.window.Window;
import com.ssc14.handy.window_manager.window.access.Source;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Supplier;

/**
 * Объект, управляющий серверными представлениями окон и их синхронизацией с клиентом
 *
 * @author vladisfaer
 */
public final class ServerUIManager implements IWindowHandler {
    private static final Map<UUID, ModularUI> PLAYER_UIS = new HashMap<>();
    private static final Map<UUID, Map<Source, UIElement>> PLAYER_WINDOWS = new HashMap<>();
    private static final List<Supplier<? extends Window>> PERSISTENT_WINDOWS =
            new ArrayList<>();

    public static final ServerUIManager INSTANCE = new ServerUIManager();

    private ServerUIManager() {
        NeoForge.EVENT_BUS.post(
                new RegisterPersistentWindowsEvent(this)
        );
    }

    public static void create(ServerPlayer player) {
        Handy.LOGGER.debug("Handy Sync: Server MUI Created");
        remove(player);

        ModularUI mui = ModularUI.of(UI.of(new UIElement()), player);
        // Attach the root to the MUI without creating a menu.
        mui.setMenu(null);
        PLAYER_UIS.put(player.getUUID(), mui);
        openPersistentWindows(player);
    }

    public static ModularUI get(ServerPlayer player) {
        return PLAYER_UIS.get(player.getUUID());
    }

    @ApiStatus.Internal
    public static void tick(ServerPlayer player) {
        ModularUI mui = get(player);
        if (mui != null) {
            mui.tickServer();
        }
    }

    @ApiStatus.Internal
    public static void remove(ServerPlayer player) {
        UUID playerId = player.getUUID();
        PLAYER_WINDOWS.remove(playerId);
        ModularUI mui = PLAYER_UIS.remove(playerId);

        if (mui != null) {
            mui.ui.rootElement.clearAllChildren();
            mui.onRemoved();
        }
    }

    /**
     * Создаёт серверное представление окна без источника
     */
    public static void attach(ServerPlayer player, ModularUI windowUI) {
        attachInternal(player, null, windowUI.ui.rootElement);
    }

    /**
     * Создаёт серверное представление окна по источнику
     */
    public static void attach(ServerPlayer player, Source source, ModularUI windowUI) {
        if (source == null) {
            throw new IllegalArgumentException("Source is required for a sourced UI");
        }
        attachInternal(player, source, windowUI.ui.rootElement);
    }

    private static void attachInternal(ServerPlayer player, Source source, UIElement windowUI) {
        ModularUI main = get(player);
        if (main == null) {
            throw new IllegalStateException("No server MUI for player " + player.getUUID());
        }

        main.ui.rootElement.addChild(windowUI);

        if (source != null) {
            Map<Source, UIElement> windows = PLAYER_WINDOWS.computeIfAbsent(
                    player.getUUID(), ignored -> new HashMap<>()
            );
            UIElement previous = windows.put(source, windowUI);
            if (previous != null && previous != windowUI) {
                previous.removeSelf();
            }
        }
    }

    /**
     * Удаляет серверное представление окна источника и закрывает это окна на клиенте
     */
    public static boolean closeWindow(ServerPlayer player, Source source) {
        if (source == null) return false;

        Map<Source, UIElement> windows = PLAYER_WINDOWS.get(player.getUUID());
        if (windows == null) return false;

        UIElement element = windows.remove(source);
        if (element == null) return false;

        element.removeSelf();
        if (windows.isEmpty()) {
            PLAYER_WINDOWS.remove(player.getUUID());
        }

        PacketDistributor.sendToPlayer(player, new CloseWindowPayload(source));
        return true;
    }

    /**
     * Возвращает список источников, для которых есть открыто серверное представление окна
     * @param player игрок для которого ищутся окна
     */
    public static List<Source> getSources(ServerPlayer player) {
        Map<Source, UIElement> windows = PLAYER_WINDOWS.get(player.getUUID());
        return windows == null ? List.of() : new ArrayList<>(windows.keySet());
    }

    /**
     * Открывает серверное представление окна по его источнику
     * @param player игрок для которого открывается окно
     * @param source источник окна
     */
    public static void openUI(ServerPlayer player, Source source){
        ModularUI ui = switch (source.getType()){
            case ITEM_STACK -> {
                UUID id = source.getUuid();

                List<ItemStack> stacks = ServerItemRegistry.INSTANCE.get(id);
                if (stacks.isEmpty()) yield null;

                ItemStack stack = stacks.getFirst();
                if (!(stack.getItem() instanceof ItemUIProvider provider)) yield null;

                yield provider.getUI(id, player);
            }
            case ENTITY -> {
                UUID id = source.getUuid();

                Entity entity = findEntityByUuidEverywhere(id);
                if (!(entity instanceof EntityUIProvider provider)) yield null;

                yield provider.getUI(id, player);
            }
            case BLOCK_ENTITY -> {
                GlobalPos pos = source.getGlobalPos();

                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server == null) {
                    yield null;
                }
                BlockEntity blockEntity = Objects.requireNonNull(server.getLevel(pos.dimension())).getBlockEntity(pos.pos());
                if (!(blockEntity instanceof BlockEntityUIProvider provider)) yield null;

                yield provider.getUI(pos, player);
            }
        };
        attach(player, source, ui);
    }

    @ApiStatus.Internal
    public static void handleRPC(ServerPlayer player, byte[] eventData) {
        ModularUI mui = get(player);
        if (mui == null) return;

        ByteBufUtil.readCustomData(
                eventData,
                mui.syncManager::handEvent,
                player.registryAccess()
        );
    }

    @ApiStatus.Internal
    public static void handleSync(ServerPlayer player, byte[] data) {
        ModularUI mui = get(player);
        if (mui == null) return;

        ByteBufUtil.readCustomData(
                data,
                mui.syncManager::handleSyncPacket,
                player.registryAccess()
        );
    }

    /**
     * Ищет сущность во всех измерениях по её {@link UUID}
     * @param uuid {@link UUID} сущности
     * @return найденная сущность или {@code null} если ничего не найдено
     */
    public static Entity findEntityByUuidEverywhere(UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity != null){
                if (entity.getUUID() == uuid)
                    return entity;
            }
        }
        return null;
    }

    @Override
    public void registerPersistentWindow(Supplier<? extends Window> factory) {
        PERSISTENT_WINDOWS.add(Objects.requireNonNull(factory));
    }

    private static void openPersistentWindows(ServerPlayer player) {
        for (Supplier<? extends Window> factory : List.copyOf(PERSISTENT_WINDOWS)) {
            attachInternal(player, null, factory.get().getChildren().getFirst());
        }
    }
}
