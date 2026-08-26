package dev.vfyjxf.taffy.tree;

/** Placement of one item in origin-zero grid line coordinates. */
public record DetailedGridItemInfo(
    NodeId node,
    int columnStart,
    int columnEnd,
    int rowStart,
    int rowEnd) {
}
