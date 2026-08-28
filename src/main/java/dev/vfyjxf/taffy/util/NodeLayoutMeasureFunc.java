package dev.vfyjxf.taffy.util;

import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.LayoutInput;
import dev.vfyjxf.taffy.tree.LayoutOutput;
import dev.vfyjxf.taffy.tree.NodeId;

/** Computes a complete leaf layout output while exposing node context and style. */
@FunctionalInterface
public interface NodeLayoutMeasureFunc<C> {
    LayoutOutput measure(LayoutInput input, NodeId node, C context, TaffyStyle style);
}
