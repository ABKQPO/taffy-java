package dev.vfyjxf.taffy.style;

import java.util.Locale;

/** Controls whether a block item is floated within its block formatting context. */
public enum TaffyFloat {
    LEFT,
    RIGHT,
    NONE;

    /** Returns true when this value creates a floated box. */
    public boolean isFloated() {
        return this != NONE;
    }

    /** Converts this value to a resolved direction, or null for {@link #NONE}. */
    public FloatDirection floatDirection() {
        switch (this) {
            case LEFT: return FloatDirection.LEFT;
            case RIGHT: return FloatDirection.RIGHT;
            default: return null;
        }
    }

    /** Parse the CSS {@code float} property. */
    public static TaffyFloat parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Float value must not be empty");
        }
        switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "left": return LEFT;
            case "right": return RIGHT;
            case "none": return NONE;
            default: throw new IllegalArgumentException("Unknown float keyword: " + value);
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
