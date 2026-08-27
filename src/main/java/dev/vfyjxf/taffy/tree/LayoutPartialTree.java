package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.util.MeasureFunc;

/**
 * Data access contract required by Taffy's low-level layout algorithms.
 * Implementations can use any node storage strategy and need only expose direct children.
 */
public interface LayoutPartialTree extends TraversePartialTree, CacheTree {
    TaffyStyle getStyle(NodeId node);

    void setUnroundedLayout(NodeId node, Layout layout);

    Layout getUnroundedLayout(NodeId node);

    MeasureFunc getMeasureFunc(NodeId node);

    LayoutOutput getCacheEntry(NodeId node, LayoutInput input);

    void storeCacheEntry(NodeId node, LayoutInput input, LayoutOutput output);

    void clearCache(NodeId node);

    @Override
    default LayoutOutput cacheGet(NodeId node, LayoutInput input) {
        return getCacheEntry(node, input);
    }

    @Override
    default void cacheStore(NodeId node, LayoutInput input, LayoutOutput output) {
        storeCacheEntry(node, input, output);
    }

    @Override
    default void cacheClear(NodeId node) {
        clearCache(node);
    }

    /**
     * Compute a child through the standard low-level dispatcher.
     * Trees with specialized child dispatch can override this method.
     */
    default LayoutOutput computeChildLayout(NodeId node, LayoutInput inputs) {
        return LayoutAlgorithms.computeCachedLayout(this, node, inputs);
    }

    default NodeId getParent(NodeId node) {
        return null;
    }

    default void setDetailedLayoutInfo(NodeId node, DetailedLayoutInfo info) {
    }
}
