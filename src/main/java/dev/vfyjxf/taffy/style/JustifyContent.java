package dev.vfyjxf.taffy.style;

/**
 * Defines how content is distributed along the main axis in flexbox and grid.
 * <p>
 * Note: STRETCH has special meaning in CSS Grid - it expands auto tracks to fill free space.
 * In flexbox, STRETCH is equivalent to FLEX_START for justify-content.
 */
public enum JustifyContent {
    /** Items are packed at the start */
    FLEX_START,
    /** Items are packed at the end */
    FLEX_END,
    /** Items are centered */
    CENTER,
    /** Items are evenly distributed with space between */
    SPACE_BETWEEN,
    /** Items are evenly distributed with space around */
    SPACE_AROUND,
    /** Items are evenly distributed with equal space */
    SPACE_EVENLY,
    /** Items are packed at the start */
    START,
    /** Items are packed at the end */
    END,
    /**
     * Auto tracks are stretched to fill free space.
     * <p>
     * In CSS Grid: auto tracks expand to share remaining space equally.
     * In Flexbox: equivalent to FLEX_START (no effect on main axis distribution).
     */
    STRETCH,
    /** Safe start alignment. */
    SAFE_START,
    /** Safe end alignment. */
    SAFE_END,
    /** Safe flex-start alignment. */
    SAFE_FLEX_START,
    /** Safe flex-end alignment. */
    SAFE_FLEX_END,
    /** Safe center alignment. */
    SAFE_CENTER;

    /** Parse a CSS justify-content value. */
    public static JustifyContent parse(String value) {
        AlignContent parsed = CssParser.parseJustifyContent(value);
        boolean safe = parsed.isSafe();
        return switch (parsed.withoutSafety()) {
            case START -> safe ? SAFE_START : START;
            case END -> safe ? SAFE_END : END;
            case FLEX_START -> safe ? SAFE_FLEX_START : FLEX_START;
            case FLEX_END -> safe ? SAFE_FLEX_END : FLEX_END;
            case CENTER -> safe ? SAFE_CENTER : CENTER;
            case STRETCH -> STRETCH;
            case SPACE_BETWEEN -> SPACE_BETWEEN;
            case SPACE_EVENLY -> SPACE_EVENLY;
            case SPACE_AROUND -> SPACE_AROUND;
            case AUTO -> FLEX_START;
            default -> throw new IllegalStateException("Unexpected justify-content value: " + parsed);
        };
    }

    /** Returns the underlying distribution keyword. */
    public JustifyContent withoutSafety() {
        return switch (this) {
            case SAFE_START -> START;
            case SAFE_END -> END;
            case SAFE_FLEX_START -> FLEX_START;
            case SAFE_FLEX_END -> FLEX_END;
            case SAFE_CENTER -> CENTER;
            default -> this;
        };
    }

    /** Returns whether this value has the CSS safe overflow modifier. */
    public boolean isSafe() {
        return this == SAFE_START || this == SAFE_END || this == SAFE_FLEX_START
            || this == SAFE_FLEX_END || this == SAFE_CENTER;
    }
}
