package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.util.RoundLayout;

/** Standalone low-level entry points corresponding to Rust Taffy's compute module. */
public class LayoutAlgorithms {
    private LayoutAlgorithms() {
    }

    public static void computeRootLayout(
        LayoutPartialTree tree,
        NodeId root,
        TaffySize<AvailableSpace> availableSpace) {
        new LayoutComputer(tree, null).computeLayout(root, availableSpace);
    }

    public static LayoutOutput computeCachedLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeChildLayout(node, inputs);
    }

    /** Round unrounded layouts for a fully traversable tree. */
    public static void roundLayout(RoundTree tree, NodeId node) {
        RoundLayout.roundLayout(tree, node);
    }

    public static LayoutOutput computeHiddenLayout(LayoutPartialTree tree, NodeId node) {
        return new LayoutComputer(tree, null).computeHiddenLayout(node);
    }

    public static LayoutOutput computeLeafLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeLeafLayout(node, inputs, tree.getStyle(node));
    }

    public static LayoutOutput computeBlockLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeBlockLayout(node, inputs, tree.getStyle(node));
    }

    /** Computes a block layout through the specialized container contract. */
    public static LayoutOutput computeBlockLayout(LayoutBlockContainer tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeBlockLayout(node, inputs, tree.getBlockContainerStyle(node));
    }

    public static LayoutOutput computeFlexboxLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeFlexboxLayout(node, inputs, tree.getStyle(node));
    }

    /** Computes a Flexbox layout through the specialized container contract. */
    public static LayoutOutput computeFlexboxLayout(LayoutFlexboxContainer tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeFlexboxLayout(node, inputs, tree.getFlexboxContainerStyle(node));
    }

    public static LayoutOutput computeGridLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeGridLayout(node, inputs, tree.getStyle(node));
    }

    /** Computes a Grid layout through the specialized container contract. */
    public static LayoutOutput computeGridLayout(LayoutGridContainer tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeGridLayout(node, inputs, tree.getGridContainerStyle(node));
    }
}
