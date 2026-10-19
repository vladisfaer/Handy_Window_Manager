package com.ssc14.handy.window_manager.item;

import com.ssc14.handy.window_manager.registry.HandyComponents;
import net.minecraft.world.item.ItemStack;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Реестр всех {@link ItemStack} имеющих окна
 *
 * <p>Отслеживает создание, копирование и удаление всех ItemStack с помощью миксина.
 * Хранит несколько {@link ItemStack} на каждый {@link UUID}, так как в работе с {@link ItemStack}
 * создаются технические копии, неотличимые от оригинала
 *
 * @author vladisfaer
 */
public final class ServerItemRegistry {
    /**
     * Экземпляр реестра
     */
    public static final ServerItemRegistry INSTANCE = new ServerItemRegistry();

    private final Map<UUID, Set<StackReference>> items = new HashMap<>();
    private final ReferenceQueue<ItemStack> queue = new ReferenceQueue<>();

    private ServerItemRegistry() {
    }

    public void register(ItemStack stack) {
        cleanup();

        UUID id = getId(stack);
        if (id == null || stack.isEmpty()) {
            return;
        }

        if (contains(id, stack)) {
            return;
        }

        items
                .computeIfAbsent(id, _ -> new HashSet<>())
                .add(new StackReference(id, stack, queue));
    }

    public void unregister(ItemStack stack) {
        cleanup();

        UUID id = getId(stack);
        if (id == null) {
            return;
        }

        Set<StackReference> references = items.get(id);
        if (references == null) {
            return;
        }

        references.removeIf(reference -> reference.get() == stack);

        if (references.isEmpty()) {
            items.remove(id);
        }
    }

    public boolean contains(UUID id) {
        cleanup();

        Set<StackReference> references = items.get(id);
        return references != null && !references.isEmpty();
    }

    public boolean contains(UUID id, ItemStack stack) {
        cleanup();

        Set<StackReference> references = items.get(id);
        if (references == null) {
            return false;
        }

        for (StackReference reference : references) {
            if (reference.get() == stack) {
                return true;
            }
        }

        return false;
    }

    /**
     * Получить предмет из реестра по его идентификатору
     * @param id идентификатор предмета
     * @return все {@link ItemStack} с соответствующим идентификатором
     */
    public List<ItemStack> get(UUID id) {
        cleanup();

        Set<StackReference> references = items.get(id);
        if (references == null) {
            return List.of();
        }

        List<ItemStack> result = new ArrayList<>();

        for (StackReference reference : references) {
            ItemStack stack = reference.get();

            if (stack != null) {
                result.add(stack);
            }
        }

        return List.copyOf(result);
    }

    public int size() {
        cleanup();
        return items.size();
    }

    private UUID getId(ItemStack stack) {
        return stack.get(HandyComponents.INSTANCE_ID);
    }

    private void cleanup() {
        StackReference reference;

        while ((reference = (StackReference) queue.poll()) != null) {
            Set<StackReference> references = items.get(reference.id);

            if (references == null) {
                continue;
            }

            references.remove(reference);

            if (references.isEmpty()) {
                items.remove(reference.id);
            }
        }
    }

    /**
     * Слабая ссылка на {@link ItemStack} хранящая также его {@link UUID}
     *
     * @author vladisfaer
     */
    private static final class StackReference extends WeakReference<ItemStack> {

        private final UUID id;

        private StackReference(
                UUID id,
                ItemStack stack,
                ReferenceQueue<ItemStack> queue
        ) {
            super(stack, queue);
            this.id = id;
        }
    }

    /**
     * Выполняет код для всех {@link ItemStack} имеющих данный идентификатор
     * @param uuid искомый идентификатор
     * @param action код, исполняемый для найденных {@link ItemStack}
     */
    public void execute(UUID uuid, java.util.function.Consumer<ItemStack> action) {
        cleanup();

        Set<StackReference> references = items.get(uuid);
        if (references == null || references.isEmpty()) {
            return;
        }

        // Снимок ссылок до выполнения лямбды.
        List<StackReference> snapshot = new ArrayList<>(references);

        for (StackReference reference : snapshot) {
            ItemStack stack = reference.get();

            if (stack != null) {
                action.accept(stack);
            }
        }
    }

    //==========================
    //         DEBUG ZONE
    //==========================

    public int getNonEmptySetCount() {
        cleanup();
        return items.size();
    }

    public Map<UUID, Integer> getLiveReferenceCounts() {
        cleanup();

        Map<UUID, Integer> result = new HashMap<>();

        for (Map.Entry<UUID, Set<StackReference>> entry : items.entrySet()) {
            int count = 0;

            for (StackReference reference : entry.getValue()) {
                if (reference.get() != null) {
                    count++;
                }
            }

            if (count > 0) {
                result.put(entry.getKey(), count);
            }
        }

        return result;
    }

    public String debugDump() {
        cleanup();

        StringBuilder result = new StringBuilder();

        result.append("ServerItemRegistry:\n");

        for (Map.Entry<UUID, Set<StackReference>> entry : items.entrySet()) {
            UUID id = entry.getKey();

            result.append(id).append(":\n");

            for (StackReference reference : entry.getValue()) {
                ItemStack stack = reference.get();

                if (stack == null) {
                    continue;
                }

                result
                        .append("  ItemStack@")
                        .append(Integer.toHexString(System.identityHashCode(stack)))
                        .append(" count=")
                        .append(stack.getCount())
                        .append('\n');
            }
        }

        return result.toString();
    }

    public Map<UUID, List<ItemStack>> getDebugEntries() {
        cleanup();

        Map<UUID, List<ItemStack>> result = new HashMap<>();

        for (var entry : items.entrySet()) {
            List<ItemStack> stacks = new ArrayList<>();

            for (StackReference reference : entry.getValue()) {
                ItemStack stack = reference.get();

                if (stack != null) {
                    stacks.add(stack);
                }
            }

            if (!stacks.isEmpty()) {
                result.put(entry.getKey(), List.copyOf(stacks));
            }
        }

        return Map.copyOf(result);
    }
}