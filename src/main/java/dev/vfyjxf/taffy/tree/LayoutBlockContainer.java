package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.TaffyStyle;

/**
 * Style access contract required by the CSS Block layout algorithm.
 */
public interface LayoutBlockContainer extends LayoutPartialTree {
    /** Returns the styles of a Block container node. */
    default TaffyStyle getBlockContainerStyle(NodeId node) {
        return getStyle(node);
    }

    /** Returns the styles of a Block item node. */
    default TaffyStyle getBlockChildStyle(NodeId node) {
        return getStyle(node);
    }

    /** Computes a block child through the tree's layout dispatcher. */
    default LayoutOutput computeBlockChildLayout(NodeId node, LayoutInput inputs) {
        return computeChildLayout(node, inputs);
    }
}
