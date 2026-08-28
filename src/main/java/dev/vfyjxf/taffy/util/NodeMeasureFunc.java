package dev.vfyjxf.taffy.util;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;

/** Measures a node while exposing its stored context and style. */
@FunctionalInterface
public interface NodeMeasureFunc<C> {
    /** Computes the intrinsic size for a node. */
    FloatSize measure(
        FloatSize knownDimensions,
        TaffySize<AvailableSpace> availableSpace,
        NodeId node,
        C context,
        TaffyStyle style
    );
}
