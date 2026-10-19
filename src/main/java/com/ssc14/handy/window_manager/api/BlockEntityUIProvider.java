package com.ssc14.handy.window_manager.api;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.ssc14.handy.window_manager.window.Window;
import com.ssc14.handy.window_manager.window.WindowManager;
import com.ssc14.handy.window_manager.window.WindowProperties;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Блок, предоставляющий окно для оконного менеджера
 *
 * @author vladisfaer
 */
public interface BlockEntityUIProvider {
    Map<GlobalPos, Window> openWindows = new HashMap<>();

    /**
     * Открывает окно для этого блока с помощью {@link WindowManager}
     *
     * @param blockEntity блок, представляющий данные этого окна
     * @param player игрок, открывающий окно
     */
    default void openUI(BlockEntity blockEntity, Player player) {
        if (blockEntity.getLevel() == null) return;
        GlobalPos globalPos = new GlobalPos(blockEntity.getLevel().dimension(), blockEntity.getBlockPos());

        Window existing = openWindows.get(globalPos);

        if (existing != null && WindowManager.INSTANCE.isOpen(existing)) {
            WindowManager.INSTANCE.focus(existing);
            return;
        }

        openWindows.remove(globalPos);

        Window window = WindowManager.INSTANCE.fromBlockEntity(blockEntity, getUI(globalPos, player), getWindowProperties());

        openWindows.put(globalPos, window);
        window.setCloseCallback(() -> openWindows.remove(globalPos, window));
    }

    /**
     * Проверяет, имеет ли доступ к этому блоку
     *
     * @param globalPos позиция проверяемого блока
     */
    default boolean hasAccess(Player player, GlobalPos globalPos) {
        if (globalPos == null) return false;

        if (player.getAbilities().instabuild) {
            return true;
        }

        return canInteractWithBlockEntity(player, globalPos);
    }

    private boolean canInteractWithBlockEntity(@NonNull Player player, @NonNull GlobalPos globalPos) {
        Level level = player.level();

        if (!level.dimension().equals(globalPos.dimension())) {
            return false;
        }

        Vec3 eyePos = player.getEyePosition();
        Vec3 target = Vec3.atCenterOf(globalPos.pos());

        double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        if (eyePos.distanceToSqr(target) > reach * reach) {
            return false;
        }

        ClipContext ctx = new ClipContext(
                eyePos,
                target,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );
        BlockHitResult hit = level.clip(ctx);

        return hit.getBlockPos().equals(globalPos.pos());
    }

    /**
     * Создаёт {@link ModularUI} готовый к развёртыванию в виде окна
     *
     * @param globalPos позиция {@link BlockEntity}, представляющего данные этого окна
     * @param player игрок, открывающий окно
     * @return готовый {@link ModularUI}
     */
    ModularUI getUI(GlobalPos globalPos, Player player);

    /**
     * Предоставляет параметры окна для этого блока
     *
     * @return экземпляр параметров окна
     */
    default WindowProperties getWindowProperties(){
        return new WindowProperties();
    }
}
