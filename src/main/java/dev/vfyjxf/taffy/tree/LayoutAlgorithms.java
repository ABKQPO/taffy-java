package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;

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

    public static LayoutOutput computeHiddenLayout(LayoutPartialTree tree, NodeId node) {
        return new LayoutComputer(tree, null).computeHiddenLayout(node);
    }

    public static LayoutOutput computeLeafLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeLeafLayout(node, inputs, tree.getStyle(node));
    }

    public static LayoutOutput computeBlockLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeBlockLayout(node, inputs, tree.getStyle(node));
    }

    public static LayoutOutput computeFlexboxLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeFlexboxLayout(node, inputs, tree.getStyle(node));
    }

    public static LayoutOutput computeGridLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeGridLayout(node, inputs, tree.getStyle(node));
    }
}
