package dev.vfyjxf.taffy.tree;

/** Computes one uncached child layout for the low-level cache adapter. */
@FunctionalInterface
public interface LayoutComputeFunc {
    /** Computes the layout output for the supplied node and constraints. */
    LayoutOutput compute(NodeId node, LayoutInput input);
}
