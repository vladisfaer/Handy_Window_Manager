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
 * Server-side index of world ItemEntities by Handy item-stack instance ID.
 * All access is expected to happen on the logical server thread.
 */
@ApiStatus.Internal
@EventBusSubscriber(modid = Handy.MOD_ID)
public final class ItemEntityRegistry {
    private static final Map<UUID, ItemEntity> ENTITIES_BY_INSTANCE_ID = new HashMap<>();
    private static final Map<UUID, UUID> INSTANCE_IDS_BY_ENTITY_ID = new HashMap<>();

    private ItemEntityRegistry() {}

    /**
     * Returns the currently loaded ItemEntity for the given Handy instance ID,
     * or {@code null} if the ID has no currently loaded ItemEntity.
     */
    public static ItemEntity get(UUID instanceId) {
        if (instanceId == null) return null;

        ItemEntity entity = ENTITIES_BY_INSTANCE_ID.get(instanceId);
        if (entity == null) return null;

        // ItemEntity.setItem can replace the contents without a level event.
        // Repair the index on lookup if the entity no longer contains this ID.
        UUID currentInstanceId = getInstanceId(entity.getItem());
        if (!instanceId.equals(currentInstanceId)) {
            refresh(entity);
            return ENTITIES_BY_INSTANCE_ID.get(instanceId);
        }

        return entity;
    }

    /** Returns whether the registry has seen this ID, even if its value is null. */
    public static boolean hasEntry(UUID instanceId) {
        return instanceId != null && ENTITIES_BY_INSTANCE_ID.containsKey(instanceId);
    }

    /** Re-indexes an entity after its ItemStack changes. */
    public static void refresh(ItemEntity entity) {
        if (!(entity.level() instanceof ServerLevel)) return;

        untrack(entity);

        UUID instanceId = getInstanceId(entity.getItem());
        if (instanceId == null) return;

        ENTITIES_BY_INSTANCE_ID.put(instanceId, entity);
        INSTANCE_IDS_BY_ENTITY_ID.put(entity.getUUID(), instanceId);
    }

    /** Total number of instance IDs currently retained by the registry, including null entries. */
    public static int getEntryCount() {
        return ENTITIES_BY_INSTANCE_ID.size();
    }

    /** Number of entries whose value currently points to an ItemEntity. */
    public static int getLiveEntityCount() {
        int count = 0;
        for (ItemEntity entity : ENTITIES_BY_INSTANCE_ID.values()) {
            if (entity != null) count++;
        }
        return count;
    }

    /** Number of retained entries whose value is null. */
    public static int getNullEntryCount() {
        int count = 0;
        for (ItemEntity entity : ENTITIES_BY_INSTANCE_ID.values()) {
            if (entity == null) count++;
        }
        return count;
    }

    /**
     * Returns a read-only snapshot for debug commands. The map intentionally
     * permits null values, because null means the ID is known but not loaded.
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

        // The reverse index can be absent if this entity had no instance ID
        // when it joined, or if it was not indexed before this class loaded.
        if (instanceId == null) {
            instanceId = getInstanceId(entity.getItem());
        }

        // Do not clear a mapping that has since been replaced by another entity.
        if (instanceId != null && ENTITIES_BY_INSTANCE_ID.get(instanceId) == entity) {
            ENTITIES_BY_INSTANCE_ID.put(instanceId, null);
        }
    }

    private static UUID getInstanceId(ItemStack stack) {
        return stack.get(HandyComponents.INSTANCE_ID);
    }
}