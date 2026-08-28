package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.CustomIdentCodec;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.Style;
import dev.vfyjxf.taffy.util.MeasureFunc;
import dev.vfyjxf.taffy.util.NodeLayoutMeasureFunc;
import dev.vfyjxf.taffy.util.NodeMeasureFunc;
import dev.vfyjxf.taffy.util.RoundLayout;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

/**
 * An entire tree of UI nodes. The entry point to Taffy's high-level API.
 * <p>
 * Allows you to build a tree of UI nodes, run Taffy's layout algorithms over that tree,
 * and then access the resultant layout.
 */
public class TaffyTree implements LayoutFlexboxContainer, LayoutGridContainer, LayoutBlockContainer, RoundTree, PrintTree, CacheTree {

    private static final int DEFAULT_CAPACITY = 16;

    /** Counter for generating unique node IDs */
    private final AtomicLong nodeIdCounter = new AtomicLong(0);

    /** NodeData storage by node ID - using fastutil for faster primitive key access */
    private final Long2ObjectOpenHashMap<NodeData> nodes;

    /** Context data (measure functions) storage by node ID */
    private final Long2ObjectOpenHashMap<MeasureFunc> nodeContextData;

    /** Arbitrary user context storage by node ID. */
    private final Long2ObjectOpenHashMap<Object> nodeContexts;

    /** Children of each node */
    private final Long2ObjectOpenHashMap<List<NodeId>> children;

    /** Parent of each node */
    private final Long2ObjectOpenHashMap<NodeId> parents;

    /** Whether to round layout values */
    private boolean useRounding = true;
    
    /** Optional listener for layout change notifications */
    private LayoutChangeListener layoutChangeListener = null;

    /**
     * Creates a new TaffyTree with default capacity.
     */
    public TaffyTree() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a new TaffyTree with the specified initial capacity.
     */
    public TaffyTree(int capacity) {
        this.nodes = new Long2ObjectOpenHashMap<>(capacity);
        this.nodeContextData = new Long2ObjectOpenHashMap<>(capacity);
        this.nodeContexts = new Long2ObjectOpenHashMap<>(capacity);
        this.children = new Long2ObjectOpenHashMap<>(capacity);
        this.parents = new Long2ObjectOpenHashMap<>(capacity);
    }

    /** Creates a tree with the requested initial storage capacity. */
    public static TaffyTree withCapacity(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("capacity must not be negative");
        return new TaffyTree(capacity);
    }


    /**
     * Enable rounding of layout values. Rounding is enabled by default.
     */
    public void enableRounding() {
        this.useRounding = true;
    }

    /**
     * Disable rounding of layout values.
     */
    public void disableRounding() {
        this.useRounding = false;
    }

    /**
     * Returns whether rounding is enabled.
     */
    public boolean roundingEnabled() {
        return useRounding;
    }
    
    /**
     * Sets a listener to be notified when node layouts change during computation.
     * 
     * <p>This allows users to:
     * <ul>
     *   <li>Collect a dirty set of changed nodes for efficient incremental updates</li>
     *   <li>Define custom "root node" concepts and track changes within subtrees</li>
     *   <li>Implement custom layout change handling logic</li>
     * </ul>
     * 
     * @param listener the listener, or null to remove the current listener
     * @see LayoutChangeListener
     */
    public void setLayoutChangeListener(LayoutChangeListener listener) {
        this.layoutChangeListener = listener;
    }
    
    /**
     * Gets the current layout change listener.
     * @return the current listener, or null if none is set
     */
    public LayoutChangeListener getLayoutChangeListener() {
        return layoutChangeListener;
    }


    /**
     * Creates and adds a new unattached leaf node to the tree.
     */
    public NodeId newLeaf(TaffyStyle style) {
        long id = nodeIdCounter.getAndIncrement();
        NodeId nodeId = new NodeId(id);
        
        nodes.put(id, new NodeData(style));
        children.put(id, new ArrayList<>());
        parents.put(id, null);
        
        return nodeId;
    }

    /**
     * Creates and adds a new unattached leaf node with a measure function.
     */
    public NodeId newLeafWithMeasure(TaffyStyle style, MeasureFunc measureFunc) {
        if (measureFunc == null) return newLeaf(style);
        long id = nodeIdCounter.getAndIncrement();
        NodeId nodeId = new NodeId(id);
        
        NodeData data = new NodeData(style);
        data.setHasContext(true);
        nodes.put(id, data);
        nodeContextData.put(id, measureFunc);
        children.put(id, new ArrayList<>());
        parents.put(id, null);
        
        return nodeId;
    }

    /** Creates a measured leaf from a generic style after normalizing custom grid identifiers. */
    public <S> NodeId newLeafWithMeasure(Style<S> style, MeasureFunc measureFunc) {
        return newLeafWithMeasure(style.toTaffyStyle(), measureFunc);
    }

    /** Create a leaf from a generic style after normalizing its custom grid identifiers. */
    public <S> NodeId newLeaf(Style<S> style) {
        return newLeaf(style.toTaffyStyle());
    }

    /** Creates an unattached leaf node with arbitrary user context data. */
    public NodeId newLeafWithContext(TaffyStyle style, Object context) {
        NodeId node = newLeaf(style);
        setNodeContext(node, context);
        return node;
    }

    /** Creates a contextual leaf from a generic style after normalizing custom grid identifiers. */
    public <S> NodeId newLeafWithContext(Style<S> style, Object context) {
        return newLeafWithContext(style.toTaffyStyle(), context);
    }

    /**
     * Creates and adds a new node with children.
     */
    public NodeId newWithChildren(TaffyStyle style, NodeId... childNodes) {
        if (childNodes == null) throw new IllegalArgumentException("childNodes must not be null");
        Set<NodeId> uniqueChildren = new HashSet<>();
        for (NodeId child : childNodes) {
            requireExistingChild(child);
            if (!uniqueChildren.add(child)) throw new IllegalArgumentException("A child node may only appear once");
        }
        long id = nodeIdCounter.getAndIncrement();
        NodeId nodeId = new NodeId(id);

        nodes.put(id, new NodeData(style));

        List<NodeId> childList = new ArrayList<>(childNodes.length);
        for (NodeId child : childNodes) {
            NodeId previousParent = parents.get(child.getId());
            if (previousParent != null) {
                List<NodeId> previousChildren = children.get(previousParent.getId());
                if (previousChildren != null) previousChildren.remove(child);
                markDirty(previousParent);
            }
            parents.put(child.getId(), nodeId);
            childList.add(child);
        }
        
        children.put(id, childList);
        parents.put(id, null);
        
        return nodeId;
    }

    /**
     * Creates and adds a new node with children from a list.
     */
    public NodeId newWithChildren(TaffyStyle style, List<NodeId> childNodes) {
        return newWithChildren(style, childNodes.toArray(new NodeId[0]));
    }

    /** Creates a node with children from a generic style after normalizing custom grid identifiers. */
    public <S> NodeId newWithChildren(Style<S> style, NodeId... childNodes) {
        return newWithChildren(style.toTaffyStyle(), childNodes);
    }

    /** Creates a node with a child list from a generic style after normalizing custom grid identifiers. */
    public <S> NodeId newWithChildren(Style<S> style, List<NodeId> childNodes) {
        return newWithChildren(style.toTaffyStyle(), childNodes);
    }


    /**
     * Drops all nodes in the tree.
     */
    public void clear() {
        nodes.clear();
        nodeContextData.clear();
        nodeContexts.clear();
        children.clear();
        parents.clear();
    }

    /**
     * Remove a specific node from the tree.
     */
    public void remove(NodeId node) {
        requireExistingNode(node);
        long key = node.getId();
        
        // Remove from parent's children list
        NodeId parent = parents.get(key);
        if (parent != null) {
            List<NodeId> parentChildren = children.get(parent.getId());
            if (parentChildren != null) {
                parentChildren.removeIf(n -> n.equals(node));
            }
            markDirty(parent);
        }
        
        // Remove parent references from this node's children
        List<NodeId> nodeChildren = children.get(key);
        if (nodeChildren != null) {
            for (NodeId child : nodeChildren) {
                parents.put(child.getId(), null);
            }
        }
        
        children.remove(key);
        parents.remove(key);
        nodes.remove(key);
        nodeContextData.remove(key);
        nodeContexts.remove(key);
    }

    /** Removes a node and returns its identifier. */
    public NodeId removeNode(NodeId node) {
        requireExistingNode(node);
        remove(node);
        return node;
    }


    /**
     * Sets the measure function for a node.
     */
    public void setMeasureFunc(NodeId node, MeasureFunc measureFunc) {
        requireExistingNode(node);
        long key = node.getId();
        NodeData data = nodes.get(key);
        
        if (measureFunc != null) {
            nodeContextData.put(key, measureFunc);
        } else {
            nodeContextData.remove(key);
        }
        data.setHasContext(measureFunc != null || nodeContexts.containsKey(key));
        
        markDirty(node);
    }

    /**
     * Gets the measure function for a node.
     */
    public MeasureFunc getMeasureFunc(NodeId node) {
        requireExistingNode(node);
        return nodeContextData.get(node.getId());
    }

    /** Stores arbitrary user context and invalidates cached layout for the node. */
    public void setNodeContext(NodeId node, Object context) {
        requireExistingNode(node);
        long key = node.getId();
        NodeData data = nodes.get(key);
        if (context == null) {
            nodeContexts.remove(key);
        } else {
            nodeContexts.put(key, context);
        }
        data.setHasContext(context != null || nodeContextData.containsKey(key));
        markDirty(node);
    }

    /** Stores a typed context value for a node. */
    public <C> void setNodeContext(NodeId node, C context, Class<C> type) {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (context != null && !type.isInstance(context)) {
            throw new IllegalArgumentException("Context does not match requested type");
        }
        setNodeContext(node, context);
    }

    /** Returns the arbitrary user context stored for a node, or null when none is set. */
    @Override
    @SuppressWarnings("unchecked")
    public <C> C getNodeContext(NodeId node) {
        requireExistingNode(node);
        return (C) nodeContexts.get(node.getId());
    }

    /** Returns a typed context value, failing when the stored value has another type. */
    public <C> C getNodeContext(NodeId node, Class<C> type) {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        Object context = getNodeContext(node);
        return context == null ? null : type.cast(context);
    }

    /** Returns a typed context value or throws when no value is stored. */
    public <C> C getNodeContextOrThrow(NodeId node, Class<C> type) {
        C context = getNodeContext(node, type);
        if (context == null) throw new IllegalStateException("Node has no context: " + node);
        return context;
    }

    /** Updates a typed node context and returns the updated value. */
    public <C> C updateNodeContext(NodeId node, Class<C> type, UnaryOperator<C> update) {
        if (type == null || update == null) throw new IllegalArgumentException("Arguments must not be null");
        C current = getNodeContext(node, type);
        C next = update.apply(current);
        setNodeContext(node, next, type);
        return next;
    }

    /** Returns multiple typed contexts for disjoint node access. */
    public <C> Map<NodeId, C> getDisjointNodeContexts(List<NodeId> requested, Class<C> type) {
        if (requested == null || type == null) throw new IllegalArgumentException("Arguments must not be null");
        Map<NodeId, C> result = new LinkedHashMap<>();
        for (NodeId node : requested) {
            if (node == null || !nodes.containsKey(node.getId()) || result.containsKey(node)) return null;
            C context = getNodeContext(node, type);
            if (context == null) return null;
            result.put(node, context);
        }
        return Collections.unmodifiableMap(result);
    }

    /** Rust-compatible naming for disjoint typed context access. */
    public <C> Map<NodeId, C> getDisjointNodeContextMut(Class<C> type, NodeId... requested) {
        if (requested == null) throw new IllegalArgumentException("requested must not be null");
        return getDisjointNodeContexts(Arrays.asList(requested), type);
    }


    /**
     * Adds a child node under the parent.
     */
    public void addChild(NodeId parent, NodeId child) {
        requireExistingParent(parent);
        requireExistingChild(child);
        ensureCanAttach(parent, child);
        rejectAlreadyAttached(child);
        long parentKey = parent.getId();
        long childKey = child.getId();

        detachFromCurrentParent(child);
        parents.put(childKey, parent);
        children.get(parentKey).add(child);
        markDirty(parent);
    }

    /**
     * Inserts a child at the given index.
     */
    public void insertChildAtIndex(NodeId parent, int childIndex, NodeId child) {
        requireExistingParent(parent);
        requireExistingChild(child);
        ensureCanAttach(parent, child);
        rejectAlreadyAttached(child);
        long parentKey = parent.getId();
        List<NodeId> parentChildren = children.get(parentKey);

        int childCount = parentChildren.size();
        if (childIndex < 0 || childIndex > childCount) {
            throw TaffyException.childIndexOutOfBounds(parent, childIndex, childCount);
        }
        
        detachFromCurrentParent(child);
        parents.put(child.getId(), parent);
        parentChildren.add(childIndex, child);
        markDirty(parent);
    }

    /**
     * Sets the children of a node, replacing existing children.
     */
    public void setChildren(NodeId parent, NodeId... newChildren) {
        requireExistingParent(parent);
        if (newChildren == null) throw new IllegalArgumentException("newChildren must not be null");
        Set<NodeId> uniqueChildren = new HashSet<>();
        for (NodeId child : newChildren) {
            requireExistingChild(child);
            ensureCanAttach(parent, child);
            if (!uniqueChildren.add(child)) throw new IllegalArgumentException("A child node may only appear once");
        }
        long parentKey = parent.getId();
        List<NodeId> parentChildList = children.get(parentKey);

        // Remove parent reference from current children
        for (NodeId child : parentChildList) {
            parents.put(child.getId(), null);
        }

        // Set new children
        for (NodeId child : newChildren) {
            detachFromCurrentParent(child);
            parents.put(child.getId(), parent);
        }
        
        parentChildList.clear();
        parentChildList.addAll(Arrays.asList(newChildren));
        markDirty(parent);
    }

    /**
     * Removes a child from a parent.
     */
    public NodeId removeChild(NodeId parent, NodeId child) {
        requireExistingParent(parent);
        requireExistingChild(child);
        List<NodeId> parentChildren = children.get(parent.getId());

        int index = -1;
        for (int i = 0; i < parentChildren.size(); i++) {
            if (parentChildren.get(i).equals(child)) {
                index = i;
                break;
            }
        }
        
        if (index < 0) {
            throw TaffyException.invalidChildNode(child);
        }
        return removeChildAtIndex(parent, index);
    }

    /** Removes a child and returns the detached identifier. */
    public NodeId removeChildChecked(NodeId parent, NodeId child) {
        return removeChild(parent, child);
    }

    /**
     * Removes the child at the given index.
     */
    public NodeId removeChildAtIndex(NodeId parent, int childIndex) {
        long parentKey = parent.getId();
        List<NodeId> parentChildren = children.get(parentKey);
        
        if (parentChildren == null) {
            throw TaffyException.invalidParentNode(parent);
        }
        
        int childCount = parentChildren.size();
        if (childIndex < 0 || childIndex >= childCount) {
            throw TaffyException.childIndexOutOfBounds(parent, childIndex, childCount);
        }
        
        NodeId child = parentChildren.remove(childIndex);
        parents.put(child.getId(), null);
        markDirty(parent);
        
        return child;
    }

    /**
     * Removes all children in the half-open range [fromIndex, toIndex).
     * Removed nodes remain in the tree as unattached nodes.
     */
    public void removeChildrenRange(NodeId parent, int fromIndex, int toIndex) {
        long parentKey = parent.getId();
        List<NodeId> parentChildren = children.get(parentKey);
        if (parentChildren == null) {
            throw TaffyException.invalidParentNode(parent);
        }
        if (fromIndex < 0 || toIndex < 0 || toIndex < fromIndex || toIndex > parentChildren.size()) {
            throw TaffyException.childIndexOutOfBounds(parent, toIndex, parentChildren.size());
        }
        for (int i = fromIndex; i < toIndex; i++) {
            parents.put(parentChildren.get(i).getId(), null);
        }
        parentChildren.subList(fromIndex, toIndex).clear();
        markDirty(parent);
    }

    /** Removes children in an inclusive-exclusive range and returns detached identifiers. */
    public List<NodeId> removeChildrenRangeChecked(NodeId parent, int fromIndex, int toIndex) {
        List<NodeId> parentChildren = children.get(parent.getId());
        if (parentChildren == null) throw TaffyException.invalidParentNode(parent);
        if (fromIndex < 0 || toIndex < 0 || toIndex < fromIndex || toIndex > parentChildren.size()) {
            throw TaffyException.childIndexOutOfBounds(parent, toIndex, parentChildren.size());
        }
        List<NodeId> removed = new ArrayList<>(parentChildren.subList(fromIndex, toIndex));
        removeChildrenRange(parent, fromIndex, toIndex);
        return removed;
    }

    /** Removes all children from the supplied index through the end. */
    public void removeChildrenRange(NodeId parent, int fromIndex) {
        removeChildrenRange(parent, fromIndex, childCount(parent));
    }

    /** Removes children using a Rust-style open-ended range. */
    public void removeChildrenRange(NodeId parent, ChildRange range) {
        if (range == null) throw new IllegalArgumentException("range must not be null");
        removeChildrenRange(parent, range.start(), Math.min(range.end(), childCount(parent)));
    }

    /** Removes children using a range and returns the detached identifiers. */
    public List<NodeId> removeChildrenRangeChecked(NodeId parent, ChildRange range) {
        if (range == null) throw new IllegalArgumentException("range must not be null");
        int end = Math.min(range.end(), childCount(parent));
        return removeChildrenRangeChecked(parent, range.start(), end);
    }

    /**
     * Gets the child at the given index.
     */
    public NodeId getChildAtIndex(NodeId parent, int childIndex) {
        long parentKey = parent.getId();
        List<NodeId> parentChildren = children.get(parentKey);
        
        if (parentChildren == null) {
            throw TaffyException.invalidParentNode(parent);
        }
        
        int childCount = parentChildren.size();
        if (childIndex < 0 || childIndex >= childCount) {
            throw TaffyException.childIndexOutOfBounds(parent, childIndex, childCount);
        }
        
        return parentChildren.get(childIndex);
    }

    /** Rust-compatible alias for indexed child access. */
    public NodeId childAtIndex(NodeId parent, int childIndex) {
        return getChildAtIndex(parent, childIndex);
    }

    /**
     * Replaces the child at the given index with a new child.
     */
    public NodeId replaceChildAtIndex(NodeId parent, int childIndex, NodeId newChild) {
        requireExistingParent(parent);
        requireExistingChild(newChild);
        ensureCanAttach(parent, newChild);
        long parentKey = parent.getId();
        List<NodeId> parentChildren = children.get(parentKey);

        int childCount = parentChildren.size();
        if (childIndex < 0 || childIndex >= childCount) {
            throw TaffyException.childIndexOutOfBounds(parent, childIndex, childCount);
        }
        NodeId currentChild = parentChildren.get(childIndex);
        if (currentChild.equals(newChild)) return currentChild;
        if (parentChildren.contains(newChild)) {
            throw new IllegalArgumentException("The replacement child is already attached to this parent");
        }
        detachFromCurrentParent(newChild);
        parents.put(newChild.getId(), parent);
        NodeId oldChild = parentChildren.set(childIndex, newChild);
        parents.put(oldChild.getId(), null);
        markDirty(parent);
        
        return oldChild;
    }


    /**
     * Returns the number of children of a node.
     */
    public int childCount(NodeId parent) {
        if (parent == null || !nodes.containsKey(parent.getId())) {
            throw TaffyException.invalidParentNode(parent);
        }
        List<NodeId> parentChildren = children.get(parent.getId());
        return parentChildren.size();
    }

    /**
     * Returns an unmodifiable list of children.
     */
    public List<NodeId> getChildren(NodeId parent) {
        if (parent == null || !nodes.containsKey(parent.getId())) {
            throw TaffyException.invalidParentNode(parent);
        }
        List<NodeId> parentChildren = children.get(parent.getId());
        return List.copyOf(parentChildren);
    }

    /** Rust-compatible alias for retrieving a node's children. */
    public List<NodeId> children(NodeId parent) {
        if (parent == null || !nodes.containsKey(parent.getId())) throw TaffyException.invalidParentNode(parent);
        return List.copyOf(getChildren(parent));
    }

    /**
     * Returns the children list directly without creating a wrapper.
     * Only for internal use where the caller guarantees not to modify the list.
     */
    List<NodeId> getChildrenInternal(NodeId parent) {
        return children.get(parent.getId());
    }

    /**
     * Returns the total number of nodes in the tree.
     */
    public int totalNodeCount() {
        return nodes.size();
    }

    /**
     * Returns the parent of a node.
     */
    public NodeId getParent(NodeId child) {
        if (child == null || !nodes.containsKey(child.getId())) {
            throw TaffyException.invalidInputNode(child);
        }
        return parents.get(child.getId());
    }

    /** Rust-compatible alias for retrieving a node parent. */
    public NodeId parent(NodeId child) {
        if (child == null || !nodes.containsKey(child.getId())) throw TaffyException.invalidInputNode(child);
        return getParent(child);
    }


    /**
     * Sets the style of a node.
     */
    public void setStyle(NodeId node, TaffyStyle style) {
        requireExistingNode(node);
        Objects.requireNonNull(style, "style");
        NodeData data = nodes.get(node.getId());
        data.setStyle(style);
        markDirty(node);
    }

    /**
     * Gets the style of a node.
     */
    public TaffyStyle getStyle(NodeId node) {
        NodeData data = nodes.get(node.getId());
        if (data == null) {
            throw TaffyException.invalidInputNode(node);
        }
        return data.getStyle();
    }

    /** Rust-compatible alias for retrieving a node style. */
    public TaffyStyle style(NodeId node) {
        return getStyle(node);
    }

    /** Restores a typed generic style view with the caller's custom identifier codec. */
    public <S> Style<S> getStyle(NodeId node, CustomIdentCodec<S> identifierCodec) {
        return Style.fromTaffyStyle(getStyle(node), identifierCodec);
    }

    /** Set a generic style after converting its custom identifiers to runtime grid names. */
    public <S> void setStyle(NodeId node, Style<S> style) {
        setStyle(node, style.toTaffyStyle());
    }


    /**
     * Returns the layout of a node.
     */
    public Layout getLayout(NodeId node) {
        if (node == null) return null;
        NodeData data = nodes.get(node.getId());
        if (data == null) {
            return null;
        }
        return useRounding ? data.getFinalLayout() : data.getUnroundedLayout();
    }

    /** Returns a layout or throws when the node is not present. */
    public Layout getLayoutChecked(NodeId node) {
        Layout layout = getLayout(node);
        if (layout == null) throw TaffyException.invalidInputNode(node);
        return layout;
    }

    /** Rust-compatible strict layout accessor. */
    public Layout layout(NodeId node) {
        return getLayoutChecked(node);
    }

    /**
     * Returns the unrounded layout of a node.
     */
    public Layout getUnroundedLayout(NodeId node) {
        if (node == null) return null;
        NodeData data = nodes.get(node.getId());
        return data != null ? data.getUnroundedLayout() : null;
    }

    /** Rust-compatible strict access to unrounded layout. */
    public Layout unroundedLayout(NodeId node) {
        Layout layout = getUnroundedLayout(node);
        if (layout == null) throw TaffyException.invalidInputNode(node);
        return layout;
    }

    /** Returns algorithm-specific detail data for a node. */
    public DetailedLayoutInfo getDetailedLayoutInfo(NodeId node) {
        NodeData data = nodes.get(node.getId());
        if (data == null) throw TaffyException.invalidInputNode(node);
        return data.getDetailedLayoutInfo();
    }

    /** Alias matching the Rust API naming. */
    public DetailedLayoutInfo detailedLayoutInfo(NodeId node) {
        return getDetailedLayoutInfo(node);
    }

    /** Returns typed grid diagnostics by decoding runtime names with the supplied identifier codec. */
    public <S> GenericDetailedGridInfo<S> getDetailedGridInfo(NodeId node, CustomIdentCodec<S> identifierCodec) {
        DetailedLayoutInfo detail = getDetailedLayoutInfo(node);
        if (!detail.isGrid()) {
            throw new IllegalStateException("Node does not have detailed grid layout information");
        }
        return new GenericDetailedGridInfo<>(detail.grid(), identifierCodec);
    }

    public void setDetailedLayoutInfo(NodeId node, DetailedLayoutInfo info) {
        NodeData data = nodes.get(node.getId());
        if (data != null) data.setDetailedLayoutInfo(info);
    }


    /**
     * Returns true if this node has a new layout that hasn't been acknowledged.
     * <p>
     * Similar to Yoga's hasNewLayout - set after layout computation, cleared by acknowledgeLayout().
     * Unlike the previous version-based approach, this is set regardless of whether the layout
     * actually changed, allowing users to walk the tree efficiently from root.
     * 
     * @deprecated Use {@link #hasNewLayout(NodeId)} instead for clearer naming.
     */
    @Deprecated
    public boolean hasUnconsumedLayout(NodeId node) {
        return hasNewLayout(node);
    }

    /**
     * Returns true if this node has a new layout that hasn't been acknowledged.
     * <p>
     * Similar to Yoga's hasNewLayout - set after layout computation, cleared by acknowledgeLayout().
     */
    public boolean hasNewLayout(NodeId node) {
        NodeData data = nodes.get(node.getId());
        if (data == null) {
            throw TaffyException.invalidInputNode(node);
        }
        return data.hasNewLayout();
    }

    /**
     * Returns true if any descendant of this node has a new layout.
     * <p>
     * This allows efficient tree walking from root - you can skip entire subtrees
     * where no layout changes occurred.
     */
    public boolean hasDirtyDescendant(NodeId node) {
        NodeData data = nodes.get(node.getId());
        if (data == null) {
            throw TaffyException.invalidInputNode(node);
        }
        return data.hasDirtyDescendant();
    }

    /**
     * Returns true if this node or any of its descendants has a new layout.
     * <p>
     * Convenience method for tree walking - returns true if you need to visit
     * this node or any of its children.
     */
    public boolean needsVisit(NodeId node) {
        NodeData data = nodes.get(node.getId());
        if (data == null) {
            throw TaffyException.invalidInputNode(node);
        }
        return data.needsVisit();
    }

    /**
     * Marks the current layout as consumed/acknowledged for this node.
     */
    public void acknowledgeLayout(NodeId node) {
        NodeData data = nodes.get(node.getId());
        if (data == null) {
            throw TaffyException.invalidInputNode(node);
        }
        data.acknowledgeLayout();
    }

    /**
     * Acknowledges layout for this node and clears dirty descendant flag.
     * <p>
     * Call this after you have processed this node AND all its descendants.
     * This is useful for bottom-up acknowledgement during tree traversal.
     */
    public void acknowledgeSubtree(NodeId node) {
        NodeData data = nodes.get(node.getId());
        if (data == null) {
            throw TaffyException.invalidInputNode(node);
        }
        data.acknowledgeLayout();
        data.clearDirtyDescendant();
    }

    /**
     * Marks a node as having a new layout and propagates dirty flag up to ancestors.
     * Also notifies the layout change listener if one is set.
     */
    private void markNodeLayoutUpdated(NodeId node, Layout oldLayout, Layout newLayout) {
        NodeData data = nodes.get(node.getId());
        if (data == null) return;
        
        data.markNewLayout();
        
        // Notify the layout change listener
        if (layoutChangeListener != null) {
            layoutChangeListener.onLayoutChanged(node, oldLayout, newLayout);
        }
        
        // Propagate dirty descendant flag up to ancestors
        NodeId parent = parents.get(node.getId());
        while (parent != null) {
            NodeData parentData = nodes.get(parent.getId());
            if (parentData == null) break;
            
            // If already marked, all ancestors are already marked too
            if (parentData.markDirtyDescendant()) {
                break;
            }
            parent = parents.get(parent.getId());
        }
    }

    /**
     * Sets the final (rounded) layout of a node.
     */
    public void setLayout(NodeId node, Layout layout) {
        NodeData data = nodes.get(node.getId());
        if (data != null) {
            Layout oldLayout = data.getFinalLayout();
            data.setFinalLayout(layout);
            // When rounding is enabled, mark after setting final layout
            if (useRounding) {
                markNodeLayoutUpdated(node, oldLayout, layout);
            }
        }
    }

    /** Set the rounded layout, as required by the low-level RoundTree contract. */
    public void setFinalLayout(NodeId node, Layout layout) {
        setLayout(node, layout);
    }

    /** Return the rounded layout regardless of the tree's current rounding display mode. */
    public Layout getFinalLayout(NodeId node) {
        NodeData data = nodes.get(node.getId());
        return data == null ? null : data.getFinalLayout();
    }

    /**
     * Sets the unrounded layout of a node.
     */
    public void setUnroundedLayout(NodeId node, Layout layout) {
        NodeData data = nodes.get(node.getId());
        if (data != null) {
            Layout oldLayout = data.getUnroundedLayout();
            data.setUnroundedLayout(layout);
            // When rounding is disabled, mark after setting unrounded layout
            if (!useRounding) {
                markNodeLayoutUpdated(node, oldLayout, layout);
            }
        }
    }


    /**
     * Gets the cache entry for a node.
     */
    public LayoutOutput getCacheEntry(NodeId node, LayoutInput input) {
        NodeData data = nodes.get(node.getId());
        if (data == null) return null;
        return data.getCache().get(input);
    }

    /**
     * Stores a cache entry for a node.
     */
    public void storeCacheEntry(NodeId node, LayoutInput input, LayoutOutput output) {
        NodeData data = nodes.get(node.getId());
        if (data != null) {
            data.getCache().store(input, output);
        }
    }

    /**
     * Clears the cache for a node.
     */
    public void clearCache(NodeId node) {
        NodeData data = nodes.get(node.getId());
        if (data != null) {
            data.getCache().clear();
        }
    }

    @Override
    public LayoutCache getCache(NodeId node) {
        NodeData data = nodes.get(node.getId());
        return data == null ? null : data.getCache();
    }

    @Override
    public LayoutOutput cacheGet(NodeId node, LayoutInput input) {
        return getCacheEntry(node, input);
    }

    @Override
    public void cacheStore(NodeId node, LayoutInput input, LayoutOutput output) {
        storeCacheEntry(node, input, output);
    }

    @Override
    public void cacheClear(NodeId node) {
        clearCache(node);
    }


    /**
     * Marks the node and its ancestors as needing layout recalculation.
     */
    public void markDirty(NodeId node) {
        requireExistingNode(node);
        markDirtyRecursive(node);
    }

    private void markDirtyRecursive(NodeId node) {
        NodeData data = nodes.get(node.getId());
        if (data == null) return;
        
        ClearState clearState = data.markDirty();
        if (clearState == ClearState.CLEARED) {
            NodeId parent = parents.get(node.getId());
            if (parent != null) {
                markDirtyRecursive(parent);
            }
        }
    }

    /**
     * Returns whether a node needs layout recalculation.
     */
    public boolean isDirty(NodeId node) {
        NodeData data = nodes.get(node.getId());
        return data == null || data.getCache().isEmpty();
    }

    /** Rust-compatible alias for querying whether a node is dirty. */
    public boolean dirty(NodeId node) {
        if (!nodes.containsKey(node.getId())) throw TaffyException.invalidInputNode(node);
        return isDirty(node);
    }


    /**
     * Computes the layout for the tree starting from the given root node.
     */
    public void computeLayout(NodeId rootNode, TaffySize<AvailableSpace> availableSpace) {
        computeLayoutWithMeasure(rootNode, availableSpace, (MeasureFunc) null);
    }

    /**
     * Computes the layout with a custom measure function for all nodes.
     */
    public void computeLayoutWithMeasure(NodeId rootNode, TaffySize<AvailableSpace> availableSpace,
                                          MeasureFunc defaultMeasureFunc) {
        // This will be implemented by the compute module
        // For now, delegate to the LayoutComputer
        LayoutComputer computer = new LayoutComputer(this, defaultMeasureFunc);
        LayoutOutput output = computer.computeLayoutWithOutput(rootNode, availableSpace);
        new OutOfFlowPositioner().reposition(this, rootNode, output.oofCandidates(), computer);
        ScrollableOverflow.refreshTree(this, computer, rootNode);
        
        // Round layouts if enabled
        if (useRounding) {
            RoundLayout.roundLayout(this, rootNode);
        }
    }

    /**
     * Computes layout with a callback that receives the measured node, its stored context, and
     * its style. This is the context-aware form of Rust's compute-layout-with-measure entry point.
     */
    public <C> void computeLayoutWithMeasure(
        NodeId rootNode,
        TaffySize<AvailableSpace> availableSpace,
        NodeMeasureFunc<C> measureFunc) {
        computeLayoutWithContextMeasure(rootNode, availableSpace, measureFunc);
    }

    /**
     * Computes layout with a callback that returns a complete output for each leaf node.
     * The callback owns leaf size, overflow, baseline, and collapsible-margin metadata.
     */
    public <C> void computeLayoutWithMeasure(
        NodeId rootNode,
        TaffySize<AvailableSpace> availableSpace,
        NodeLayoutMeasureFunc<C> measureFunc) {
        LayoutComputer computer = new LayoutComputer(this, null, null, measureFunc, null);
        LayoutOutput output = computer.computeLayoutWithOutput(rootNode, availableSpace);
        new OutOfFlowPositioner().reposition(this, rootNode, output.oofCandidates(), computer);
        ScrollableOverflow.refreshTree(this, computer, rootNode);
        if (useRounding) {
            RoundLayout.roundLayout(this, rootNode);
        }
    }

    /** Computes layout using a measure callback that receives node context and style. */
    public <C> void computeLayoutWithContextMeasure(
        NodeId rootNode,
        TaffySize<AvailableSpace> availableSpace,
        NodeMeasureFunc<C> measureFunc) {
        LayoutComputer computer = new LayoutComputer(this, null, measureFunc);
        LayoutOutput output = computer.computeLayoutWithOutput(rootNode, availableSpace);
        new OutOfFlowPositioner().reposition(this, rootNode, output.oofCandidates(), computer);
        ScrollableOverflow.refreshTree(this, computer, rootNode);
        if (useRounding) {
            RoundLayout.roundLayout(this, rootNode);
        }
    }


    /**
     * Returns the node data for internal use.
     */
    NodeData getNodeData(NodeId node) {
        return nodes.get(node.getId());
    }

    /**
     * Checks if a node exists in the tree.
     */
    public boolean containsNode(NodeId node) {
        return node != null && nodes.containsKey(node.getId());
    }

    /**
     * Returns all node IDs in the tree.
     */
    public Set<NodeId> getAllNodes() {
        Set<NodeId> result = new HashSet<>();
        for (Long id : nodes.keySet()) {
            result.add(new NodeId(id));
        }
        return result;
    }

    private void requireExistingNode(NodeId node) {
        if (node == null || !nodes.containsKey(node.getId())) {
            throw TaffyException.invalidInputNode(node);
        }
    }

    private void requireExistingParent(NodeId parent) {
        if (parent == null || !nodes.containsKey(parent.getId())) {
            throw TaffyException.invalidParentNode(parent);
        }
    }

    private void requireExistingChild(NodeId child) {
        if (child == null || !nodes.containsKey(child.getId())) {
            throw TaffyException.invalidChildNode(child);
        }
    }

    private void rejectAlreadyAttached(NodeId child) {
        if (parents.get(child.getId()) != null) {
            throw new IllegalArgumentException("A child node is already attached to a parent");
        }
    }

    private void ensureCanAttach(NodeId parent, NodeId child) {
        NodeId current = parent;
        while (current != null) {
            if (current.equals(child)) {
                throw new IllegalArgumentException("Attaching a node below its descendant would create a cycle");
            }
            current = parents.get(current.getId());
        }
    }

    private void detachFromCurrentParent(NodeId child) {
        NodeId previousParent = parents.get(child.getId());
        if (previousParent == null) return;
        List<NodeId> previousChildren = children.get(previousParent.getId());
        if (previousChildren != null) previousChildren.remove(child);
        parents.put(child.getId(), null);
        markDirty(previousParent);
    }

    /**
     * Prints a debug representation of the tree.
     */
    public void printTree(NodeId root) {
        try {
            writeTree(System.out, root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write tree", exception);
        }
    }

    /** Write a debug tree representation to an arbitrary character sink. */
    public void writeTree(Appendable writer, NodeId root) throws IOException {
        TreePrinter.writeTree(writer, this, root);
    }

    public String getDebugLabel(NodeId node) {
        NodeData data = nodes.get(node.getId());
        if (data == null) return "UNKNOWN";
        
        int numChildren = childCount(node);
        TaffyDisplay display = data.getStyle().getDisplay();
        
        if (display == TaffyDisplay.NONE) return "NONE";
        if (numChildren == 0) return "LEAF";
        
        switch (display) {
            case BLOCK: return "BLOCK";
            case FLEX:
                FlexDirection dir = data.getStyle().getFlexDirection();
                if (dir == FlexDirection.ROW ||
                    dir == FlexDirection.ROW_REVERSE) {
                    return "FLEX ROW";
                }
                return "FLEX COL";
            case GRID: return "GRID";
            default: return display.toString();
        }
    }
}
