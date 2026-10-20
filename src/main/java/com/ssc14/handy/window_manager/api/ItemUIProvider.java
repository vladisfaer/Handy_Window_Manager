package com.ssc14.handy.window_manager.api;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.ssc14.handy.window_manager.item.ItemEntityRegistry;
import com.ssc14.handy.window_manager.item.ServerItemRegistry;
import com.ssc14.handy.window_manager.registry.HandyComponents;
import com.ssc14.handy.window_manager.window.Window;
import com.ssc14.handy.window_manager.window.WindowManager;
import com.ssc14.handy.window_manager.window.WindowProperties;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.EffectiveSide;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Предмет, предоставляющий окно для оконного менеджера
 *
 * @author vladisfaer
 */
public abstract class ItemUIProvider extends Item implements ItemStackLifecycle {
    private final Map<UUID, Window> openWindows = new HashMap<>();

    public ItemUIProvider(Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * Открывает окно этого предмета с помощью {@link WindowManager}
     *
     * @param stack предмет, представляющий данные этого окна
     * @param player игрок, открывающий окно
     */
    public void openUI(ItemStack stack, Player player) {
        UUID instanceId = stack.get(HandyComponents.INSTANCE_ID);

        WindowManager manager = WindowManager.INSTANCE;
        Window existing = openWindows.get(instanceId);

        if (existing != null && manager.isOpen(existing)) {
            manager.focus(existing);
            return;
        }

        openWindows.remove(instanceId);

        Window window = WindowManager.INSTANCE.openFromItemStack(stack, getUI(instanceId, player), getWindowProperties());

        openWindows.put(instanceId, window);
        window.setCloseCallback(() -> openWindows.remove(instanceId, window));
    }

    /**
     * Проверяет, имеет ли игрок доступ к этому предмету
     *
     * @param instanceId uuid проверяемого предмета
     */
    public boolean hasAccess(Player player, UUID instanceId) {
        if (instanceId == null) return false;

        // 1. The exact instance is present in the player's inventory.
        if (player.getInventory().hasAnyMatching(stack -> instanceId.equals(
                stack.get(HandyComponents.INSTANCE_ID)))) {
            return true;
        }

        // Include equipped slots; in current Minecraft versions equipment is stored separately.
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipped = player.getItemBySlot(slot);
            if (instanceId.equals(equipped.get(HandyComponents.INSTANCE_ID))) {
                return true;
            }
        }

        // 2. Creative mode grants access regardless of physical location.
        if (player.getAbilities().instabuild) {
            return true;
        }

        // 3. If the source is a dropped item, require entity interaction reach and line of sight.
        ItemEntity itemEntity = ItemEntityRegistry.get(instanceId);
        if (itemEntity == null || itemEntity.level() != player.level()) {
            return false;
        }

        if (!canInteractWithEntity(player, itemEntity)) {
            return false;
        }
        if (!player.hasLineOfSight(itemEntity)) {
            return false;
        }

        // 4. A future check for currently opened modded inventories belongs here.
        return true;
    }

    private boolean canInteractWithEntity(Player player, ItemEntity itemEntity){
        double reach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        return player.distanceToSqr(itemEntity) <= reach * reach && player.hasLineOfSight(itemEntity);
    }

    @Override
    public void handy$onItemStackDestroyed(ItemStack stack) {
        if (EffectiveSide.get().isServer()) {
            ServerItemRegistry.INSTANCE.unregister(stack);
        }
    }

    @Override
    public void handy$onItemStackCreated(ItemStack stack) {
        UUID id = stack.get(HandyComponents.INSTANCE_ID);

        if (id == null) {
            stack.set(HandyComponents.INSTANCE_ID, UUID.randomUUID());
        }

        if (EffectiveSide.get().isServer()) {
            ServerItemRegistry.INSTANCE.register(stack);
        }
    }

    /**
     * Создаёт {@link ModularUI} готовый к развёртыванию в виде окна
     *
     * @param uuid uuid предмета, представляющего данные этого окна
     * @param player игрок, открывающий окно
     * @return готовый {@link ModularUI}
     */
    public abstract ModularUI getUI(UUID uuid, Player player);

    /**
     * Предоставляет параметры окна для этого предмета
     *
     * @return экземпляр параметров окна
     */
    public WindowProperties getWindowProperties(){
        return new WindowProperties();
    }
}