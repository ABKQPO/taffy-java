package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.CalcExpression;
import dev.vfyjxf.taffy.style.CoreStyle;
import dev.vfyjxf.taffy.util.MeasureFunc;

/**
 * Data access contract required by Taffy's low-level layout algorithms.
 * Implementations can use any node storage strategy and need only expose direct children.
 */
public interface LayoutPartialTree extends TraversePartialTree {
    /**
     * Returns the complete runtime style view for a node.
     *
     * <p>Existing integrations can override this method. New integrations can instead override
     * {@link #getCoreContainerStyle(NodeId)} and keep their native style representation.</p>
     */
    default TaffyStyle getStyle(NodeId node) {
        return getCoreContainerStyle(node).toTaffyStyle();
    }

    /** Returns the application-owned style view required by common layout operations. */
    default CoreStyle getCoreContainerStyle(NodeId node) {
        throw new UnsupportedOperationException("Tree must provide getStyle or getCoreContainerStyle");
    }

    void setUnroundedLayout(NodeId node, Layout layout);

    Layout getUnroundedLayout(NodeId node);

    default MeasureFunc getMeasureFunc(NodeId node) {
        return null;
    }

    /** Returns arbitrary user context associated with a node, when supported. */
    default <C> C getNodeContext(NodeId node) {
        return null;
    }

    /** Resolve a calc expression through the tree's application-owned value model. */
    default float resolveCalcValue(CalcExpression expression, float basis) {
        return expression.resolve(basis);
    }

    /** Retrieves an entry when this tree also implements {@link CacheTree}. */
    default LayoutOutput getCacheEntry(NodeId node, LayoutInput input) {
        return this instanceof CacheTree cacheTree ? cacheTree.cacheGet(node, input) : null;
    }

    /** Stores an entry when this tree also implements {@link CacheTree}. */
    default void storeCacheEntry(NodeId node, LayoutInput input, LayoutOutput output) {
        if (this instanceof CacheTree cacheTree) {
            cacheTree.cacheStore(node, input, output);
        }
    }

    /** Clears entries when this tree also implements {@link CacheTree}. */
    default void clearCache(NodeId node) {
        if (this instanceof CacheTree cacheTree) {
            cacheTree.cacheClear(node);
        }
    }

    /** Returns this node's cache when the tree owns per-node caches. */
    default LayoutCache getCache(NodeId node) {
        return null;
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
