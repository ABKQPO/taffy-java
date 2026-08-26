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
    /** Items are aligned at the center of the cross axis */
    CENTER,
    /** Items are aligned at the baseline */
    BASELINE,
    /** Items are stretched to fill the cross axis */
    STRETCH;

    /**
     * Creates an AlignSelf from an AlignItems value.
     */
    public static AlignSelf fromAlignItems(AlignItems alignItems) {
        if (alignItems == null || alignItems == AlignItems.AUTO) return STRETCH;
        return mapFromAlignItems(alignItems);
    }

    private static AlignSelf mapFromAlignItems(AlignItems alignItems) {
        switch (alignItems) {
            case FLEX_START:
            case START:
                return FLEX_START;
            case FLEX_END:
            case END:
                return FLEX_END;
            case CENTER:
                return CENTER;
            case BASELINE:
                return BASELINE;
            case STRETCH:
                return STRETCH;
            case AUTO:
                return STRETCH; // defensive (handled above)
        }
        throw new IllegalStateException("Unexpected: " + alignItems);
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
            case CENTER:
                return AlignItems.CENTER;
            case BASELINE:
                return AlignItems.BASELINE;
            default:
                return AlignItems.STRETCH;
        }
    }
}
