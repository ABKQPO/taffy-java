package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.style.BoxSizing;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.util.Resolve;

import java.util.List;

/**
 * Repositions out-of-flow descendants whose containing block differs from their tree parent.
 *
 * This transitional implementation preserves the existing tree-level positioning behavior while
 * layout algorithms are migrated to the candidate-driven out-of-flow dispatcher.
 */
public class OutOfFlowPositioner {
    public void reposition(TaffyTree tree, NodeId rootNode) {
        for (NodeId node : tree.getAllNodes()) {
            repositionNode(tree, rootNode, node, null);
        }
    }

    /**
     * Repositions the candidates emitted by the root layout algorithm.
     */
    public void reposition(TaffyTree tree, NodeId rootNode, List<OofCandidate> candidates) {
        for (OofCandidate candidate : candidates) {
            repositionNode(tree, rootNode, candidate.node(), null);
        }
    }

    /** Repositions candidates while preserving access to the layout measurement dispatcher. */
    public void reposition(TaffyTree tree, NodeId rootNode, List<OofCandidate> candidates,
                           LayoutComputer layoutComputer) {
        for (OofCandidate candidate : candidates) {
            repositionNode(tree, rootNode, candidate.node(), layoutComputer);
        }
    }

    private void repositionNode(TaffyTree tree, NodeId rootNode, NodeId node, LayoutComputer layoutComputer) {
            LayoutComputer resolverComputer = layoutComputer == null ? new LayoutComputer(tree, null) : layoutComputer;
            TaffyStyle style = tree.getStyle(node);
            if (!style.getPosition().isOutOfFlow()) {
                return;
            }

            NodeId parent = tree.getParent(node);
            if (parent == null) {
                return;
            }

            NodeId containingBlock = containingBlock(tree, node, rootNode, style.getPosition());
            if (style.getPosition() == TaffyPosition.ABSOLUTE && containingBlock.equals(parent)) {
                return;
            }
            if (style.getPosition() == TaffyPosition.ABSOLUTE
                && containingBlock.equals(rootNode)
                && !tree.getStyle(rootNode).getPosition().isPositioned()) {
                return;
            }
            Layout containingLayout = tree.getUnroundedLayout(containingBlock);
            float width = containingLayout.size().width - containingLayout.border().left - containingLayout.border().right;
            float height = containingLayout.size().height - containingLayout.border().top - containingLayout.border().bottom;
            float left = style.getInset().left.maybeResolve(width, resolverComputer::resolveCalcValue);
            float right = style.getInset().right.maybeResolve(width, resolverComputer::resolveCalcValue);
            float top = style.getInset().top.maybeResolve(height, resolverComputer::resolveCalcValue);
            float bottom = style.getInset().bottom.maybeResolve(height, resolverComputer::resolveCalcValue);
            float marginLeft = resolvedMargin(style.getMargin().left, width, resolverComputer);
            float marginRight = resolvedMargin(style.getMargin().right, width, resolverComputer);
            float marginTop = resolvedMargin(style.getMargin().top, width, resolverComputer);
            float marginBottom = resolvedMargin(style.getMargin().bottom, width, resolverComputer);
            FloatRect padding = Resolve.resolveRectOrZero(style.getPadding(), width, resolverComputer::resolveCalcValue);
            FloatRect border = Resolve.resolveRectOrZero(style.getBorder(), width, resolverComputer::resolveCalcValue);
            float paddingBorderWidth = padding.left + padding.right + border.left + border.right;
            float paddingBorderHeight = padding.top + padding.bottom + border.top + border.bottom;
            boolean contentBox = style.getBoxSizing() == BoxSizing.CONTENT_BOX;
            FloatSize areaSize = new FloatSize(width, height);
            FloatRect inset = new FloatRect(left, right, top, bottom);
            FloatRect margin = new FloatRect(marginLeft, marginRight, marginTop, marginBottom);
            FloatSize keywordSize = layoutComputer == null
                ? AbsoluteSizing.resolveStretch(style.getSize(), new FloatSize(Float.NaN, Float.NaN), areaSize, inset, margin)
                : AbsoluteSizing.resolveMeasurementKeywords(
                    resolverComputer,
                    node,
                    style.getSize(),
                    new FloatSize(Float.NaN, Float.NaN),
                    areaSize,
                    inset,
                    margin,
                    SizingMode.CONTENT_SIZE
                );
            if (Float.isNaN(left) && Float.isNaN(right) && Float.isNaN(top) && Float.isNaN(bottom)
                && !isContainingBlockResolvable(style.getSize().width)
                && !isContainingBlockResolvable(style.getSize().height)
                && Float.isNaN(keywordSize.width) && Float.isNaN(keywordSize.height)) {
                return;
            }

            FloatPoint containingOrigin = accumulatedLocation(tree, containingBlock);
            FloatPoint parentOrigin = accumulatedLocation(tree, parent);
            Layout layout = tree.getUnroundedLayout(node).copy();
            boolean widthDerivedFromInsets = false;
            boolean heightDerivedFromInsets = false;
            if (!Float.isNaN(keywordSize.width)) {
                layout.size().width = keywordSize.width;
            } else if (!Float.isNaN(left) && !Float.isNaN(right) && style.getSize().width.isAuto()) {
                layout.size().width = Math.max(0f, width - left - right - marginLeft - marginRight);
                widthDerivedFromInsets = true;
            } else if (isContainingBlockResolvable(style.getSize().width)) {
                layout.size().width = style.getSize().width.maybeResolve(width, resolverComputer::resolveCalcValue)
                    + (contentBox ? paddingBorderWidth : 0f);
            }
            if (!Float.isNaN(keywordSize.height)) {
                layout.size().height = keywordSize.height;
            } else if (!Float.isNaN(top) && !Float.isNaN(bottom) && style.getSize().height.isAuto()) {
                layout.size().height = Math.max(0f, height - top - bottom - marginTop - marginBottom);
                heightDerivedFromInsets = true;
            } else if (isContainingBlockResolvable(style.getSize().height)) {
                layout.size().height = style.getSize().height.maybeResolve(height, resolverComputer::resolveCalcValue)
                    + (contentBox ? paddingBorderHeight : 0f);
            }
            Float aspectRatio = style.getAspectRatio();
            if (aspectRatio != null && !Float.isNaN(aspectRatio) && aspectRatio > 0f) {
                if (widthDerivedFromInsets && style.getSize().height.isAuto()) {
                    layout.size().height = layout.size().width / aspectRatio;
                } else if (heightDerivedFromInsets && style.getSize().width.isAuto()) {
                    layout.size().width = layout.size().height * aspectRatio;
                }
            }
            float minWidth = style.getMinSize().width.maybeResolve(width, resolverComputer::resolveCalcValue);
            float maxWidth = style.getMaxSize().width.maybeResolve(width, resolverComputer::resolveCalcValue);
            float minHeight = style.getMinSize().height.maybeResolve(height, resolverComputer::resolveCalcValue);
            float maxHeight = style.getMaxSize().height.maybeResolve(height, resolverComputer::resolveCalcValue);
            minWidth = addBoxSizingAdjustment(minWidth, contentBox, paddingBorderWidth);
            maxWidth = addBoxSizingAdjustment(maxWidth, contentBox, paddingBorderWidth);
            minHeight = addBoxSizingAdjustment(minHeight, contentBox, paddingBorderHeight);
            maxHeight = addBoxSizingAdjustment(maxHeight, contentBox, paddingBorderHeight);
            layout.size().width = clamp(layout.size().width, minWidth, maxWidth);
            layout.size().height = clamp(layout.size().height, minHeight, maxHeight);
            layout.size().width = Math.max(layout.size().width, paddingBorderWidth);
            layout.size().height = Math.max(layout.size().height, paddingBorderHeight);
            if (!Float.isNaN(left) && !Float.isNaN(right)) {
                float freeSpace = Math.max(0f, width - left - right - layout.size().width - marginLeft - marginRight);
                if (style.getMargin().left.isAuto() && style.getMargin().right.isAuto()) {
                    marginLeft = freeSpace / 2f;
                    marginRight = freeSpace / 2f;
                } else if (style.getMargin().left.isAuto()) {
                    marginLeft = freeSpace;
                } else if (style.getMargin().right.isAuto()) {
                    marginRight = freeSpace;
                }
            }
            if (!Float.isNaN(top) && !Float.isNaN(bottom)) {
                float freeSpace = Math.max(0f, height - top - bottom - layout.size().height - marginTop - marginBottom);
                if (style.getMargin().top.isAuto() && style.getMargin().bottom.isAuto()) {
                    marginTop = freeSpace / 2f;
                    marginBottom = freeSpace / 2f;
                } else if (style.getMargin().top.isAuto()) {
                    marginTop = freeSpace;
                } else if (style.getMargin().bottom.isAuto()) {
                    marginBottom = freeSpace;
                }
            }
            if (!Float.isNaN(left) && (!resolveDirection(tree, containingBlock).isRtl() || Float.isNaN(right))) {
                layout.location().x = containingOrigin.x + containingLayout.border().left + left + marginLeft - parentOrigin.x;
            } else if (!Float.isNaN(right)) {
                layout.location().x = containingOrigin.x + containingLayout.size().width
                    - containingLayout.border().right - right - marginRight - layout.size().width - parentOrigin.x;
            }
            if (!Float.isNaN(top)) {
                layout.location().y = containingOrigin.y + containingLayout.border().top + top + marginTop - parentOrigin.y;
            } else if (!Float.isNaN(bottom)) {
                layout.location().y = containingOrigin.y + containingLayout.size().height
                    - containingLayout.border().bottom - bottom - marginBottom - layout.size().height - parentOrigin.y;
            }
            tree.setUnroundedLayout(node, layout);
    }

    private NodeId containingBlock(TaffyTree tree, NodeId node, NodeId rootNode, TaffyPosition position) {
        if (position == TaffyPosition.FIXED) {
            return rootNode;
        }
        NodeId ancestor = tree.getParent(node);
        while (ancestor != null) {
            if (tree.getStyle(ancestor).getPosition().isPositioned()) {
                return ancestor;
            }
            ancestor = tree.getParent(ancestor);
        }
        return rootNode;
    }

    private TaffyDirection resolveDirection(TaffyTree tree, NodeId node) {
        NodeId current = node;
        while (current != null) {
            TaffyDirection direction = tree.getStyle(current).getDirection();
            if (!direction.isInherit()) {
                return direction;
            }
            current = tree.getParent(current);
        }
        return TaffyDirection.DEFAULT;
    }

    private FloatPoint accumulatedLocation(TaffyTree tree, NodeId node) {
        float x = 0f;
        float y = 0f;
        NodeId current = node;
        while (current != null) {
            Layout layout = tree.getUnroundedLayout(current);
            x += layout.location().x;
            y += layout.location().y;
            current = tree.getParent(current);
        }
        return new FloatPoint(x, y);
    }

    private float clamp(float value, float min, float max) {
        if (!Float.isNaN(min)) {
            value = Math.max(value, min);
        }
        if (!Float.isNaN(max)) {
            value = Math.min(value, max);
        }
        return value;
    }

    private float addBoxSizingAdjustment(float value, boolean contentBox, float paddingBorderSize) {
        return !Float.isNaN(value) && contentBox ? value + paddingBorderSize : value;
    }

    private boolean isContainingBlockResolvable(TaffyDimension dimension) {
        return dimension.isPercent() || dimension.isCalc();
    }

    private float resolvedMargin(LengthPercentageAuto value, float context, LayoutComputer layoutComputer) {
        float resolved = value.maybeResolve(context, layoutComputer::resolveCalcValue);
        return Float.isNaN(resolved) ? 0f : resolved;
    }
}
