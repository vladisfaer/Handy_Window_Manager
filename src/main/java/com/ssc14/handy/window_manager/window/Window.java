package com.ssc14.handy.window_manager.window;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.ssc14.handy.window_manager.Handy;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;

/**
 * Окно для оконного менеджера
 *
 * <p>Содержит в себе дерево элементов изначального {@link ModularUI} и его стили
 *
 * @author vladisfaer
 */
public final class Window extends UIElement {
    public static final int DEFAULT_WIDTH = 200;
    public static final int DEFAULT_HEIGHT = 100;

    private float x;
    private float y;
    private float width;
    private float height;
    private final float minWidth;
    private final float minHeight;

    private ResizeType resizing = ResizeType.NONE;
    private final float resizeMarginX;
    private final float resizeMarginY;

    private final UIElement content;

    private static final float HANDLE_SIZE_PIXELS = 15;

    private Runnable closeCallback = () -> {};

    /**
     * Создаёт окно со стандартными параметрами из {@link ModularUI}
     *
     * @param mui {@link ModularUI}, из которого будет сделано окно
     * @param _x координата, на которой появится окно
     * @param _y координата, на которой появится окно
     */
    public Window(ModularUI mui, float _x, float _y) {
        this(mui, _x, _y, new WindowProperties());
    }

    /**
     * Создаёт окно с переданными параметрами из {@link ModularUI}
     *
     * @param mui {@link ModularUI}, из которого будет сделано окно
     * @param _x координата, на которой появится окно
     * @param _y координата, на которой появится окно
     * @param properties параметры окна
     */
    public Window(ModularUI mui, float _x, float _y, WindowProperties properties) {
        this.x = _x;
        this.y = _y;
        this.width = properties.width;
        this.height = properties.height;
        this.minWidth = properties.minWidth;
        this.minHeight = properties.minHeight;
        this.resizeMarginX = properties.resizeMarginX;
        this.resizeMarginY = properties.resizeMarginY;

        if(properties.fullscreen){
            x = 0;
            y = 0;
        }

        if(properties.autoSetup) {
            layout(layout -> layout
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(x)
                    .top(y)
                    .width(width)
                    .height(height));
            if (properties.fullscreen) {
                layout(layoutStyle -> layoutStyle
                        .widthPercent(100)
                        .heightPercent(100));
            }
        }

        content = mui.ui.rootElement;
        content.layout(layout -> layout
                .widthPercent(100)
                .heightPercent(100));

        addChild(content);

        addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 0) {
                return;
            }

            if (event.target != this && event.target != content) {
                return;
            }

            if (properties.immovable) return;
            WindowManager.INSTANCE.beginWindowDrag(this);
            computeResizeType(event.x, event.y);
            startDrag(null, null);
        });

        addEventListener(
                UIEvents.MOUSE_DOWN,
                _ -> WindowManager.INSTANCE.focus(this),
                true
        );
    }

    private void computeResizeType(float ex, float ey){
        int xi = Math.abs(ex - x) < resizeMarginX ? -1 : Math.abs(ex - x - width) < resizeMarginX ? 1 : 0;
        int yi = Math.abs(ey - y) < resizeMarginY ? 1 : Math.abs(ey - y - height) < resizeMarginY ? -1 : 0;
        resizing = ResizeType.from2i(yi, xi);
        Handy.LOGGER.debug("Resize Type: {}", resizing.toString());
    }

    private void clampPosition() {
        var window = Minecraft.getInstance().getWindow();

        int guiWidth = window.getGuiScaledWidth();
        int guiHeight = window.getGuiScaledHeight();
        int pixelWidth = window.getWidth();
        int pixelHeight = window.getHeight();

        if (guiWidth <= 0 || guiHeight <= 0
                || pixelWidth <= 0 || pixelHeight <= 0) {
            return;
        }

        float handleWidth =
                HANDLE_SIZE_PIXELS * guiWidth / pixelWidth;
        float handleHeight =
                HANDLE_SIZE_PIXELS * guiHeight / pixelHeight;

        float maxX = Math.max(0, guiWidth - handleWidth);
        float maxY = Math.max(0, guiHeight - handleHeight);

        x = Math.clamp(x, 0, maxX);
        y = Math.clamp(y, 0, maxY);
    }

    private void applyPosition() {
        layout(layout -> layout
                .left(this.x)
                .top(this.y));
    }

    /**
     * Возвращает окно на экран, если оно оказалось за его пределами
     */
    public void keepOnScreen() {
        float oldX = x;
        float oldY = y;

        clampPosition();

        if (oldX != x || oldY != y) {
            applyPosition();
        }
    }

    /**
     * Перемещает окно на заданные координаты
     */
    public void setPosition(float _x, float _y) {
        this.x = _x;
        this.y = _y;
        clampPosition();
        applyPosition();
    }

    void dragBy(double deltaX, double deltaY) {
        if (resizing == ResizeType.NONE) {
            setPosition(
                    x + (float) deltaX,
                    y + (float) deltaY
            );
        } else {
            float newX = x;
            float newY = y;
            float newWidth = width;
            float newHeight = height;
            if (resizing.isLeft()) newX += (float) deltaX;
            if (resizing.isHorizontal()) newWidth += ((float) deltaX) * (resizing.isLeft()?-1:1);
            if (resizing.isUp()) newY += (float) deltaY;
            if (resizing.isVertical()) newHeight += ((float) deltaY) * (resizing.isUp()?-1:1);
            if (newWidth < minWidth){
                if(resizing.isLeft()) newX += newWidth - minWidth;
                newWidth = minWidth;
            }
            if (newHeight < minHeight){
                if(resizing.isUp()) newY += newHeight - minHeight;
                newHeight = minHeight;
            }
            setSize(newWidth, newHeight);
            setPosition(newX, newY);
        }
    }

    /**
     * Устанавливает новый размер окна
     *
     * @param _width ширина
     * @param _height высота
     */
    public void setSize(float _width, float _height) {
        width = _width;
        height = _height;
        clampPosition();
        applyPosition();
        layout(layout -> layout
                .width(width)
                .height(height));
    }

    /**
     * Проверяет, находится ли мышь в окне
     *
     * @param mouseX координата X мыши на экране в пикселях
     * @param mouseY координата X мыши на экране в пикселях
     * @return находится ли мышь в окне
     */
    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    /**
     * Задаёт поведение окна при закрытии
     *
     * @param callback код, который выполнится при закрытии
     */
    public void setCloseCallback(Runnable callback) {
        closeCallback = callback;
    }

    void notifyClosed() {
        Runnable callback = closeCallback;
        closeCallback = () -> {};
        callback.run();
    }

    /**
     * Представляет направление, в котором в данный момент окно может менять размер
     *
     * @author vladisfaer
     */
    public enum ResizeType{
        UP_LEFT,
        UP,
        UP_RIGHT,
        LEFT,
        NONE,
        RIGHT,
        DOWN_LEFT,
        DOWN,
        DOWN_RIGHT;

        /**
         * Проверяет, может ли размер окна меняться по левому краю
         */
        public boolean isLeft(){
            return this == UP_LEFT || this == LEFT || this == DOWN_LEFT;
        }

        /**
         * Проверяет, может ли размер окна меняться по правому краю
         */
        public boolean isRight(){
            return this == UP_RIGHT || this == RIGHT || this == DOWN_RIGHT;
        }

        /**
         * Проверяет, может ли размер окна меняться по верхнему краю
         */
        public boolean isUp(){
            return this == UP_LEFT || this == UP || this == UP_RIGHT;
        }

        /**
         * Проверяет, может ли размер окна меняться по нижнему краю
         */
        public boolean isDown(){
            return this == DOWN_LEFT || this == DOWN || this == DOWN_RIGHT;
        }

        /**
         * Проверяет, может ли размер окна меняться по горизонтали
         */
        public boolean isHorizontal(){
            return isLeft() || isRight();
        }

        /**
         * Проверяет, может ли размер окна меняться по вертикали
         */
        public boolean isVertical(){
            return isDown() || isUp();
        }

        /**
         * Строит {@link ResizeType} по координатам перемещаемой точки
         *
         * <p> (-1,-1) - левый нижний угол
         * <p> (1, 1) - правый верхний угол
         *
         * @param y изменение по вертикали
         * @param x изменение по горизонтали
         */
        public static ResizeType from2i(int y, int x){
            return switch (-y*3+x+4){
                case 0 -> UP_LEFT;
                case 1 -> UP;
                case 2 -> UP_RIGHT;
                case 3 -> LEFT;
                case 4 -> NONE;
                case 5 -> RIGHT;
                case 6 -> DOWN_LEFT;
                case 7 -> DOWN;
                case 8 -> DOWN_RIGHT;
                default -> throw new IllegalStateException("unknown ResizeType x: "+x+" y: "+y);
            };
        }
    }
}