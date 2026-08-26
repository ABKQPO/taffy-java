package dev.vfyjxf.taffy.tree;

/** Tree contract required to round an unrounded layout tree. */
public interface RoundTree extends TraverseTree {
    Layout getUnroundedLayout(NodeId node);

    void setFinalLayout(NodeId node, Layout layout);
}
