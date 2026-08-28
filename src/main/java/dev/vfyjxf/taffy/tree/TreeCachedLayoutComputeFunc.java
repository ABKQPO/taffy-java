package dev.vfyjxf.taffy.tree;

/** Computes an uncached layout with access to one concrete tree that also owns the cache. */
@FunctionalInterface
public interface TreeCachedLayoutComputeFunc<T extends LayoutPartialTree & CacheTree> {
    LayoutOutput compute(T tree, NodeId node, LayoutInput input);
}
