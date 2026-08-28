package dev.vfyjxf.taffy.tree;

/**
 * Computes a block child while retaining the caller's concrete tree and shared formatting context.
 *
 * @param <T> caller-owned tree implementation
 */
@FunctionalInterface
public interface BlockLayoutComputeFunc<T extends LayoutBlockContainer> {
    LayoutOutput compute(T tree, NodeId node, LayoutInput input, BlockContext blockContext);
}
