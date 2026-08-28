package dev.vfyjxf.taffy.tree;

/** Computes one layout operation while retaining the caller's concrete tree type. */
@FunctionalInterface
public interface TreeLayoutComputeFunc<T extends LayoutPartialTree> {
    LayoutOutput compute(T tree, NodeId node, LayoutInput input);
}
