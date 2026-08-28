package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.BlockContainerStyle;
import dev.vfyjxf.taffy.style.BlockItemStyle;
import dev.vfyjxf.taffy.style.Style;

/**
 * Block layout tree contract that retains an application's custom identifier type.
 *
 * @param <S> application-owned custom identifier type
 */
public interface GenericLayoutBlockContainer<S> extends GenericLayoutPartialTree<S>, LayoutBlockContainer {
    /** Returns the typed block container style. */
    Style<S> getGenericBlockContainerStyle(NodeId node);

    /** Returns the typed block item style. */
    Style<S> getGenericBlockChildStyle(NodeId node);

    @Override
    default BlockContainerStyle getBlockContainerStyle(NodeId node) {
        return getGenericBlockContainerStyle(node);
    }

    @Override
    default BlockItemStyle getBlockChildStyle(NodeId node) {
        return getGenericBlockChildStyle(node);
    }
}
