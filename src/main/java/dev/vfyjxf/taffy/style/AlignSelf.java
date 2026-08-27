package dev.vfyjxf.taffy.style;

/**
 * Controls how an individual flex/grid item is aligned along the cross axis.
 */
public enum AlignSelf {
    /** Use the parent's align-items value */
    AUTO,
    /** Items are aligned at the start of the cross axis */
    FLEX_START,
    /** Items are aligned at the end of the cross axis */
    FLEX_END,
    /** Items are aligned at the start of the item's own writing direction */
    SELF_START,
    /** Items are aligned at the end of the item's own writing direction */
    SELF_END,
    /** Items are aligned at the center of the cross axis */
    CENTER,
    /** Items are aligned at the baseline */
    BASELINE,
    /** Items are stretched to fill the cross axis */
    STRETCH,
    /** Safe start alignment */
    SAFE_START,
    /** Safe end alignment */
    SAFE_END,
    /** Safe flex-start alignment */
    SAFE_FLEX_START,
    /** Safe flex-end alignment */
    SAFE_FLEX_END,
    /** Safe self-start alignment */
    SAFE_SELF_START,
    /** Safe self-end alignment */
    SAFE_SELF_END,
    /** Safe center alignment */
    SAFE_CENTER;

    /** Parse a CSS align-self value. */
    public static AlignSelf parse(String value) {
        return CssParser.parseAlignSelf(value);
    }

    /**
     * Creates an AlignSelf from an AlignItems value.
     */
    public static AlignSelf fromAlignItems(AlignItems alignItems) {
        if (alignItems == null || alignItems == AlignItems.AUTO) return STRETCH;
        return mapFromAlignItems(alignItems);
    }

    private static AlignSelf mapFromAlignItems(AlignItems alignItems) {
        boolean safe = alignItems.isSafe();
        return switch (alignItems.keyword()) {
            case FLEX_START -> safe ? SAFE_FLEX_START : FLEX_START;
            case START -> safe ? SAFE_START : FLEX_START;
            case FLEX_END -> safe ? SAFE_FLEX_END : FLEX_END;
            case END -> safe ? SAFE_END : FLEX_END;
            case SELF_START -> safe ? SAFE_SELF_START : SELF_START;
            case SELF_END -> safe ? SAFE_SELF_END : SELF_END;
            case CENTER -> safe ? SAFE_CENTER : CENTER;
            case BASELINE -> BASELINE;
            case STRETCH -> STRETCH;
        };
    }

    /** Returns the underlying alignment keyword. */
    public AlignItemsKeyword keyword() {
        return switch (this) {
            case FLEX_START, SAFE_FLEX_START -> AlignItemsKeyword.FLEX_START;
            case FLEX_END, SAFE_FLEX_END -> AlignItemsKeyword.FLEX_END;
            case SELF_START, SAFE_SELF_START -> AlignItemsKeyword.SELF_START;
            case SELF_END, SAFE_SELF_END -> AlignItemsKeyword.SELF_END;
            case CENTER, SAFE_CENTER -> AlignItemsKeyword.CENTER;
            case BASELINE -> AlignItemsKeyword.BASELINE;
            case STRETCH, AUTO -> AlignItemsKeyword.STRETCH;
            case SAFE_START -> AlignItemsKeyword.START;
            case SAFE_END -> AlignItemsKeyword.END;
            default -> throw new IllegalStateException("Unexpected align-self value: " + this);
        };
    }

    /** Returns whether this value has the CSS safe overflow modifier. */
    public boolean isSafe() {
        return this == SAFE_START || this == SAFE_END || this == SAFE_FLEX_START
            || this == SAFE_FLEX_END || this == SAFE_SELF_START || this == SAFE_SELF_END
            || this == SAFE_CENTER;
    }

    /** Removes the safe modifier while preserving the alignment position. */
    public AlignSelf withoutSafety() {
        return switch (this) {
            case SAFE_START -> FLEX_START;
            case SAFE_END -> FLEX_END;
            case SAFE_FLEX_START -> FLEX_START;
            case SAFE_FLEX_END -> FLEX_END;
            case SAFE_SELF_START -> SELF_START;
            case SAFE_SELF_END -> SELF_END;
            case SAFE_CENTER -> CENTER;
            default -> this;
        };
    }

    /**
     * Converts to AlignItems.
     */
    public AlignItems toAlignItems() {
        return switch (this) {
            case FLEX_START -> AlignItems.FLEX_START;
            case FLEX_END -> AlignItems.FLEX_END;
            case SELF_START -> AlignItems.SELF_START;
            case SELF_END -> AlignItems.SELF_END;
            case CENTER -> AlignItems.CENTER;
            case BASELINE -> AlignItems.BASELINE;
            case SAFE_START -> AlignItems.SAFE_START;
            case SAFE_END -> AlignItems.SAFE_END;
            case SAFE_FLEX_START -> AlignItems.SAFE_FLEX_START;
            case SAFE_FLEX_END -> AlignItems.SAFE_FLEX_END;
            case SAFE_SELF_START -> AlignItems.SAFE_SELF_START;
            case SAFE_SELF_END -> AlignItems.SAFE_SELF_END;
            case SAFE_CENTER -> AlignItems.SAFE_CENTER;
            default -> AlignItems.STRETCH;
        };
    }
}
