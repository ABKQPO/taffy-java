package dev.vfyjxf.taffy.tree;

/** Computes an uncached layout while retaining access to the caller-owned cache contract. */
@FunctionalInterface
public interface CachedLayoutComputeFunc<T extends CacheTree> {
    LayoutOutput compute(T tree, NodeId node, LayoutInput input);
}
