package dev.vfyjxf.taffy.util;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.Style;
import dev.vfyjxf.taffy.tree.NodeId;

/** Measures a typed tree node without erasing its application-owned grid identifier type. */
@FunctionalInterface
public interface GenericNodeMeasureFunc<S, C> {
    /** Computes the intrinsic size for a node with its typed style and application context. */
    FloatSize measure(
        FloatSize knownDimensions,
        TaffySize<AvailableSpace> availableSpace,
        NodeId node,
        C context,
        Style<S> style
    );
}
