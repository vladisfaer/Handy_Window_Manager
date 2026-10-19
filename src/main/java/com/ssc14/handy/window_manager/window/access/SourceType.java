package com.ssc14.handy.window_manager.window.access;

/**
 * Объект представляющий тип источника окна
 *
 * @author vladisfaer
 */
public enum SourceType {
    ITEM_STACK(0),
    ENTITY(1),
    BLOCK_ENTITY(2);

    private final int id;

    SourceType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SourceType fromId(int id) {
        for (SourceType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown Handy source type: " + id);
    }
}
