package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.util.MeasureFunc;

/**
 * Data access contract required by Taffy's low-level layout algorithms.
 * Implementations can use any node storage strategy and need only expose direct children.
 */
public interface LayoutPartialTree extends TraversePartialTree {
    TaffyStyle getStyle(NodeId node);

    void setUnroundedLayout(NodeId node, Layout layout);

    Layout getUnroundedLayout(NodeId node);

    MeasureFunc getMeasureFunc(NodeId node);

    LayoutOutput getCacheEntry(
        NodeId node,
        FloatSize knownDimensions,
        TaffySize<AvailableSpace> availableSpace,
        RunMode runMode,
        RequestedAxis axis,
        TaffySize<Boolean> knownDimensionsAreDefinite);

    void storeCacheEntry(
        NodeId node,
        FloatSize knownDimensions,
        TaffySize<AvailableSpace> availableSpace,
        RunMode runMode,
        RequestedAxis axis,
        TaffySize<Boolean> knownDimensionsAreDefinite,
        LayoutOutput output);

    void clearCache(NodeId node);

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
