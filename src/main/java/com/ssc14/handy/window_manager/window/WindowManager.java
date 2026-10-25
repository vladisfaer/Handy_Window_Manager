package com.ssc14.handy.window_manager.window;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUIClientAccess;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUIWidget;
import com.lowdragmc.lowdraglib2.utils.ByteBufUtil;
import com.ssc14.handy.window_manager.api.IWindowHandler;
import com.ssc14.handy.window_manager.client.gui.ClientGuiModeEvents;
import com.ssc14.handy.window_manager.client.gui.GuiMode;
import com.ssc14.handy.window_manager.network.OpenWindowPayload;
import com.ssc14.handy.window_manager.registry.HandyComponents;
import com.ssc14.handy.window_manager.window.access.Source;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Главный класс отвечающий за управление окнами на стороне клиента
 *
 * @author vladisfaer
 */
public final class WindowManager implements IWindowHandler {
    /**
     * Экземпляр оконного менеджера
     */
    public static final WindowManager INSTANCE = new WindowManager();

    private final List<Window> windows = new ArrayList<>();
    private final Map<Source, Window> windowsBySource = new HashMap<>();

    private ModularUI mainUI;
    private Player owner;

    private int lastWidth = -1;
    private int lastHeight = -1;

    private Window focusedWindow;
    private int nextZIndex = 0;
    private Window draggingWindow;

    private final List<Supplier<? extends Window>> PERSISTENT_WINDOWS =
            new ArrayList<>();
    private boolean isReady = false;

    private WindowManager() {}

    /**
     * Возвращает окно, которое имеет фокус
     * <p>Если окон нет вернёт {@code null}
     */
    public Window getFocusedWindow() {
        return focusedWindow;
    }

    /**
     * Передаёт фокус выбранному окну
     *
     * @param window окно, которое получит фокус
     */
    public void focus(Window window) {
        if (!windows.contains(window) || focusedWindow == window) {
            return;
        }

        focusedWindow = window;
        window.style(style -> style.zIndex(++nextZIndex));
    }

    public void beginWindowDrag(Window window) {
        draggingWindow = window;
    }

    /**
     * Возвращает главный {@link ModularUI} на стороне клиента
     *
     * @return главный {@link ModularUI}
     */
    public ModularUI getMainUI() {
        return ensureReady() ? mainUI : null;
    }

    /**
     * Открывает окно из {@link ModularUI}, используя любой источник данных окна и стандартные параметры
     *
     * @param source источник данных окна
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @return созданное окно
     */
    public Window openFromSource(Source source, ModularUI modularUI){
        return openFromSource(source, modularUI, new WindowProperties());
    }

    /**
     * Открывает окно из {@link ModularUI} и параметров окна, используя любой источник данных окна
     *
     * @param source источник данных окна
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @param properties параметры окна
     * @return созданное окно
     */
    public Window openFromSource(Source source, ModularUI modularUI, WindowProperties properties) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(modularUI, "modularUI");

        Window existing = windowsBySource.get(source);
        if (existing != null && windows.contains(existing)) {
            focus(existing);
            modularUI.onRemoved();
            return existing;
        }

        Window window = openFromMUI(modularUI, properties);
        windowsBySource.put(source, window);
        ClientPacketDistributor.sendToServer(new OpenWindowPayload(source));
        return window;
    }

    /**
     * Открывает окно из {@link ModularUI}, используя {@link ItemStack} как источник данных
     *
     * @param itemStack предмет, представляющий данные окна
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @return созданное окно
     */
    public Window openFromItemStack(ItemStack itemStack, ModularUI modularUI){
        return openFromSource(Source.itemStack(itemStack.get(HandyComponents.INSTANCE_ID)), modularUI);
    }
    /**
     * Открывает окно из {@link ModularUI} и параметров окна, используя {@link ItemStack} как источник данных
     *
     * @param itemStack предмет, представляющий данные окна
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @return созданное окно
     */
    public Window openFromItemStack(ItemStack itemStack, ModularUI modularUI, WindowProperties properties){
        return openFromSource(Source.itemStack(itemStack.get(HandyComponents.INSTANCE_ID)), modularUI, properties);
    }

    /**
     * Открывает окно из {@link ModularUI}, используя {@link Entity} как источник данных
     *
     * @param entity сущность, представляющая данные окна
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @return созданное окно
     */
    public Window openFromEntity(Entity entity, ModularUI modularUI){
        return openFromSource(Source.entity(entity.getUUID()), modularUI);
    }
    /**
     * Открывает окно из {@link ModularUI} и параметров окна, используя {@link Entity} как источник данных
     *
     * @param entity сущность, представляющая данные окна
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @param properties параметры окна
     * @return созданное окно
     */
    public Window openFromEntity(Entity entity, ModularUI modularUI, WindowProperties properties){
        return openFromSource(Source.entity(entity.getUUID()), modularUI, properties);
    }

    /**
     * Открывает окно из {@link ModularUI}, используя {@link BlockEntity} как источник данных
     *
     * @param blockEntity блок, представляющий данные окна
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @return созданное окно
     */
    public Window openFromBlockEntity(BlockEntity blockEntity, ModularUI modularUI){
        return openFromBlockEntity(blockEntity, modularUI, new WindowProperties());
    }
    /**
     * Открывает окно из {@link ModularUI} и параметров окна, используя {@link BlockEntity} как источник данных окна
     *
     * @param blockEntity блок, представляющий данные окна
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @param properties параметры окна
     * @return созданное окно
     */
    public Window openFromBlockEntity(BlockEntity blockEntity, ModularUI modularUI, WindowProperties properties){
        assert blockEntity.getLevel() != null;
        return openFromSource(Source.blockEntity(new GlobalPos(blockEntity.getLevel().dimension(), blockEntity.getBlockPos())), modularUI, properties);
    }

    /**
     * Открывает окно из {@link ModularUI} без серверного источника окна
     *
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @return созданное окно
     */
    public Window openFromMUI(ModularUI modularUI) {
        Objects.requireNonNull(modularUI, "mui");
        return openFromMUI(modularUI, new WindowProperties());
    }
    /**
     * Открывает окно из {@link ModularUI} и параметров окна без серверного источника окна
     * 
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @param properties параметры окна
     * @return созданное окно
     */
    public Window openFromMUI(ModularUI modularUI, WindowProperties properties) {
        Objects.requireNonNull(modularUI, "mui");
        int index = windows.size();
        float x = (lastWidth - WindowProperties.DEFAULT_WIDTH) / 2f + index * 16;
        float y = (lastHeight - WindowProperties.DEFAULT_HEIGHT) / 2f + index * 16;
        Window window = createWindowFromMui(modularUI, x, y, properties);
        return openWindow(window);
    }

    /**
     * Создаёт окно из {@link ModularUI}
     * @param modularUI {@link ModularUI} который будет трансформирован
     * @param _x координата окна по горизонтали
     * @param _y координата окна по вертикали
     * @param properties параметры окна
     * @return созданное окно
     */
    public static Window createWindowFromMui(ModularUI modularUI, float _x, float _y, WindowProperties properties){
        Window window = new Window(modularUI, _x, _y, properties);
        modularUI.ui.getStylesheets().forEach(
                window::addLocalStylesheet
        );
        return window;
    }

    /**
     * Создаёт полноэкранное окно из {@link ModularUI}
     * @param modularUI {@link ModularUI} из которого будет сделано окно
     * @return созданное окно
     */
    public static Window createFullscreenWindowFromMui(ModularUI modularUI){
        return createWindowFromMui(modularUI, 0, 0, new WindowProperties().immovable().fullscreen());
    }

    /**
     * Открывает окно
     * @param window окно, которое будет открыто
     * @return открытое окно
     */
    public Window openWindow(Window window){
        if (!ensureReady()) {
            throw new IllegalStateException("Cannot open a window without a player");
        }
        mainUI.ui.rootElement.addChild(window);
        windows.add(window);
        focus(window);
        return window;
    }

    /**
     * Регистрирует постоянное окно
     *
     * <p>Постоянные окна - это окна, которые автоматически открываются при входе в игру и никогда не закрываются
     * @param factory Фабрика, предоставляющая окно
     */
    @ApiStatus.Internal
    @Override
    public void registerPersistentWindow(Supplier<? extends Window> factory) {
        PERSISTENT_WINDOWS.add(Objects.requireNonNull(factory));
    }

    /**
     * Закрывает переданное окно
     *
     * @param window окно, которое будет закрыто
     */
    public void closeWindow(Window window) {
        if (!windows.remove(window)) {
            return;
        }

        windowsBySource.values().removeIf(mappedWindow -> mappedWindow == window);
        window.removeSelf();
        window.notifyClosed();

        if (focusedWindow == window) {
            focusedWindow = null;
            if (!windows.isEmpty()) {
                focus(windows.getLast());
            }
        }
        if (draggingWindow == window) {
            draggingWindow = null;
        }
    }

    /**
     * Закрывает окно по его источнику
     *
     * @param source источник окна
     */
    public void closeWindow(Source source) {
        Window window = windowsBySource.get(source);
        if (window == null) {
            return;
        }
        closeWindow(window);
    }

    /**
     * Проверяет, открыто ли переданное окно
     * @param window проверяемое окно
     */
    public boolean isOpen(Window window) {
        return windows.contains(window);
    }

    private void openPersistentWindows() {
        for (Supplier<? extends Window> factory : List.copyOf(PERSISTENT_WINDOWS)) {
            openWindow(factory.get());
        }
    }

    @ApiStatus.Internal
    public void tick() {
        if (ensureReady()) {
            mainUI.tick();
        }
    }

    @ApiStatus.Internal
    public void render(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || !ensureReady()) {
            return;
        }

        for (Window window : windows) {
            window.keepOnScreen();
        }

        double mouseX = -1;
        double mouseY = -1;
        if (GuiMode.isActive()) {
            mouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());
            mouseY = mc.mouseHandler.getScaledYPos(mc.getWindow());
        }

        ModularUIWidget widget = ModularUIClientAccess.getWidget(mainUI);
        widget.extractRenderState(graphics, (int) mouseX, (int) mouseY, 0.0f);
        ClientGuiModeEvents.updateMouseMovement(mouseX, mouseY);
    }

    @ApiStatus.Internal
    public boolean mousePressed(MouseButtonEvent event) {
        if (!ensureReady()) {
            return false;
        }
        return ModularUIClientAccess.getWidget(mainUI).mouseClicked(event, false);
    }

    @ApiStatus.Internal
    public void mouseReleased(MouseButtonEvent event) {
        if (ensureReady()) {
            ModularUIClientAccess.getWidget(mainUI).mouseReleased(event);
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            draggingWindow = null;
        }
    }

    @ApiStatus.Internal
    public void mouseMoved(double mouseX, double mouseY) {
        if (ensureReady()) {
            ModularUIClientAccess.getWidget(mainUI).mouseMoved(mouseX, mouseY);
        }
    }

    @ApiStatus.Internal
    public void mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (draggingWindow != null) {
            draggingWindow.dragBy(deltaX, deltaY);
            return;
        }

        if (ensureReady()) {
            ModularUIClientAccess.getWidget(mainUI).mouseDragged(event, deltaX, deltaY);
        }
    }

    @ApiStatus.Internal
    public boolean ensureReady() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            discard();
            return false;
        }

        if (mainUI == null || owner != player) {
            discard();
            owner = player;
            mainUI = RootMUI.createUI(player);
            openPersistentWindows();
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        if (width != lastWidth || height != lastHeight) {
            mainUI.init(width, height);
            lastWidth = width;
            lastHeight = height;
        }
        isReady = true;
        return true;
    }

    @ApiStatus.Internal
    public void discard() {
        if (mainUI != null) {
            mainUI.onRemoved();
        }

        windows.clear();
        windowsBySource.clear();
        focusedWindow = null;
        draggingWindow = null;
        mainUI = null;
        owner = null;
        lastWidth = -1;
        lastHeight = -1;
        isReady = false;
    }

    @ApiStatus.Internal
    public void handleSync(Player player, byte[] data) {
        ModularUI mui = mainUI;
        if (mui == null) {
            return;
        }

        ByteBufUtil.readCustomData(
                data,
                mui.syncManager::handleSyncPacket,
                player.registryAccess()
        );
    }
}
