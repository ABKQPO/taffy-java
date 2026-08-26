package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.AbsoluteAxis;

/**
 * An axis that layout algorithms can be requested to compute a size for.
 */
public enum RequestedAxis {
    /** The horizontal axis */
    HORIZONTAL,
    
    /** The vertical axis */
    VERTICAL,
    
    /** Both axes */
    BOTH;

    /**
     * Convert from AbsoluteAxis
     */
    public static RequestedAxis from(AbsoluteAxis axis) {
        return axis == AbsoluteAxis.HORIZONTAL ? HORIZONTAL : VERTICAL;
    }

    /**
     * Try to convert to AbsoluteAxis (returns null for BOTH)
     */
    public AbsoluteAxis toAbsoluteAxis() {
        switch (this) {
            case HORIZONTAL:
                return AbsoluteAxis.HORIZONTAL;
            case VERTICAL:
                return AbsoluteAxis.VERTICAL;
            case BOTH:
                return null;
            default:
                throw new IllegalStateException("Unexpected: " + this);
        }
    }
}
