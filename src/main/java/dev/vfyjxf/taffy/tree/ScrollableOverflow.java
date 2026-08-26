package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyPoint;
import dev.vfyjxf.taffy.style.BoxGenerationMode;
import dev.vfyjxf.taffy.style.Contain;
import dev.vfyjxf.taffy.style.Overflow;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyStyle;

/** Computes scrollable overflow rectangles using the Rust Taffy propagation rules. */
public class ScrollableOverflow {
    /** Refreshes container overflow after deferred out-of-flow positioning changes child layouts. */
    public static void refreshTree(LayoutPartialTree tree, LayoutComputer layoutComputer, NodeId node) {
        for (NodeId child : tree.getChildren(node)) {
            refreshTree(tree, layoutComputer, child);
        }
        if (tree.childCount(node) == 0) {
            return;
        }

        Layout layout = tree.getUnroundedLayout(node);
        if (layout == null) {
            return;
        }
        TaffyStyle style = tree.getStyle(node);
        FloatRect overflowRect = fromChildren(
            tree,
            node,
            layout.size(),
            layout.border(),
            layout.padding(),
            layout.scrollbarSize(),
            layoutComputer.resolveDirection(node),
            style.getOverflow()
        );
        tree.setUnroundedLayout(node, new Layout(
            layout.order(),
            layout.location(),
            layout.size(),
            layout.contentSize(),
            layout.scrollbarSize(),
            layout.border(),
            layout.padding(),
            layout.margin(),
            overflowRect,
            layout.baselines()
        ));
    }

    public static FloatRect fromChildren(
        LayoutPartialTree tree,
        NodeId node,
        FloatSize containerSize,
        FloatRect border,
        FloatRect padding,
        FloatSize scrollbarGutter,
        TaffyDirection direction,
        TaffyPoint<Overflow> containerOverflow
    ) {
        boolean parentIsScrollContainer = isScrollContainer(containerOverflow);
        FloatRect overflowRect = FloatRect.zero();
        for (NodeId child : tree.getChildren(node)) {
            TaffyStyle childStyle = tree.getStyle(child);
            if (childStyle.getBoxGenerationMode() == BoxGenerationMode.NONE) {
                continue;
            }

            Layout childLayout = tree.getUnroundedLayout(child);
            if (childLayout == null) {
                continue;
            }

            boolean outOfFlow = childStyle.getPosition().isOutOfFlow();
            FloatPoint contributionLocation = contributionLocation(
                childLayout.location(),
                childLayout.size(),
                containerSize,
                border,
                scrollbarGutter,
                direction,
                outOfFlow
            );
            overflowRect = union(
                overflowRect,
                contribution(
                    contributionLocation,
                    childLayout.size(),
                    childLayout.scrollableOverflowRect(),
                    childStyle.getOverflow(),
                    childStyle.contain,
                    parentIsScrollContainer
                )
            );
        }

        if (parentIsScrollContainer) {
            overflowRect.right += direction.isRtl() ? padding.left : padding.right;
            overflowRect.bottom += padding.bottom;
        }
        return overflowRect;
    }

    public static FloatRect contribution(
        FloatPoint location,
        FloatSize size,
        FloatRect childOverflow,
        TaffyPoint<Overflow> overflow,
        Contain contain,
        boolean parentIsScrollContainer
    ) {
        boolean childIsScrollContainer = isScrollContainer(overflow);
        boolean containsOverflow = contain.containsScrollableOverflow();
        boolean propagatesX = !childIsScrollContainer && !containsOverflow && overflow.x == Overflow.VISIBLE;
        boolean propagatesY = !childIsScrollContainer && !containsOverflow && overflow.y == Overflow.VISIBLE;

        float endWidth = propagatesX ? Math.max(size.width, childOverflow.right) : size.width;
        float endHeight = propagatesY ? Math.max(size.height, childOverflow.bottom) : size.height;
        if (endWidth <= 0f || endHeight <= 0f) {
            return FloatRect.zero();
        }

        float startX = propagatesX ? Math.min(0f, childOverflow.left) : 0f;
        float startY = propagatesY ? Math.min(0f, childOverflow.top) : 0f;
        FloatRect result = FloatRect.ltrb(
            location.x + startX,
            location.y + startY,
            location.x + endWidth,
            location.y + endHeight
        );
        return parentIsScrollContainer && (result.right <= 0f || result.bottom <= 0f)
            ? FloatRect.zero()
            : result;
    }

    public static FloatRect union(FloatRect first, FloatRect second) {
        return FloatRect.ltrb(
            Math.min(first.left, second.left),
            Math.min(first.top, second.top),
            Math.max(first.right, second.right),
            Math.max(first.bottom, second.bottom)
        );
    }

    private static FloatPoint contributionLocation(
        FloatPoint childLocation,
        FloatSize childSize,
        FloatSize containerSize,
        FloatRect border,
        FloatSize scrollbarGutter,
        TaffyDirection direction,
        boolean outOfFlow
    ) {
        float startX = border.left + (outOfFlow ? scrollbarGutter.width : 0f);
        float endX = border.right + (outOfFlow ? scrollbarGutter.width : 0f);
        float startY = border.top + (outOfFlow ? scrollbarGutter.height : 0f);
        float x = direction.isRtl()
            ? containerSize.width - childLocation.x - childSize.width - endX
            : childLocation.x - startX;
        return new FloatPoint(x, childLocation.y - startY);
    }

    private static boolean isScrollContainer(TaffyPoint<Overflow> overflow) {
        return overflow.x.isScrollContainer() || overflow.y.isScrollContainer();
    }
}
