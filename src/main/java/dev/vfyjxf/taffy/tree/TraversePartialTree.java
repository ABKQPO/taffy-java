package dev.vfyjxf.taffy.tree;

import java.util.List;

/** Access to a node and its direct children for low-level layout algorithms. */
public interface TraversePartialTree {
    List<NodeId> getChildren(NodeId parent);

    int childCount(NodeId parent);

    default NodeId getChildId(NodeId parent, int childIndex) {
        return getChildren(parent).get(childIndex);
    }

    /** Rust-compatible alias for iterating direct child identifiers. */
    default List<NodeId> childIds(NodeId parent) {
        return getChildren(parent);
    }

    /** Rust-compatible alias for indexed child access. */
    default NodeId getChild(NodeId parent, int childIndex) {
        return getChildId(parent, childIndex);
    }
}
