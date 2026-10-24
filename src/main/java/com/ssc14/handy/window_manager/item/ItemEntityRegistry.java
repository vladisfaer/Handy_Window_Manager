package com.ssc14.handy.window_manager.item;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.ssc14.handy.window_manager.Handy;
import com.ssc14.handy.window_manager.registry.HandyComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Реестр всех {@link ItemEntity}
 *
 * <p>Отслеживает появление и уничтожение {@link ItemEntity} с помощью встроенных событий майнкрафта.
 *
 * @author vladisfaer
 */
@ApiStatus.Internal
@EventBusSubscriber(modid = Handy.MOD_ID)
public final class ItemEntityRegistry {
    private static final Map<UUID, ItemEntity> ENTITIES_BY_INSTANCE_ID = new HashMap<>();
    private static final Map<UUID, UUID> INSTANCE_IDS_BY_ENTITY_ID = new HashMap<>();

    private ItemEntityRegistry() {}

    /**
     * Получает {@link ItemEntity} по его uuid
     * @param instanceId uuid искомого {@link ItemEntity}
     * @return {@link ItemEntity} с соответствующим uuid
     */
    public static ItemEntity get(UUID instanceId) {
        if (instanceId == null) return null;

        ItemEntity entity = ENTITIES_BY_INSTANCE_ID.get(instanceId);
        if (entity == null) return null;

        UUID currentInstanceId = getInstanceId(entity.getItem());
        if (!instanceId.equals(currentInstanceId)) {
            refresh(entity);
            return ENTITIES_BY_INSTANCE_ID.get(instanceId);
        }

        return entity;
    }

    /**
     * Проверяет наличие {@link ItemEntity} с данным uuid в реестре
     * @param instanceId uuid искомого {@link ItemEntity}
     * @return наличие в реестре
     */
    public static boolean hasEntry(UUID instanceId) {
        return instanceId != null && ENTITIES_BY_INSTANCE_ID.containsKey(instanceId);
    }

    /**
     * Обновляет данные в реестре об {@link ItemEntity}
     * @param entity сущность, данные которой в реестре обновляются
     */
    public static void refresh(ItemEntity entity) {
        if (!(entity.level() instanceof ServerLevel)) return;

        untrack(entity);

        UUID instanceId = getInstanceId(entity.getItem());
        if (instanceId == null) return;

        ENTITIES_BY_INSTANCE_ID.put(instanceId, entity);
        INSTANCE_IDS_BY_ENTITY_ID.put(entity.getUUID(), instanceId);
    }

    /**
     * Возвращает количество записей об ItemEntity в реестре
     */
    public static int getEntryCount() {
        return ENTITIES_BY_INSTANCE_ID.size();
    }

    /**
     * Возвращает количество реальных ItemEntity в реестре
     */
    public static int getLiveEntityCount() {
        int count = 0;
        for (ItemEntity entity : ENTITIES_BY_INSTANCE_ID.values()) {
            if (entity != null) count++;
        }
        return count;
    }

    /**
     * Возвращает количество {@code null} записей в реестре
     */
    public static int getNullEntryCount() {
        int count = 0;
        for (ItemEntity entity : ENTITIES_BY_INSTANCE_ID.values()) {
            if (entity == null) count++;
        }
        return count;
    }

    /**
     * Возвращает полный список записей для отладки
     */
    public static Map<UUID, ItemEntity> getDebugEntries() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(ENTITIES_BY_INSTANCE_ID));
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            refresh(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            untrack(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ENTITIES_BY_INSTANCE_ID.clear();
        INSTANCE_IDS_BY_ENTITY_ID.clear();
    }

    private static void untrack(ItemEntity entity) {
        UUID instanceId = INSTANCE_IDS_BY_ENTITY_ID.remove(entity.getUUID());

        if (instanceId == null) {
            instanceId = getInstanceId(entity.getItem());
        }

        if (instanceId != null && ENTITIES_BY_INSTANCE_ID.get(instanceId) == entity) {
            ENTITIES_BY_INSTANCE_ID.put(instanceId, null);
        }
    }

    private static UUID getInstanceId(ItemStack stack) {
        return stack.get(HandyComponents.INSTANCE_ID);
    }
}