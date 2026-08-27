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

    /** Returns the underlying distribution keyword. */
    public JustifyContent withoutSafety() {
        switch (this) {
            case SAFE_START: return START;
            case SAFE_END: return END;
            case SAFE_FLEX_START: return FLEX_START;
            case SAFE_FLEX_END: return FLEX_END;
            case SAFE_CENTER: return CENTER;
            default: return this;
        }
    }

    /** Returns whether this value has the CSS safe overflow modifier. */
    public boolean isSafe() {
        return this == SAFE_START || this == SAFE_END || this == SAFE_FLEX_START
            || this == SAFE_FLEX_END || this == SAFE_CENTER;
    }
}
