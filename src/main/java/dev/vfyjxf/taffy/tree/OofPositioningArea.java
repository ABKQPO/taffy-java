package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatSize;

/** The containing-block area used to resolve an out-of-flow descendant. */
public record OofPositioningArea(FloatSize size, FloatPoint offset) {
}
