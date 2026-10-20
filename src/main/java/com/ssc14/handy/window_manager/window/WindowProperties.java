package com.ssc14.handy.window_manager.window;

/**
 * Параметры окна, используемые при его создании
 *
 * @author vladisfaer
 */
public class WindowProperties {
    public static final int DEFAULT_WIDTH = 100;
    public static final int DEFAULT_HEIGHT = 100;
    /**
     * Минимальная ширина окна
     */
    public int minWidth = DEFAULT_WIDTH;
    /**
     * Минимальная высота окна
     */
    public int minHeight = DEFAULT_HEIGHT;
    /**
     * Ширина окна в момент создания
     */
    public int width = DEFAULT_WIDTH;
    /**
     * Высота окна в момент создания
     */
    public int height = DEFAULT_HEIGHT;
    /**
     * Отступ по горизонтали для изменения размера окна
     */
    public int resizeMarginX = 0;
    /**
     * Отступ по вертикали для изменения размера окна
     */
    public int resizeMarginY = 0;
    /**
     * Недвижимое ли окно
     */
    public boolean immovable = false;
    /**
     * Занимает ли окно весь экран
     * <p>Рекомендуется использовать с {@code immovable = true;}
     */
    public boolean fullscreen = false;

    /**
     * Будет ли автоматически настроено позиционирование окна
     */
    public boolean autoSetup = true;

    /**
     * Устанавливает начальную ширину окна
     * @param _width новая ширина
     * @return Изменённые параметры окна
     */
    public WindowProperties width(int _width){
        this.width = _width;
        return this;
    }

    /**
     * Устанавливает начальную высоту окна
     * @param _height новая высота
     * @return Изменённые параметры окна
     */
    public WindowProperties height(int _height){
        this.height = _height;
        return this;
    }

    /**
     * Устанавливает минимальную ширину окна
     * @param _minWidth новая минимальная ширина
     * @return Изменённые параметры окна
     */
    public WindowProperties minWidth(int _minWidth){
        this.minWidth = _minWidth;
        return this;
    }

    /**
     * Устанавливает минимальную высоту окна
     * @param _minHeight новая минимальная высота
     * @return Изменённые параметры окна
     */
    public WindowProperties minHeight(int _minHeight){
        this.minHeight = _minHeight;
        return this;
    }

    /**
     * Устанавливает отступ для изменения размера по горизонтали
     * @param _resizeMarginX новый отступ
     * @return Изменённые параметры окна
     */
    public WindowProperties resizeMarginX(int _resizeMarginX){
        this.resizeMarginX = _resizeMarginX;
        return this;
    }

    /**
     * Устанавливает отступ для изменения размера по вертикали
     * @param _resizeMarginY новый отступ
     * @return Изменённые параметры окна
     */
    public WindowProperties resizeMarginY(int _resizeMarginY){
        this.resizeMarginY = _resizeMarginY;
        return this;
    }

    /**
     * Устанавливает отступ для изменения размера
     * @param _resizeMargin новый отступ
     * @return Изменённые параметры окна
     */
    public WindowProperties resizeMargin(int _resizeMargin){
        this.resizeMarginX = _resizeMargin;
        this.resizeMarginY = _resizeMargin;
        return this;
    }

    /**
     * Делает окно недвижимым
     * @return Изменённые параметры окна
     */
    public WindowProperties immovable(){
        return immovable(true);
    }

    /**
     * Переключает недвижимость окна
     * @param value будет ли окно недвижимым
     * @return Изменённые параметры окна
     */
    public WindowProperties immovable(boolean value){
        this.immovable = value;
        return this;
    }

    /**
     * Делает окно полноэкранным
     * @return Изменённые параметры окна
     */
    public WindowProperties fullscreen(){
        return fullscreen(true);
    }

    /**
     * Переключает полноэкранность окна
     * @param value будет ли окно полноэкранным
     * @return Изменённые параметры окна
     */
    public WindowProperties fullscreen(boolean value){
        this.fullscreen = value;
        return this;
    }

    /**
     * Переключает автоматическую настройку позиционирования окна
     * @param _autoSetup включать ли автоматическую настройку
     * @return Изменённые параметры окна
     */
    public WindowProperties autoSetup(boolean _autoSetup){
        this.autoSetup = _autoSetup;
        return this;
    }
}
