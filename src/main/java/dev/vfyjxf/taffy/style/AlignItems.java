package dev.vfyjxf.taffy.style;

/**
 * Used to control how child nodes are aligned.
 * For Flexbox it controls alignment in the cross axis.
 * For Grid it controls alignment in the block axis.
 * 
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/align-items">MDN - align-items</a>
 */
public enum AlignItems {
    /**
     * Unspecified/auto value used as a non-null sentinel.
     * <p>
     * The effective behavior depends on the algorithm context (e.g. defaulting to STRETCH).
     */
    AUTO,

    /** Items are packed toward the start of the axis */
    START,
    
    /** Items are packed toward the end of the axis */
    END,
    
    /**
     * Items are packed towards the flex-relative start of the axis.
     * For flex containers with flex_direction RowReverse or ColumnReverse this is equivalent to End.
     * In all other cases it is equivalent to Start.
     */
    FLEX_START,
    
    /**
     * Items are packed towards the flex-relative end of the axis.
     * For flex containers with flex_direction RowReverse or ColumnReverse this is equivalent to Start.
     * In all other cases it is equivalent to End.
     */
    FLEX_END,

    /** Items are packed toward the start of the item's own writing direction. */
    SELF_START,

    /** Items are packed toward the end of the item's own writing direction. */
    SELF_END,
    
    /** Items are packed along the center of the cross axis */
    CENTER,
    
    /** Items are aligned such as their baselines align */
    BASELINE,
    
    /** Stretch to fill the container */
    STRETCH,

    /** Safe start alignment. */
    SAFE_START,
    /** Safe end alignment. */
    SAFE_END,
    /** Safe flex-start alignment. */
    SAFE_FLEX_START,
    /** Safe flex-end alignment. */
    SAFE_FLEX_END,
    /** Safe self-start alignment. */
    SAFE_SELF_START,
    /** Safe self-end alignment. */
    SAFE_SELF_END,
    /** Safe center alignment. */
    SAFE_CENTER;

    /** Returns the underlying position keyword. */
    public AlignItemsKeyword keyword() {
        return switch (this) {
            case START, SAFE_START -> AlignItemsKeyword.START;
            case END, SAFE_END -> AlignItemsKeyword.END;
            case FLEX_START, SAFE_FLEX_START -> AlignItemsKeyword.FLEX_START;
            case FLEX_END, SAFE_FLEX_END -> AlignItemsKeyword.FLEX_END;
            case SELF_START, SAFE_SELF_START -> AlignItemsKeyword.SELF_START;
            case SELF_END, SAFE_SELF_END -> AlignItemsKeyword.SELF_END;
            case CENTER, SAFE_CENTER -> AlignItemsKeyword.CENTER;
            case BASELINE -> AlignItemsKeyword.BASELINE;
            case STRETCH, AUTO -> AlignItemsKeyword.STRETCH;
        };
    }

    /** Returns whether this value has the CSS safe overflow modifier. */
    public boolean isSafe() {
        return switch (this) {
            case SAFE_START, SAFE_END, SAFE_FLEX_START, SAFE_FLEX_END, SAFE_SELF_START, SAFE_SELF_END, SAFE_CENTER ->
                    true;
            default -> false;
        };
    }

    /** Returns whether this value uses the item's own writing direction. */
    public boolean isSelfRelative() {
        return this == SELF_START || this == SELF_END || this == SAFE_SELF_START || this == SAFE_SELF_END;
    }

    /** Resolves self-relative alignment against item and container directions. */
    public AlignItems resolveSelfRelative(TaffyDirection itemDirection, TaffyDirection containerDirection,
                                          boolean inlineAxis) {
        if (!isSelfRelative()) return this;
        boolean flip = inlineAxis && itemDirection != containerDirection;
        if (this == SELF_START) return flip ? END : START;
        if (this == SELF_END) return flip ? START : END;
        if (this == SAFE_SELF_START) return flip ? SAFE_END : SAFE_START;
        return flip ? SAFE_START : SAFE_END;
    }

    /** Removes the safe modifier while preserving the alignment position. */
    public AlignItems withoutSafety() {
        return switch (this) {
            case SAFE_START -> START;
            case SAFE_END -> END;
            case SAFE_FLEX_START -> FLEX_START;
            case SAFE_FLEX_END -> FLEX_END;
            case SAFE_SELF_START -> SELF_START;
            case SAFE_SELF_END -> SELF_END;
            case SAFE_CENTER -> CENTER;
            default -> this;
        };
    }

    /** Parse a CSS align-items keyword. */
    public static AlignItems parse(String value) {
        return CssParser.parseAlignItems(value);
    }
}
