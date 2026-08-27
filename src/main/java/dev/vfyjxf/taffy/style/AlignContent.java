package dev.vfyjxf.taffy.style;

/**
 * Sets the distribution of space between and around content items.
 * For Flexbox it controls alignment in the cross axis.
 * For Grid it controls alignment in the block axis.
 * 
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/align-content">MDN - align-content</a>
 */
public enum AlignContent {
    /**
     * Unspecified/auto value used as a non-null sentinel.
     * <p>
     * The effective behavior depends on the algorithm context (e.g. grid defaults to STRETCH).
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
    
    /** Items are centered around the middle of the axis */
    CENTER,
    
    /** Items are stretched to fill the container */
    STRETCH,
    
    /**
     * The first and last items are aligned flush with the edges of the container (no gap).
     * The gap between items is distributed evenly.
     */
    SPACE_BETWEEN,
    
    /**
     * The gap between the first and last items is exactly THE SAME as the gap between items.
     * The gaps are distributed evenly.
     */
    SPACE_EVENLY,
    
    /**
     * The gap between the first and last items is exactly HALF the gap between items.
     * The gaps are distributed evenly in proportion to these ratios.
     */
    SPACE_AROUND,

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

    /** Returns the underlying position keyword. */
    public AlignContentKeyword keyword() {
        switch (this) {
            case START: case SAFE_START: return AlignContentKeyword.START;
            case END: case SAFE_END: return AlignContentKeyword.END;
            case FLEX_START: case SAFE_FLEX_START: return AlignContentKeyword.FLEX_START;
            case FLEX_END: case SAFE_FLEX_END: return AlignContentKeyword.FLEX_END;
            case CENTER: case SAFE_CENTER: return AlignContentKeyword.CENTER;
            case STRETCH: case AUTO: return AlignContentKeyword.STRETCH;
            case SPACE_BETWEEN: return AlignContentKeyword.SPACE_BETWEEN;
            case SPACE_EVENLY: return AlignContentKeyword.SPACE_EVENLY;
            case SPACE_AROUND: return AlignContentKeyword.SPACE_AROUND;
            default: throw new IllegalStateException("Unexpected align-content value: " + this);
        }
    }

    /** Returns whether this value has the CSS safe overflow modifier. */
    public boolean isSafe() {
        return this == SAFE_START || this == SAFE_END || this == SAFE_FLEX_START
            || this == SAFE_FLEX_END || this == SAFE_CENTER;
    }

    /** Removes the safe modifier while preserving the alignment position. */
    public AlignContent withoutSafety() {
        switch (this) {
            case SAFE_START: return START;
            case SAFE_END: return END;
            case SAFE_FLEX_START: return FLEX_START;
            case SAFE_FLEX_END: return FLEX_END;
            case SAFE_CENTER: return CENTER;
            default: return this;
        }
    }

    /** Parse a CSS align-content keyword. */
    public static AlignContent parse(String value) {
        return CssParser.parseAlignContent(value);
    }
}
