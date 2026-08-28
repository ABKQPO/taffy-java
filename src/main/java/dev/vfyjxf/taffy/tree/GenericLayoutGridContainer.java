package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.GridContainerStyle;
import dev.vfyjxf.taffy.style.GridItemStyle;
import dev.vfyjxf.taffy.style.Style;

/**
 * Grid tree contract that retains an application's custom identifier type.
 *
 * @param <S> application-owned custom identifier type
 */
public interface GenericLayoutGridContainer<S> extends GenericLayoutPartialTree<S>, LayoutGridContainer {
    /** Returns the typed Grid container style. */
    Style<S> getGenericGridContainerStyle(NodeId node);

    /** Returns the typed Grid item style. */
    Style<S> getGenericGridChildStyle(NodeId node);

    @Override
    default GridContainerStyle getGridContainerStyle(NodeId node) {
        return getGenericGridContainerStyle(node);
    }

    @Override
    default GridItemStyle getGridChildStyle(NodeId node) {
        return getGenericGridChildStyle(node);
    }

    /** Receives typed detailed Grid diagnostics after layout when the tree needs them. */
    default void setGenericDetailedGridInfo(NodeId node, GenericDetailedGridInfo<S> info) {
        setDetailedGridInfo(node, info.runtime());
    }
}
