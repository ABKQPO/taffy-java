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
        return switch (this) {
            case LEFT -> FloatDirection.LEFT;
            case RIGHT -> FloatDirection.RIGHT;
            default -> null;
        };
    }

    /** Parse the CSS {@code float} property. */
    public static TaffyFloat parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ParseError("Float value must not be empty");
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "left" -> LEFT;
            case "right" -> RIGHT;
            case "none" -> NONE;
            default -> throw new ParseError("Unknown float keyword: " + value);
        };
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
