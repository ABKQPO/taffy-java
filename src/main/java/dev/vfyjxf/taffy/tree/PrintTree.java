package dev.vfyjxf.taffy.tree;

/** Tree contract required to write a debug representation of a layout tree. */
public interface PrintTree extends TraverseTree {
    String getDebugLabel(NodeId node);

    Layout getFinalLayout(NodeId node);
}
