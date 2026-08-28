package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.CoreStyle;
import dev.vfyjxf.taffy.style.Style;

/**
 * Low-level tree contract that preserves an application's custom grid identifier type.
 * The layout engine normalizes identifiers only at the runtime style boundary.
 *
 * @param <S> application-owned custom identifier type
 */
public interface GenericLayoutPartialTree<S> extends LayoutPartialTree {
    /** Returns the typed style owned by the application for a node. */
    Style<S> getGenericStyle(NodeId node);

    @Override
    default CoreStyle getCoreContainerStyle(NodeId node) {
        return getGenericStyle(node);
    }
}
