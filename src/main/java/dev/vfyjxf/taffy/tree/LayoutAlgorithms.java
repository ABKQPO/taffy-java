package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.CalcValueResolver;
import dev.vfyjxf.taffy.util.MeasureFunc;
import dev.vfyjxf.taffy.util.GenericNodeMeasureFunc;
import dev.vfyjxf.taffy.util.NodeMeasureFunc;
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

    /** Computes a root layout with a callback that can inspect application node context. */
    public static LayoutOutput computeRootLayout(
        LayoutPartialTree tree,
        NodeId root,
        TaffySize<AvailableSpace> availableSpace,
        NodeMeasureFunc<?> measureFunc) {
        return new LayoutComputer(tree, null, measureFunc).computeLayoutWithOutput(root, availableSpace);
    }

    /**
     * Computes a root layout through the caller's concrete tree dispatcher.
     * The callback owns recursive child dispatch and cache policy while this entry point retains
     * Taffy's root sizing and final root-layout writeback behavior.
     */
    public static <T extends LayoutPartialTree> LayoutOutput computeRootLayout(
        T tree,
        NodeId root,
        TaffySize<AvailableSpace> availableSpace,
        TreeLayoutComputeFunc<T> compute) {
        if (tree == null) throw new IllegalArgumentException("tree must not be null");
        if (compute == null) throw new IllegalArgumentException("compute must not be null");
        return new LayoutComputer(tree, null, null, (node, input) -> compute.compute(tree, node, input))
            .computeLayoutWithOutput(root, availableSpace);
    }

    /** Computes a typed tree root while retaining generic styles in its measurement callback. */
    public static <S, C> LayoutOutput computeRootLayout(
        GenericLayoutPartialTree<S> tree,
        NodeId root,
        TaffySize<AvailableSpace> availableSpace,
        GenericNodeMeasureFunc<S, C> measureFunc) {
        if (measureFunc == null) throw new IllegalArgumentException("measureFunc must not be null");
        NodeMeasureFunc<C> adapter = (knownDimensions, measuredAvailableSpace, measuredNode, context, ignoredStyle) ->
            measureFunc.measure(knownDimensions, measuredAvailableSpace, measuredNode, context,
                tree.getGenericStyle(measuredNode));
        return new LayoutComputer(tree, null, adapter).computeLayoutWithOutput(root, availableSpace);
    }

    public static LayoutOutput computeCachedLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeChildLayout(node, inputs);
    }

    /** Applies a caller-supplied uncached computation while preserving the concrete tree type. */
    public static <T extends LayoutPartialTree> LayoutOutput computeUncachedLayout(
        T tree,
        NodeId node,
        LayoutInput inputs,
        TreeLayoutComputeFunc<T> compute) {
        if (tree == null) throw new IllegalArgumentException("tree must not be null");
        if (compute == null) throw new IllegalArgumentException("compute must not be null");
        return compute.compute(tree, node, inputs);
    }

    /**
     * Applies a caller-supplied layout computation through an independent cache.
     * This supports external node stores that own dispatch while reusing Taffy's cache contract.
     */
    public static LayoutOutput computeCachedLayout(
        CacheTree cache,
        NodeId node,
        LayoutInput inputs,
        LayoutComputeFunc compute) {
        LayoutOutput cached = cache.cacheGet(node, inputs);
        if (cached != null) {
            return cached;
        }
        LayoutOutput output = compute.compute(node, inputs);
        cache.cacheStore(node, inputs, output);
        return output;
    }

    /**
     * Applies a caller-owned cache and passes the concrete cache implementation to the computation callback.
     * This is the Java equivalent of Rust's generic cache-tree dispatch contract.
     */
    public static <T extends CacheTree> LayoutOutput computeCachedLayout(
        T cache,
        NodeId node,
        LayoutInput inputs,
        CachedLayoutComputeFunc<T> compute) {
        LayoutOutput cached = cache.cacheGet(node, inputs);
        if (cached != null) {
            return cached;
        }
        LayoutOutput output = compute.compute(cache, node, inputs);
        cache.cacheStore(node, inputs, output);
        return output;
    }

    /** Applies a caller-owned tree and cache through a callback that retains the concrete tree type. */
    public static <T extends LayoutPartialTree & CacheTree> LayoutOutput computeCachedLayout(
        T tree,
        NodeId node,
        LayoutInput inputs,
        TreeCachedLayoutComputeFunc<T> compute) {
        LayoutOutput cached = tree.cacheGet(node, inputs);
        if (cached != null) {
            return cached;
        }
        LayoutOutput output = compute.compute(tree, node, inputs);
        tree.cacheStore(node, inputs, output);
        return output;
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

    /** Computes a leaf with a caller-supplied measurement callback. */
    public static LayoutOutput computeLeafLayout(
        LayoutPartialTree tree,
        NodeId node,
        LayoutInput inputs,
        MeasureFunc measureFunc) {
        return new LayoutComputer(tree, null).computeLeafLayout(node, inputs, tree.getStyle(node), measureFunc);
    }

    /** Computes a leaf using a caller-owned calc resolver and intrinsic measurement callback. */
    public static LayoutOutput computeLeafLayout(
        LayoutPartialTree tree,
        NodeId node,
        LayoutInput inputs,
        CalcValueResolver calcValueResolver,
        MeasureFunc measureFunc) {
        if (calcValueResolver == null) throw new IllegalArgumentException("calcValueResolver must not be null");
        if (measureFunc == null) throw new IllegalArgumentException("measureFunc must not be null");
        return new LayoutComputer(tree, null, null, null, null, calcValueResolver)
            .computeLeafLayout(node, inputs, tree.getStyle(node), measureFunc);
    }

    /** Computes a leaf with a context-aware caller-supplied measurement callback. */
    public static LayoutOutput computeLeafLayout(
        LayoutPartialTree tree,
        NodeId node,
        LayoutInput inputs,
        NodeMeasureFunc<?> measureFunc) {
        return new LayoutComputer(tree, null, measureFunc).computeLeafLayout(node, inputs, tree.getStyle(node));
    }

    /** Computes a leaf while retaining the typed style of a generic low-level tree in its callback. */
    public static <S, C> LayoutOutput computeLeafLayout(
        GenericLayoutPartialTree<S> tree,
        NodeId node,
        LayoutInput inputs,
        GenericNodeMeasureFunc<S, C> measureFunc) {
        if (measureFunc == null) throw new IllegalArgumentException("measureFunc must not be null");
        NodeMeasureFunc<C> adapter = (knownDimensions, availableSpace, measuredNode, context, ignoredStyle) ->
            measureFunc.measure(knownDimensions, availableSpace, measuredNode, context,
                tree.getGenericStyle(measuredNode));
        return new LayoutComputer(tree, null, adapter).computeLeafLayout(node, inputs, tree.getStyle(node));
    }

    public static LayoutOutput computeBlockLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeBlockLayout(node, inputs, tree.getStyle(node));
    }

    /** Computes a block layout through the specialized container contract. */
    public static LayoutOutput computeBlockLayout(LayoutBlockContainer tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeBlockLayout(node, inputs, tree.getBlockContainerStyle(node).toTaffyStyle());
    }

    /** Computes a block container while the caller owns all descendant layout dispatch. */
    public static <T extends LayoutBlockContainer> LayoutOutput computeBlockLayout(
        T tree,
        NodeId node,
        LayoutInput inputs,
        TreeLayoutComputeFunc<T> compute) {
        return dispatchingComputer(tree, compute)
            .computeBlockLayout(node, inputs, tree.getBlockContainerStyle(node).toTaffyStyle());
    }

    /**
     * Computes a block container while the caller owns layout for children in the shared block
     * formatting context.
     */
    public static <T extends LayoutBlockContainer> LayoutOutput computeBlockLayout(
        T tree,
        NodeId node,
        LayoutInput inputs,
        BlockLayoutComputeFunc<T> compute) {
        return blockDispatchingComputer(tree, null, compute)
            .computeBlockLayout(node, inputs, tree.getBlockContainerStyle(node).toTaffyStyle());
    }

    /**
     * Computes a block container while the caller owns both regular and shared-context child
     * dispatch.
     */
    public static <T extends LayoutBlockContainer> LayoutOutput computeBlockLayout(
        T tree,
        NodeId node,
        LayoutInput inputs,
        TreeLayoutComputeFunc<T> compute,
        BlockLayoutComputeFunc<T> blockCompute) {
        return blockDispatchingComputer(tree, compute, blockCompute)
            .computeBlockLayout(node, inputs, tree.getBlockContainerStyle(node).toTaffyStyle());
    }

    public static LayoutOutput computeFlexboxLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeFlexboxLayout(node, inputs, tree.getStyle(node));
    }

    /** Computes a Flexbox layout through the specialized container contract. */
    public static LayoutOutput computeFlexboxLayout(LayoutFlexboxContainer tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeFlexboxLayout(node, inputs, tree.getFlexboxContainerStyle(node).toTaffyStyle());
    }

    /** Computes a Flexbox container while the caller owns all descendant layout dispatch. */
    public static <T extends LayoutFlexboxContainer> LayoutOutput computeFlexboxLayout(
        T tree,
        NodeId node,
        LayoutInput inputs,
        TreeLayoutComputeFunc<T> compute) {
        return dispatchingComputer(tree, compute)
            .computeFlexboxLayout(node, inputs, tree.getFlexboxContainerStyle(node).toTaffyStyle());
    }

    public static LayoutOutput computeGridLayout(LayoutPartialTree tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeGridLayout(node, inputs, tree.getStyle(node));
    }

    /** Computes a Grid layout through the specialized container contract. */
    public static LayoutOutput computeGridLayout(LayoutGridContainer tree, NodeId node, LayoutInput inputs) {
        return new LayoutComputer(tree, null).computeGridLayout(node, inputs, tree.getGridContainerStyle(node).toTaffyStyle());
    }

    /** Computes a grid container while the caller owns all descendant layout dispatch. */
    public static <T extends LayoutGridContainer> LayoutOutput computeGridLayout(
        T tree,
        NodeId node,
        LayoutInput inputs,
        TreeLayoutComputeFunc<T> compute) {
        return dispatchingComputer(tree, compute)
            .computeGridLayout(node, inputs, tree.getGridContainerStyle(node).toTaffyStyle());
    }

    private static <T extends LayoutPartialTree> LayoutComputer dispatchingComputer(
        T tree,
        TreeLayoutComputeFunc<T> compute) {
        if (tree == null) throw new IllegalArgumentException("tree must not be null");
        if (compute == null) throw new IllegalArgumentException("compute must not be null");
        return new LayoutComputer(tree, null, null, (node, input) -> compute.compute(tree, node, input));
    }

    private static <T extends LayoutBlockContainer> LayoutComputer blockDispatchingComputer(
        T tree,
        TreeLayoutComputeFunc<T> compute,
        BlockLayoutComputeFunc<T> blockCompute) {
        if (tree == null) throw new IllegalArgumentException("tree must not be null");
        if (blockCompute == null) throw new IllegalArgumentException("blockCompute must not be null");
        LayoutComputeFunc dispatcher = compute == null ? null : (node, input) -> compute.compute(tree, node, input);
        BlockLayoutComputeFunc<LayoutBlockContainer> blockDispatcher =
            (ignoredTree, node, input, blockContext) -> blockCompute.compute(tree, node, input, blockContext);
        return new LayoutComputer(tree, null, null, null, dispatcher, blockDispatcher, null);
    }
}
