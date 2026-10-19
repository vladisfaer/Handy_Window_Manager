package com.ssc14.handy.window_manager.api;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.ssc14.handy.window_manager.window.Window;
import com.ssc14.handy.window_manager.window.WindowManager;
import com.ssc14.handy.window_manager.window.WindowProperties;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Сущность, предоставляющая окно для оконного менеджера
 *
 * @author vladisfaer
 */
public interface EntityUIProvider {
    Map<UUID, Window> openWindows = new HashMap<>();

    /**
     * Открывает окно для этой сущности с помощью {@link WindowManager}
     *
     * @param entity сущность, представляющая данные этого окна
     * @param player игрок, открывающий окно
     */
    default void openUI(Entity entity, Player player) {
        UUID id = entity.getUUID();

        Window existing = openWindows.get(id);

        if (existing != null && WindowManager.INSTANCE.isOpen(existing)) {
            WindowManager.INSTANCE.focus(existing);
            return;
        }

        openWindows.remove(id);

        Window window = WindowManager.INSTANCE.fromEntity(entity, getUI(id, player), getWindowProperties());

        openWindows.put(id, window);
        window.setCloseCallback(() -> openWindows.remove(id, window));
    }

    /**
     * Проверяет, имеет ли доступ к этой сущности
     *
     * @param entity проверяемая сущность
     */
    default boolean hasAccess(Player player, Entity entity) {
        if (entity == null) return false;

        if (player.getAbilities().instabuild) {
            return true;
        }

        return canInteractWithEntity(player, entity);
    }

    private boolean canInteractWithEntity(@NonNull Player player, Entity entity){
        double reach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        return player.distanceToSqr(entity) <= reach * reach && player.hasLineOfSight(entity);
    }

    /**
     * Создаёт {@link ModularUI} готовый к развёртыванию в виде окна
     *
     * @param uuid uuid сущности, представляющей данные этого окна
     * @param player игрок, открывающий окно
     * @return готовый {@link ModularUI}
     */
    ModularUI getUI(UUID uuid, Player player);

    /**
     * Предоставляет параметры окна для этой сущности
     *
     * @return экземпляр параметров окна
     */
    default WindowProperties getWindowProperties(){
        return new WindowProperties();
    }
}
