package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.BlockContainerStyle;
import dev.vfyjxf.taffy.style.BlockItemStyle;

/**
 * Style access contract required by the CSS Block layout algorithm.
 */
public interface LayoutBlockContainer extends LayoutPartialTree {
    /** Returns the styles of a Block container node. */
    default BlockContainerStyle getBlockContainerStyle(NodeId node) {
        return getStyle(node);
    }

    /** Returns the styles of a Block item node. */
    default BlockItemStyle getBlockChildStyle(NodeId node) {
        return getStyle(node);
    }

    /** Computes a block child through the tree's layout dispatcher. */
    default LayoutOutput computeBlockChildLayout(NodeId node, LayoutInput inputs) {
        return computeChildLayout(node, inputs);
    }

    /**
     * Computes a child that remains in its parent's block formatting context.
     *
     * <p>Implementations that own recursive layout dispatch can override this method to consume
     * the context. The default preserves the existing child-dispatch behavior.</p>
     */
    default LayoutOutput computeBlockChildLayout(NodeId node, LayoutInput inputs, BlockContext blockContext) {
        return computeBlockChildLayout(node, inputs);
    }
}
