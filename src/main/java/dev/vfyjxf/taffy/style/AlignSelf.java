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

    /**
     * Creates an AlignSelf from an AlignItems value.
     */
    public static AlignSelf fromAlignItems(AlignItems alignItems) {
        if (alignItems == null || alignItems == AlignItems.AUTO) return STRETCH;
        return mapFromAlignItems(alignItems);
    }

    private static AlignSelf mapFromAlignItems(AlignItems alignItems) {
        boolean safe = alignItems.isSafe();
        switch (alignItems.keyword()) {
            case FLEX_START:
                return safe ? SAFE_FLEX_START : FLEX_START;
            case START:
                return safe ? SAFE_START : FLEX_START;
            case FLEX_END:
                return safe ? SAFE_FLEX_END : FLEX_END;
            case END:
                return safe ? SAFE_END : FLEX_END;
            case SELF_START:
                return safe ? SAFE_SELF_START : SELF_START;
            case SELF_END:
                return safe ? SAFE_SELF_END : SELF_END;
            case CENTER:
                return safe ? SAFE_CENTER : CENTER;
            case BASELINE:
                return BASELINE;
            case STRETCH:
                return STRETCH;
        }
        throw new IllegalStateException("Unexpected: " + alignItems);
    }

    /** Returns the underlying alignment keyword. */
    public AlignItemsKeyword keyword() {
        switch (this) {
            case FLEX_START: case SAFE_FLEX_START: return AlignItemsKeyword.FLEX_START;
            case FLEX_END: case SAFE_FLEX_END: return AlignItemsKeyword.FLEX_END;
            case SELF_START: case SAFE_SELF_START: return AlignItemsKeyword.SELF_START;
            case SELF_END: case SAFE_SELF_END: return AlignItemsKeyword.SELF_END;
            case CENTER: case SAFE_CENTER: return AlignItemsKeyword.CENTER;
            case BASELINE: return AlignItemsKeyword.BASELINE;
            case STRETCH: case AUTO: return AlignItemsKeyword.STRETCH;
            case SAFE_START: return AlignItemsKeyword.START;
            case SAFE_END: return AlignItemsKeyword.END;
            default: throw new IllegalStateException("Unexpected align-self value: " + this);
        }
    }

    /** Returns whether this value has the CSS safe overflow modifier. */
    public boolean isSafe() {
        return this == SAFE_START || this == SAFE_END || this == SAFE_FLEX_START
            || this == SAFE_FLEX_END || this == SAFE_SELF_START || this == SAFE_SELF_END
            || this == SAFE_CENTER;
    }

    /** Removes the safe modifier while preserving the alignment position. */
    public AlignSelf withoutSafety() {
        switch (this) {
            case SAFE_START: return FLEX_START;
            case SAFE_END: return FLEX_END;
            case SAFE_FLEX_START: return FLEX_START;
            case SAFE_FLEX_END: return FLEX_END;
            case SAFE_SELF_START: return SELF_START;
            case SAFE_SELF_END: return SELF_END;
            case SAFE_CENTER: return CENTER;
            default: return this;
        }
    }

    /**
     * Converts to AlignItems.
     */
    public AlignItems toAlignItems() {
        switch (this) {
            case FLEX_START:
                return AlignItems.FLEX_START;
            case FLEX_END:
                return AlignItems.FLEX_END;
            case SELF_START:
                return AlignItems.SELF_START;
            case SELF_END:
                return AlignItems.SELF_END;
            case CENTER:
                return AlignItems.CENTER;
            case BASELINE:
                return AlignItems.BASELINE;
            case SAFE_START:
                return AlignItems.SAFE_START;
            case SAFE_END:
                return AlignItems.SAFE_END;
            case SAFE_FLEX_START:
                return AlignItems.SAFE_FLEX_START;
            case SAFE_FLEX_END:
                return AlignItems.SAFE_FLEX_END;
            case SAFE_SELF_START:
                return AlignItems.SAFE_SELF_START;
            case SAFE_SELF_END:
                return AlignItems.SAFE_SELF_END;
            case SAFE_CENTER:
                return AlignItems.SAFE_CENTER;
            default:
                return AlignItems.STRETCH;
        }
    }
}
