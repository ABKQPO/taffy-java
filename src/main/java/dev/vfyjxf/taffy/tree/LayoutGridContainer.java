package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.GridContainerStyle;
import dev.vfyjxf.taffy.style.GridItemStyle;

/**
 * Style and diagnostic access contract required by the CSS Grid algorithm.
 */
public interface LayoutGridContainer extends LayoutPartialTree {
    /** Returns the styles of a Grid container node. */
    default GridContainerStyle getGridContainerStyle(NodeId node) {
        return getStyle(node);
    }

    /** Returns the styles of a Grid item node. */
    default GridItemStyle getGridChildStyle(NodeId node) {
        return getStyle(node);
    }

    /** Stores detailed grid information when the implementation supports it. */
    default void setDetailedGridInfo(NodeId node, DetailedGridInfo info) {
        setDetailedLayoutInfo(node, DetailedLayoutInfo.grid(info));
    }
}
