package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatSize;

/**
 * A struct containing the result of laying out a single node.
 *
 * @param size The size of the node
 * @param contentSize The size of the content within the node
 * @param firstBaselines The first baseline of the node in each dimension
 * @param topMargin Top margin that can collapse with another margin
 * @param bottomMargin Bottom margin that can collapse with another margin
 * @param marginsCanCollapseThrough Whether margins can collapse through this node
 */
public record LayoutOutput(
    FloatSize size,
    FloatSize contentSize,
    FloatPoint firstBaselines,
    CollapsibleMarginSet topMargin,
    CollapsibleMarginSet bottomMargin,
    boolean marginsCanCollapseThrough
) {

    /**
     * An all-zero LayoutOutput for hidden nodes.
     */
    public static final LayoutOutput HIDDEN = new LayoutOutput(
        FloatSize.zero(),
        FloatSize.zero(),
        new FloatPoint(Float.NaN, Float.NaN),
        CollapsibleMarginSet.ZERO,
        CollapsibleMarginSet.ZERO,
        false
    );

    /**
     * Static factory method for hidden layout output.
     */
    public static LayoutOutput hidden() {
        return HIDDEN;
    }

    /**
     * A blank layout output.
     */
    public static final LayoutOutput DEFAULT = HIDDEN;

    /**
     * Create a LayoutOutput from the size and baselines.
     */
    public static LayoutOutput fromSizesAndBaselines(
        FloatSize size,
        FloatSize contentSize,
        FloatPoint firstBaselines
    ) {
        return new LayoutOutput(
            size,
            contentSize,
            firstBaselines,
            CollapsibleMarginSet.ZERO,
            CollapsibleMarginSet.ZERO,
            false
        );
    }

    /**
     * Construct a LayoutOutput from container and content sizes.
     */
    public static LayoutOutput fromSizes(FloatSize size, FloatSize contentSize) {
        return fromSizesAndBaselines(size, contentSize, new FloatPoint(Float.NaN, Float.NaN));
    }

    /**
     * Construct a LayoutOutput from the container size.
     */
    public static LayoutOutput fromOuterSize(FloatSize size) {
        return fromSizes(size, FloatSize.zero());
    }

    @Override
    public String toString() {
        return "LayoutOutput{size=" + size + ", contentSize=" + contentSize + "}";
    }
}
