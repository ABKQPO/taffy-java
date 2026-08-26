package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.FloatSize;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * The final result of a layout algorithm for a single node.
 *
 * @param order         The relative ordering of the node. Nodes with a higher order should be rendered on top.
 * @param location      The top-left corner of the node
 * @param size          The width and height of the node
 * @param contentSize   The width and height of the content inside the node.
 *                      This may be larger than the size of the node in the case of overflowing content.
 * @param scrollbarSize The size of the scrollbars in each dimension. If there is no scrollbar then the size will be zero.
 * @param border        The size of the borders of the node
 * @param padding       The size of the padding of the node
 * @param margin        The size of the margin of the node
 */
@Value
@Accessors(fluent = true)
// 本类另有无参 / 单参便捷构造器，@Value 因此不会自动生成全字段构造器，显式补上以供 8 参调用方使用。
@lombok.AllArgsConstructor
public class Layout {

    private final int order;
    private final FloatPoint location;
    private final FloatSize size;
    private final FloatSize contentSize;
    private final FloatSize scrollbarSize;
    private final FloatRect border;
    private final FloatRect padding;
    private final FloatRect margin;

    /**
     * Creates a new zero-Layout
     */
    public Layout() {
        this(0, FloatPoint.zero(), FloatSize.zero(), FloatSize.zero(), FloatSize.zero(), FloatRect.zero(), FloatRect.zero(), FloatRect.zero());
    }

    /**
     * Creates a new zero-Layout with the supplied order value
     */
    public Layout(int order) {
        this(order, FloatPoint.zero(), FloatSize.zero(), FloatSize.zero(), FloatSize.zero(), FloatRect.zero(), FloatRect.zero(), FloatRect.zero());
    }

    /**
     * Creates a new Layout with the given order
     */
    public static Layout withOrder(int order) {
        return new Layout(order);
    }

    /**
     * Copy constructor
     */
    public Layout copy() {
        return new Layout(
            order,
            location.copy(),
            size.copy(),
            contentSize.copy(),
            scrollbarSize.copy(),
            border.copy(),
            padding.copy(),
            margin.copy()
        );
    }

    /**
     * Get the width of the node's content box
     */
    public float contentBoxWidth() {
        return size.width - padding.left - padding.right - border.left - border.right;
    }

    /**
     * Get the height of the node's content box
     */
    public float contentBoxHeight() {
        return size.height - padding.top - padding.bottom - border.top - border.bottom;
    }

    /**
     * Get the size of the node's content box
     */
    public FloatSize contentBoxSize() {
        return new FloatSize(contentBoxWidth(), contentBoxHeight());
    }

    /**
     * Get x offset of the node's content box relative to its parent's border box
     */
    public float contentBoxX() {
        return location.x + border.left + padding.left;
    }

    /**
     * Get y offset of the node's content box relative to its parent's border box
     */
    public float contentBoxY() {
        return location.y + border.top + padding.top;
    }

    /**
     * Return the scroll width of the node.
     */
    public float scrollWidth() {
        return Math.max(0, contentSize.width + Math.min(scrollbarSize.width, size.width)
                           - size.width + border.right);
    }

    /**
     * Return the scroll height of the node.
     */
    public float scrollHeight() {
        return Math.max(0, contentSize.height + Math.min(scrollbarSize.height, size.height)
                           - size.height + border.bottom);
    }

}
