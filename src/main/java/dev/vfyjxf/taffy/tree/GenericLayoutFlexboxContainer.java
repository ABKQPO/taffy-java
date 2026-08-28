package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.FlexboxContainerStyle;
import dev.vfyjxf.taffy.style.FlexboxItemStyle;
import dev.vfyjxf.taffy.style.Style;

/**
 * Flexbox tree contract that retains an application's custom grid identifier type.
 * Runtime layout consumes the normalized style only at the algorithm boundary.
 *
 * @param <S> application-owned custom identifier type
 */
public interface GenericLayoutFlexboxContainer<S> extends GenericLayoutPartialTree<S>, LayoutFlexboxContainer {
    /** Returns the typed Flexbox container style. */
    Style<S> getGenericFlexboxContainerStyle(NodeId node);

    /** Returns the typed Flexbox item style. */
    Style<S> getGenericFlexboxChildStyle(NodeId node);

    @Override
    default FlexboxContainerStyle getFlexboxContainerStyle(NodeId node) {
        return getGenericFlexboxContainerStyle(node);
    }

    @Override
    default FlexboxItemStyle getFlexboxChildStyle(NodeId node) {
        return getGenericFlexboxChildStyle(node);
    }
}
