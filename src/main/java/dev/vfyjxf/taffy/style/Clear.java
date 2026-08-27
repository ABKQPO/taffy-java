package dev.vfyjxf.taffy.style;

import java.util.Locale;

/** Controls which preceding floats a block item must clear. */
public enum Clear {
    LEFT,
    RIGHT,
    BOTH,
    NONE;

    /** Returns true when this value clears at least one float side. */
    public boolean isClearing() {
        return this != NONE;
    }

    /** Returns whether a float on the selected side is cleared. */
    public boolean clears(TaffyFloat side) {
        if (side == TaffyFloat.LEFT) return this == LEFT || this == BOTH;
        if (side == TaffyFloat.RIGHT) return this == RIGHT || this == BOTH;
        return false;
    }

    /** Parse the CSS {@code clear} property. */
    public static Clear parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ParseError("Clear value must not be empty");
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "left" -> LEFT;
            case "right" -> RIGHT;
            case "both" -> BOTH;
            case "none" -> NONE;
            default -> throw new ParseError("Unknown clear keyword: " + value);
        };
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
