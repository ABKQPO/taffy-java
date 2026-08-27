package dev.vfyjxf.taffy.tree;

/** Cache access contract used by low-level layout algorithms. */
public interface CacheTree {
    /** Retrieve a cached layout result for the supplied input. */
    LayoutOutput cacheGet(NodeId node, LayoutInput input);

    /** Store a layout result under the supplied input. */
    void cacheStore(NodeId node, LayoutInput input, LayoutOutput output);

    /** Clear all cached results for a node. */
    void cacheClear(NodeId node);
}
