package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.FlexboxContainerStyle;
import dev.vfyjxf.taffy.style.FlexboxItemStyle;

/**
 * Style access contract required by the Flexbox layout algorithm.
 *
 * <p>Implementations may return specialized style views; the default maps both
 * container and item accessors to the existing {@link TaffyStyle} value.</p>
 */
public interface LayoutFlexboxContainer extends LayoutPartialTree {
    /** Returns the styles of a Flexbox container node. */
    default FlexboxContainerStyle getFlexboxContainerStyle(NodeId node) {
        return getStyle(node);
    }

    /** Returns the styles of a Flexbox item node. */
    default FlexboxItemStyle getFlexboxChildStyle(NodeId node) {
        return getStyle(node);
    }
}
