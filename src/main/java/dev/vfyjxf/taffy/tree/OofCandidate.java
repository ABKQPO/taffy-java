package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.style.TaffyPosition;

/** A pending absolute or fixed descendant emitted by a layout algorithm. */
public record OofCandidate(NodeId node, int order, TaffyPosition position, FloatPoint staticPosition) {
}
