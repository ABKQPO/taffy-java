package dev.vfyjxf.taffy.tree;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffyPoint;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.BoxGenerationMode;
import dev.vfyjxf.taffy.style.BoxSizing;
import dev.vfyjxf.taffy.style.Clear;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyFloat;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.Overflow;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.TextAlign;
import dev.vfyjxf.taffy.util.ContentSizeUtil;
import dev.vfyjxf.taffy.util.Resolve;
import dev.vfyjxf.taffy.util.TaffyMath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Float.NaN;

/**
 * Computes block layout for nodes with display: block.
 */
public class BlockComputer {

    private final LayoutComputer layoutComputer;

    public BlockComputer(LayoutComputer layoutComputer) {
        this.layoutComputer = layoutComputer;
    }

    /**
     * Internal data structure for block items.
     */
    private static class BlockItem {
        NodeId nodeId;
        int order;
        boolean isTable;
        boolean isReplaced;
        TaffySize<TaffyDimension> sizeStyle;
        FloatSize size;
        FloatSize minSize;
        FloatSize maxSize;
        TaffyPoint<Overflow> overflow;
        TaffyFloat floatMode;
        Clear clear;
        float scrollbarWidth;
        TaffyPosition position;
        TaffyRect<LengthPercentageAuto> inset;
        TaffyRect<LengthPercentageAuto> margin;
        FloatRect padding;
        FloatRect border;
        FloatSize paddingBorderSum;
        FloatSize computedSize;
        FloatPoint staticPosition;
        boolean canBeCollapsedThrough;
        boolean isInSameBfc;
        Layout finalLayout;
    }

    /**
     * Result of performing final layout on in-flow children, including margin collapse info.
     */
    private static class InFlowLayoutResult {
        private final float contentHeight;
        private final CollapsibleMarginSet firstChildTopMarginSet;
        private final CollapsibleMarginSet lastChildBottomMarginSet;
        private final boolean allChildrenCanBeCollapsedThrough;
        private final float firstVerticalBaseline;
        private final float floatedContentHeight;

        public InFlowLayoutResult(
            float contentHeight, CollapsibleMarginSet firstChildTopMarginSet,
            CollapsibleMarginSet lastChildBottomMarginSet,
            boolean allChildrenCanBeCollapsedThrough,
            float firstVerticalBaseline,
            float floatedContentHeight
        ) {
            this.contentHeight = contentHeight;
            this.firstChildTopMarginSet = firstChildTopMarginSet;
            this.lastChildBottomMarginSet = lastChildBottomMarginSet;
            this.allChildrenCanBeCollapsedThrough = allChildrenCanBeCollapsedThrough;
            this.firstVerticalBaseline = firstVerticalBaseline;
            this.floatedContentHeight = floatedContentHeight;
        }

        public float contentHeight() { return contentHeight; }
        public CollapsibleMarginSet firstChildTopMarginSet() { return firstChildTopMarginSet; }
        public CollapsibleMarginSet lastChildBottomMarginSet() { return lastChildBottomMarginSet; }
        public boolean allChildrenCanBeCollapsedThrough() { return allChildrenCanBeCollapsedThrough; }
        public float firstVerticalBaseline() { return firstVerticalBaseline; }
        public float floatedContentHeight() { return floatedContentHeight; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InFlowLayoutResult that = (InFlowLayoutResult) o;
            return Float.compare(contentHeight, that.contentHeight) == 0
                && allChildrenCanBeCollapsedThrough == that.allChildrenCanBeCollapsedThrough
                && Float.compare(firstVerticalBaseline, that.firstVerticalBaseline) == 0
                && Float.compare(floatedContentHeight, that.floatedContentHeight) == 0
                && Objects.equals(firstChildTopMarginSet, that.firstChildTopMarginSet)
                && Objects.equals(lastChildBottomMarginSet, that.lastChildBottomMarginSet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contentHeight, firstChildTopMarginSet,
                lastChildBottomMarginSet, allChildrenCanBeCollapsedThrough, firstVerticalBaseline,
                floatedContentHeight);
        }

        @Override
        public String toString() {
            return "InFlowLayoutResult[contentHeight=" + contentHeight
                + ", firstChildTopMarginSet=" + firstChildTopMarginSet
                + ", lastChildBottomMarginSet=" + lastChildBottomMarginSet
                + ", allChildrenCanBeCollapsedThrough=" + allChildrenCanBeCollapsedThrough
                + ", firstVerticalBaseline=" + firstVerticalBaseline + "]";
        }
    }

    /**
     * Computes block layout for a node.
     */
    public LayoutOutput compute(NodeId node, LayoutInput inputs, TaffyStyle style) {
        return compute(node, inputs, style, null);
    }

    /**
     * Computes block layout using an inherited formatting context when this box remains in the
     * same block formatting context as its parent.
     */
    public LayoutOutput compute(NodeId node, LayoutInput inputs, TaffyStyle style, BlockContext inheritedContext) {
        LayoutPartialTree tree = layoutComputer.getTree();
        FloatSize knownDimensions = inputs.knownDimensions();
        FloatSize parentSize = inputs.parentSize();
        TaffySize<AvailableSpace> availableSpace = inputs.availableSpace();
        RunMode runMode = inputs.runMode();
        TaffyLine<Boolean> verticalMarginsAreCollapsible = inputs.verticalMarginsAreCollapsible();

        float aspectRatio = style.getAspectRatio();
        FloatRect padding = Resolve.resolveRectOrZero(style.getPadding(), parentSize.width, layoutComputer::resolveCalcValue);
        FloatRect border = Resolve.resolveRectOrZero(style.getBorder(), parentSize.width, layoutComputer::resolveCalcValue);

        // Scrollbar gutter calculation - axes are transposed
        TaffyPoint<Overflow> overflow = style.getOverflow();
        float scrollbarWidth = style.getScrollbarWidth();
        float scrollbarGutterX = overflow.y == Overflow.SCROLL ? scrollbarWidth : 0f;
        float scrollbarGutterBottom = overflow.x == Overflow.SCROLL ? scrollbarWidth : 0f;
        boolean isRtl = layoutComputer.resolveDirection(node).isRtl();
        FloatRect scrollbarGutter = new FloatRect(
            isRtl ? scrollbarGutterX : 0f,
            isRtl ? 0f : scrollbarGutterX,
            0f,
            scrollbarGutterBottom
        );

        FloatSize paddingBorderSize = new FloatSize(
            padding.left + padding.right + border.left + border.right,
            padding.top + padding.bottom + border.top + border.bottom
        );

        FloatSize boxSizingAdjustment = style.getBoxSizing() == BoxSizing.CONTENT_BOX
                                        ? paddingBorderSize
                                        : new FloatSize(0f, 0f);

        FloatSize size2 = Resolve.maybeResolveSize(style.getSize(), parentSize, layoutComputer::resolveCalcValue);
        FloatSize sizeStyle = Resolve.maybeApplyAspectRatio(size2, aspectRatio);
        sizeStyle = maybeAdd(sizeStyle, boxSizingAdjustment);

        FloatSize size1 = Resolve.maybeResolveSize(style.getMinSize(), parentSize, layoutComputer::resolveCalcValue);
        FloatSize minSize = Resolve.maybeApplyAspectRatio(size1, aspectRatio);
        minSize = maybeAdd(minSize, boxSizingAdjustment);

        FloatSize size = Resolve.maybeResolveSize(style.getMaxSize(), parentSize, layoutComputer::resolveCalcValue);
        FloatSize maxSize = Resolve.maybeApplyAspectRatio(size, aspectRatio);
        maxSize = maybeAdd(maxSize, boxSizingAdjustment);

        boolean establishesNewBfc = style.getDisplay() == TaffyDisplay.FLOW_ROOT
            || style.getOverflow().x.isScrollContainer()
            || style.getOverflow().y.isScrollContainer()
            || hasNonNormalAlignContent(style.getAlignContent())
            || style.contain.establishesIndependentFormattingContext();

        // Determine margin collapsing behaviour
        boolean ownMarginsCollapseWithChildrenStart =
            verticalMarginsAreCollapsible.start &&
            !establishesNewBfc &&
            !style.getPosition().isOutOfFlow() &&
            padding.top == 0 &&
            border.top == 0;

        boolean ownMarginsCollapseWithChildrenEnd =
            verticalMarginsAreCollapsible.end &&
            !establishesNewBfc &&
            !style.getPosition().isOutOfFlow() &&
            padding.bottom == 0 &&
            border.bottom == 0 &&
            Float.isNaN(sizeStyle.height);

        TaffyLine<Boolean> ownMarginsCollapseWithChildren =
            new TaffyLine<>(ownMarginsCollapseWithChildrenStart, ownMarginsCollapseWithChildrenEnd);

        boolean hasStylesPreventingBeingCollapsedThrough =
            !style.isBlock() ||
            establishesNewBfc ||
            style.getPosition().isOutOfFlow() ||
            (!Float.isNaN(style.getAspectRatio())) ||
            padding.top > 0 ||
            padding.bottom > 0 ||
            border.top > 0 ||
            border.bottom > 0 ||
            (!Float.isNaN(sizeStyle.height) && sizeStyle.height > 0) ||
            (!Float.isNaN(minSize.height) && minSize.height > 0);

        FloatSize clampedStyleSize = inputs.sizingMode() == SizingMode.INHERENT_SIZE
                                     ? maybeClamp(sizeStyle, minSize, maxSize)
                                     : new FloatSize(NaN, NaN);

        // If both min and max are set and max <= min, use min
        FloatSize minMaxDefiniteSize = new FloatSize(
            (!Float.isNaN(minSize.width) && !Float.isNaN(maxSize.width) && maxSize.width <= minSize.width)
            ? minSize.width : NaN,
            (!Float.isNaN(minSize.height) && !Float.isNaN(maxSize.height) && maxSize.height <= minSize.height)
            ? minSize.height : NaN
        );

        FloatSize styledBasedKnownDimensions = orChain(
            knownDimensions,
            minMaxDefiniteSize,
            clampedStyleSize
        );
        styledBasedKnownDimensions = maybeMax(styledBasedKnownDimensions, paddingBorderSize);

        // Short-circuit if size is known and we're only computing size
        if (runMode == RunMode.COMPUTE_SIZE &&
            !Float.isNaN(styledBasedKnownDimensions.width) &&
            !Float.isNaN(styledBasedKnownDimensions.height)) {
            return LayoutOutput.fromOuterSize(styledBasedKnownDimensions);
        }

        // Compute container content box size for resolving child percentages.
        // A supplied block height is a percentage basis only when the caller marks it definite.
        boolean percentageHeightIsDefinite = !Float.isNaN(knownDimensions.height)
            ? Boolean.TRUE.equals(inputs.knownDimensionsAreDefinite().height)
            : !Float.isNaN(minMaxDefiniteSize.height) || !Float.isNaN(clampedStyleSize.height);
        float percentageBasisHeight = percentageHeightIsDefinite ? styledBasedKnownDimensions.height : Float.NaN;
        if (!percentageHeightIsDefinite && !Float.isNaN(aspectRatio) && aspectRatio > 0f
            && !Float.isNaN(styledBasedKnownDimensions.width)
            && Float.isNaN(styledBasedKnownDimensions.height)) {
            percentageHeightIsDefinite = true;
            percentageBasisHeight = (styledBasedKnownDimensions.width - boxSizingAdjustment.width) / aspectRatio
                + boxSizingAdjustment.height;
        }
        float contentBoxInsetWidth = padding.left + padding.right + border.left + border.right
                                     + scrollbarGutter.left + scrollbarGutter.right;
        float contentBoxInsetHeight = padding.top + padding.bottom + border.top + border.bottom
                                     + scrollbarGutter.top + scrollbarGutter.bottom;
        FloatSize containerContentBoxSize = new FloatSize(
            !Float.isNaN(styledBasedKnownDimensions.width) ? styledBasedKnownDimensions.width - contentBoxInsetWidth : NaN,
            !Float.isNaN(percentageBasisHeight) ? percentageBasisHeight - contentBoxInsetHeight : NaN
        );

        // Generate item list
        List<BlockItem> items = generateItemList(node, containerContentBoxSize);

        // Compute container width
        float containerOuterWidth = styledBasedKnownDimensions.width;
        if (Float.isNaN(containerOuterWidth)) {
            // Content box inset includes scrollbar gutter (matches Rust's content_box_inset)
            float contentBoxInsetH = padding.left + padding.right + border.left + border.right
                                     + scrollbarGutter.left + scrollbarGutter.right;
            AvailableSpace availableWidth = subtractFromAvailable(
                availableSpace.width, contentBoxInsetH);
            float intrinsicWidth = determineContentBasedContainerWidth(items, availableWidth)
                                   + contentBoxInsetH;
            containerOuterWidth = TaffyMath.clamp(intrinsicWidth, minSize.width, maxSize.width);
            containerOuterWidth = Math.max(containerOuterWidth, !Float.isNaN(paddingBorderSize.width) ? paddingBorderSize.width : 0);
        }

        // Short-circuit if computing size and both dimensions known
        if (runMode == RunMode.COMPUTE_SIZE && !Float.isNaN(styledBasedKnownDimensions.height)) {
            return LayoutOutput.fromOuterSize(new FloatSize(containerOuterWidth, styledBasedKnownDimensions.height));
        }

        // Perform final layout on children
        float percentageResolutionWidth = Float.isNaN(parentSize.width) ? containerOuterWidth : parentSize.width;
        FloatRect resolvedPadding = Resolve.resolveRectOrZero(style.getPadding(), percentageResolutionWidth, layoutComputer::resolveCalcValue);
        FloatRect resolvedBorder = Resolve.resolveRectOrZero(style.getBorder(), percentageResolutionWidth, layoutComputer::resolveCalcValue);
        FloatRect contentBoxInset = new FloatRect(
            resolvedPadding.left + resolvedBorder.left + scrollbarGutter.left,
            resolvedPadding.right + resolvedBorder.right + scrollbarGutter.right,
            resolvedPadding.top + resolvedBorder.top + scrollbarGutter.top,
            resolvedPadding.bottom + resolvedBorder.bottom + scrollbarGutter.bottom
        );

        float containerInnerWidth = Math.max(0f, containerOuterWidth - contentBoxInset.left - contentBoxInset.right);
        BlockContext blockContext = inheritedContext == null || establishesNewBfc
            ? BlockContext.root(containerInnerWidth)
            : inheritedContext.withContentBoxInsets(contentBoxInset.left, contentBoxInset.right);
        if (blockContext.isRoot()) {
            blockContext.setWidth(containerInnerWidth);
        }

        InFlowLayoutResult layoutResult = performFinalLayoutOnChildren(
            items,
            containerOuterWidth,
            percentageBasisHeight,
            contentBoxInset,
            style.getTextAlign(),
            layoutComputer.resolveDirection(node),
            ownMarginsCollapseWithChildren,
            blockContext
        );

        float intrinsicOuterHeight = layoutResult.contentHeight();
        if (establishesNewBfc) {
            intrinsicOuterHeight = Math.max(intrinsicOuterHeight, layoutResult.floatedContentHeight());
        }
        if (blockContext.isRoot()) {
            intrinsicOuterHeight = Math.max(intrinsicOuterHeight, blockContext.floatedContentHeight());
        }

        float containerOuterHeight = styledBasedKnownDimensions.height;
        if (Float.isNaN(containerOuterHeight)) {
            float contentHeight = intrinsicOuterHeight;
            // Apply aspect-ratio: when height is auto and width is known, derive height from AR.
            // AR applies to the content box, so subtract/add box-sizing adjustment.
            if (!Float.isNaN(aspectRatio) && !Float.isNaN(containerOuterWidth)) {
                float arDerivedHeight = (containerOuterWidth - boxSizingAdjustment.width) / aspectRatio
                                        + boxSizingAdjustment.height;
                contentHeight = Math.max(contentHeight, arDerivedHeight);
            }
            containerOuterHeight = TaffyMath.clamp(contentHeight, minSize.height, maxSize.height);
            containerOuterHeight = Math.max(containerOuterHeight, !Float.isNaN(paddingBorderSize.height) ? paddingBorderSize.height : 0);
        }

        FloatSize finalOuterSize = new FloatSize(containerOuterWidth, containerOuterHeight);

        boolean heightConstrainedByMinHeight =
            !Float.isNaN(minSize.height) && minSize.height > 0f && minSize.height >= containerOuterHeight;
        boolean ownBottomMarginCollapsesWithChildren =
            ownMarginsCollapseWithChildren.end && !heightConstrainedByMinHeight;

        if (runMode == RunMode.COMPUTE_SIZE) {
            return LayoutOutput.fromOuterSize(finalOuterSize);
        }

        float firstVerticalBaseline = layoutResult.firstVerticalBaseline();
        if (hasNonNormalAlignContent(style.getAlignContent())) {
            float containerInnerHeight = finalOuterSize.height - contentBoxInset.top - contentBoxInset.bottom;
            float inFlowContentHeight = intrinsicOuterHeight - contentBoxInset.top - contentBoxInset.bottom;
            float groupOffset = blockAlignmentOffset(
                style.getAlignContent(), containerInnerHeight - inFlowContentHeight);
            boolean hasDeferredLayout = false;
            for (BlockItem item : items) {
                if (item.finalLayout != null) {
                    item.finalLayout.location().y += groupOffset;
                    hasDeferredLayout = true;
                }
            }
            if (hasDeferredLayout && !Float.isNaN(firstVerticalBaseline)) {
                firstVerticalBaseline += groupOffset;
            }
        }

        for (BlockItem item : items) {
            if (item.finalLayout != null) {
                tree.setUnroundedLayout(item.nodeId, item.finalLayout);
            }
        }

        // Layout absolutely positioned children
        // Absolute position inset is border + scrollbar gutter (not padding), per CSS spec
        FloatRect absolutePositionInset = new FloatRect(
            resolvedBorder.left + scrollbarGutter.left,
            resolvedBorder.right + scrollbarGutter.right,
            resolvedBorder.top + scrollbarGutter.top,
            resolvedBorder.bottom + scrollbarGutter.bottom
        );
        performAbsoluteLayoutOnChildren(
            items,
            finalOuterSize,
            absolutePositionInset,
            layoutComputer.resolveDirection(node)
        );

        // Layout hidden children
        for (BlockItem item : items) {
            TaffyStyle childStyle = tree.getStyle(item.nodeId);
            if (childStyle.getBoxGenerationMode() == BoxGenerationMode.NONE) {
                tree.setUnroundedLayout(item.nodeId, Layout.withOrder(item.order));
                layoutComputer.computeChildLayout(item.nodeId, LayoutInput.hidden());
            }
        }

        FloatSize contentSize = computeContentSizeFromChildren(node);
        FloatRect scrollableOverflowRect = ScrollableOverflow.fromChildren(
            tree,
            node,
            finalOuterSize,
            resolvedBorder,
            resolvedPadding,
            new FloatSize(scrollbarGutterX, scrollbarGutterBottom),
            layoutComputer.resolveDirection(node),
            overflow
        );

        // Determine whether this node can be collapsed through
        boolean canBeCollapsedThrough = !hasStylesPreventingBeingCollapsedThrough &&
                                        layoutResult.allChildrenCanBeCollapsedThrough;

        // Compute output margin sets
        CollapsibleMarginSet topMargin;
        if (ownMarginsCollapseWithChildren.start) {
            topMargin = layoutResult.firstChildTopMarginSet;
        } else {
            float marginTop = Resolve.resolveLpaOrZero(style.getMargin().top, parentSize.width, layoutComputer::resolveCalcValue);
            topMargin = CollapsibleMarginSet.fromMargin(marginTop);
        }

        CollapsibleMarginSet bottomMargin;
        if (ownBottomMarginCollapsesWithChildren) {
            bottomMargin = layoutResult.lastChildBottomMarginSet;
        } else {
            float marginBottom = Resolve.resolveLpaOrZero(style.getMargin().bottom, parentSize.width, layoutComputer::resolveCalcValue);
            bottomMargin = CollapsibleMarginSet.fromMargin(marginBottom);
        }

        firstVerticalBaseline = style.contain.suppressesBaseline()
            ? NaN
            : firstVerticalBaseline;
        if (inheritedContext != null && !establishesNewBfc && blockContext != inheritedContext) {
            inheritedContext.mergeTopAdjoiningFloats(blockContext);
        }
        return new LayoutOutput(
            finalOuterSize,
            contentSize,
            new FloatPoint(NaN, firstVerticalBaseline),
            topMargin,
            bottomMargin,
            canBeCollapsedThrough,
            scrollableOverflowRect,
            Baselines.fromLegacy(firstVerticalBaseline)
        );
    }

    private static boolean hasNonNormalAlignContent(AlignContent alignContent) {
        return alignContent != null && alignContent != AlignContent.AUTO;
    }

    private static float blockAlignmentOffset(AlignContent alignment, float freeSpace) {
        boolean safe = alignment.isSafe();
        AlignContent keyword = alignment.withoutSafety();
        if (keyword == AlignContent.STRETCH || keyword == AlignContent.SPACE_BETWEEN) {
            keyword = AlignContent.START;
            safe = true;
        } else if (keyword == AlignContent.SPACE_AROUND || keyword == AlignContent.SPACE_EVENLY) {
            keyword = AlignContent.CENTER;
            safe = true;
        }
        if (safe && freeSpace <= 0f) return 0f;
        return switch (keyword) {
            case CENTER -> freeSpace / 2f;
            case END, FLEX_END -> freeSpace;
            default -> 0f;
        };
    }

    private FloatSize computeContentSizeFromChildren(NodeId node) {
        LayoutPartialTree tree = layoutComputer.getTree();
        FloatSize contentSize = FloatSize.zero();

        for (NodeId childId : tree.getChildren(node)) {
            TaffyStyle childStyle = tree.getStyle(childId);
            if (childStyle.getBoxGenerationMode() == BoxGenerationMode.NONE) continue;

            Layout childLayout = tree.getUnroundedLayout(childId);
            if (childLayout == null) continue;

            FloatSize contribution = ContentSizeUtil.computeContentSizeContribution(
                childLayout.location(),
                childLayout.size(),
                childLayout.scrollableOverflowRect(),
                childStyle.getOverflow(),
                childStyle.contain
            );
            contentSize = ContentSizeUtil.max(contentSize, contribution);
        }

        return contentSize;
    }

    private List<BlockItem> generateItemList(NodeId node, FloatSize nodeInnerSize) {
        LayoutPartialTree tree = layoutComputer.getTree();
        List<BlockItem> items = new ArrayList<>();

        int order = 0;
        for (NodeId childId : tree.getChildren(node)) {
            TaffyStyle childStyle = tree.getStyle(childId);
            if (childStyle.getBoxGenerationMode() == BoxGenerationMode.NONE) {
                order++;
                continue;
            }

            BlockItem item = new BlockItem();
            item.nodeId = childId;
            item.order = order++;

            float aspectRatio = childStyle.getAspectRatio();
            FloatRect itemPadding = Resolve.resolveRectOrZero(childStyle.getPadding(), nodeInnerSize.width, layoutComputer::resolveCalcValue);
            FloatRect itemBorder = Resolve.resolveRectOrZero(childStyle.getBorder(), nodeInnerSize.width, layoutComputer::resolveCalcValue);
            item.padding = itemPadding;
            item.border = itemBorder;
            item.paddingBorderSum = new FloatSize(
                itemPadding.left + itemPadding.right + itemBorder.left + itemBorder.right,
                itemPadding.top + itemPadding.bottom + itemBorder.top + itemBorder.bottom
            );

            FloatSize boxSizingAdj = childStyle.getBoxSizing() == BoxSizing.CONTENT_BOX
                                     ? item.paddingBorderSum
                                     : new FloatSize(0f, 0f);

            FloatSize size2 = Resolve.maybeResolveSize(childStyle.getSize(), nodeInnerSize, layoutComputer::resolveCalcValue);
            item.size = maybeAdd(Resolve.maybeApplyAspectRatio(size2, aspectRatio), boxSizingAdj);
            FloatSize size1 = Resolve.maybeResolveSize(childStyle.getMinSize(), nodeInnerSize, layoutComputer::resolveCalcValue);
            item.minSize = maybeAdd(Resolve.maybeApplyAspectRatio(size1, aspectRatio), boxSizingAdj);
            FloatSize size = Resolve.maybeResolveSize(childStyle.getMaxSize(), nodeInnerSize, layoutComputer::resolveCalcValue);
            item.maxSize = maybeAdd(Resolve.maybeApplyAspectRatio(size, aspectRatio), boxSizingAdj);

            item.overflow = childStyle.getOverflow();
            item.floatMode = childStyle.getFloatMode();
            item.clear = childStyle.getClear();
            item.scrollbarWidth = childStyle.getScrollbarWidth();
            item.position = childStyle.getPosition();
            item.inset = childStyle.getInset();
            item.margin = childStyle.getMargin();
            item.computedSize = new FloatSize(0f, 0f);
            item.staticPosition = new FloatPoint(0f, 0f);
            item.canBeCollapsedThrough = false;
            item.isTable = childStyle.getItemIsTable();
            item.isReplaced = childStyle.getItemIsReplaced();
            item.sizeStyle = childStyle.getSize();
            boolean isScrollContainer = item.overflow.x.isScrollContainer() || item.overflow.y.isScrollContainer();
            item.isInSameBfc = childStyle.getDisplay() == TaffyDisplay.BLOCK
                && !item.isTable
                && !item.position.isOutOfFlow()
                && (item.floatMode == null || !item.floatMode.isFloated())
                && !isScrollContainer
                && !hasNonNormalAlignContent(childStyle.getAlignContent())
                && !childStyle.contain.establishesIndependentFormattingContext();

            items.add(item);
        }

        return items;
    }

    private float determineContentBasedContainerWidth(List<BlockItem> items, AvailableSpace availableWidth) {
        float maxChildWidth = 0f;
        FloatIntrinsicWidthCalculator floatContribution = new FloatIntrinsicWidthCalculator(availableWidth);

        for (BlockItem item : items) {
            if (item.position.isOutOfFlow()) continue;

            FloatSize knownDimensions = maybeClamp(item.size, item.minSize, item.maxSize);

            float width;
            if (!Float.isNaN(knownDimensions.width)) {
                width = knownDimensions.width;
            } else {
                float marginSum = resolveMarginSumOrZero(item.margin, availableWidth.isDefinite() ? availableWidth.getValue() : NaN);
                AvailableSpace adjustedAvailable = subtractFromAvailable(availableWidth, marginSum);

                LayoutOutput output = layoutComputer.performChildLayout(
                    item.nodeId,
                    knownDimensions,
                    new FloatSize(NaN, NaN),
                    new TaffySize<>(adjustedAvailable, AvailableSpace.minContent()),
                    SizingMode.INHERENT_SIZE,
                    new TaffyLine<>(true, true)
                );
                width = output.size().width + marginSum;
            }
            width = Math.max(width, item.paddingBorderSum.width);
            if (item.floatMode != null && item.floatMode.isFloated()) {
                floatContribution.addFloat(width, item.floatMode.floatDirection(), item.clear);
                continue;
            }
            maxChildWidth = Math.max(maxChildWidth, width);
        }

        return Math.max(maxChildWidth, floatContribution.result());
    }

    /** Finds the first float-avoiding content slot wide enough for the supplied margin box. */
    private ContentSlot findContentSlotForBox(
        BlockContext blockContext,
        float minY,
        float outerWidth,
        Clear clear) {
        ContentSlot slot = blockContext.findContentSlot(minY, clear, null);
        while (outerWidth > slot.width + 0.001f && slot.segmentId != null) {
            ContentSlot next = blockContext.findContentSlot(minY, clear, slot.segmentId);
            if (next.y <= slot.y + 0.001f) break;
            slot = next;
        }
        return slot;
    }

    /** Finds the first independent-formatting-context slot that can contain the border box. */
    private BfcSlot findBfcSlotForBox(
        BlockContext blockContext,
        float minY,
        FloatRect margins,
        FloatSize size,
        FloatSize minSize,
        FloatSize maxSize,
        TaffyDirection direction,
        Clear clear) {
        float marginSum = margins.left + margins.right;
        float minimumAutoWidth = -marginSum;
        BfcSlot slot = blockContext.findBfcSlot(
            minY, new float[] {margins.left, margins.right}, direction, clear, null);
        while (slot.segmentId != null) {
            float width = Float.isNaN(size.width) ? Math.max(slot.stretchWidth, minimumAutoWidth) : size.width;
            width = TaffyMath.clamp(width, minSize.width, maxSize.width);
            if (width <= slot.borderWidth + 0.001f) return slot;
            BfcSlot next = blockContext.findBfcSlot(
                minY, new float[] {margins.left, margins.right}, direction, clear, slot.segmentId);
            if (next.y <= slot.y + 0.001f) return slot;
            slot = next;
        }
        return slot;
    }

    private InFlowLayoutResult performFinalLayoutOnChildren(
        List<BlockItem> items,
        float containerOuterWidth,
        float containerPercentageResolutionHeight,
        FloatRect contentBoxInset,
        TextAlign textAlign,
        TaffyDirection direction,
        TaffyLine<Boolean> ownMarginsCollapseWithChildren,
        BlockContext blockContext) {

        LayoutPartialTree tree = layoutComputer.getTree();
        float containerInnerWidth = containerOuterWidth - contentBoxInset.left - contentBoxInset.right;
        float percentageResolutionHeight = Float.isNaN(containerPercentageResolutionHeight)
            ? NaN
            : containerPercentageResolutionHeight - contentBoxInset.top - contentBoxInset.bottom;
        FloatSize parentSize = new FloatSize(containerInnerWidth, percentageResolutionHeight);
        TaffySize<AvailableSpace> availableSpace = new TaffySize<>(
            AvailableSpace.definite(containerInnerWidth),
            AvailableSpace.maxContent()
        );

        float committedYOffset = contentBoxInset.top;
        float yOffsetForAbsolute = contentBoxInset.top;
        CollapsibleMarginSet firstChildTopMarginSet = CollapsibleMarginSet.zero();
        CollapsibleMarginSet activeCollapsibleMarginSet = CollapsibleMarginSet.zero();
        boolean isCollapsingWithFirstMarginSet = true;
        boolean allChildrenCanBeCollapsedThrough = true;
        float firstVerticalBaseline = NaN;
        float floatedContentHeight = Float.NEGATIVE_INFINITY;
        boolean hasActiveFloats = blockContext.hasActiveFloats(committedYOffset);
        boolean activeMarginSetHasClearance = false;

        // Check RTL once at the start
        boolean isRtl = direction != null && direction.isRtl();
        for (BlockItem item : items) {
            if (item.position.isOutOfFlow()) {
                // In RTL, static position starts from right
                float staticX = isRtl
                    ? (containerOuterWidth - contentBoxInset.right)
                    : contentBoxInset.left;
                item.staticPosition = new FloatPoint(staticX, yOffsetForAbsolute);
                continue;
            }

            // Resolve margins
            FloatRect itemMarginOpt = resolveMarginOptional(item.margin, containerOuterWidth);
            FloatRect itemNonAutoMargin = new FloatRect(
                Float.isNaN(itemMarginOpt.left) ? 0f : itemMarginOpt.left,
                Float.isNaN(itemMarginOpt.right) ? 0f : itemMarginOpt.right,
                Float.isNaN(itemMarginOpt.top) ? 0f : itemMarginOpt.top,
                Float.isNaN(itemMarginOpt.bottom) ? 0f : itemMarginOpt.bottom
            );
            float itemNonAutoXMarginSum = itemNonAutoMargin.left + itemNonAutoMargin.right;

            if (item.floatMode != null && item.floatMode.isFloated()) {
                float availableFloatWidth = Math.max(0f, containerInnerWidth - itemNonAutoXMarginSum);
                float knownFloatWidth = resolveBlockWidth(item, availableFloatWidth, containerInnerWidth, parentSize, availableSpace.height);
                if (Float.isNaN(knownFloatWidth)) {
                    knownFloatWidth = NaN;
                }
                float knownFloatHeight = resolveStretchHeight(item, percentageResolutionHeight, itemNonAutoMargin);
                FloatSize floatKnownDimensions = maybeClamp(
                    new FloatSize(knownFloatWidth, knownFloatHeight), item.minSize, item.maxSize
                );
                AvailableSpace floatWidthSpace = Float.isNaN(knownFloatWidth)
                    ? measurementSpace(item.sizeStyle.width, availableFloatWidth)
                    : AvailableSpace.definite(knownFloatWidth);
                LayoutOutput floatOutput = layoutComputer.performChildLayout(
                    item.nodeId,
                    floatKnownDimensions,
                    parentSize,
                    new TaffySize<>(floatWidthSpace, availableSpace.height),
                    SizingMode.INHERENT_SIZE,
                    TaffyLine.FALSE
                );
                FloatSize floatSize = floatOutput.size();
                float marginBoxWidth = floatSize.width + itemNonAutoXMarginSum;
                float marginBoxHeight = floatSize.height + itemNonAutoMargin.top + itemNonAutoMargin.bottom;
                boolean adjoinsUnresolvedStrut = isCollapsingWithFirstMarginSet
                    && ownMarginsCollapseWithChildren.start;
                FloatPoint marginBoxPosition = blockContext.placeFloatedBox(
                    new FloatSize(marginBoxWidth, marginBoxHeight),
                    adjoinsUnresolvedStrut
                        ? committedYOffset
                        : committedYOffset + activeCollapsibleMarginSet.resolve(),
                    item.floatMode.floatDirection(),
                    item.clear,
                    adjoinsUnresolvedStrut
                );
                float floatX = marginBoxPosition.x + itemNonAutoMargin.left;
                float floatY = marginBoxPosition.y + itemNonAutoMargin.top;
                FloatSize floatScrollbar = new FloatSize(
                    item.overflow.y == Overflow.SCROLL ? item.scrollbarWidth : 0f,
                    item.overflow.x == Overflow.SCROLL ? item.scrollbarWidth : 0f
                );
                item.finalLayout = new Layout(
                    item.order,
                    new FloatPoint(contentBoxInset.left + floatX, floatY),
                    floatSize,
                    floatOutput.contentSize(),
                    floatScrollbar,
                    item.border,
                    item.padding,
                    itemNonAutoMargin,
                    floatOutput.scrollableOverflowRect(),
                    floatOutput.baselines()
                );
                floatedContentHeight = Math.max(floatedContentHeight, marginBoxPosition.y + marginBoxHeight);
                hasActiveFloats = true;
                item.computedSize = floatSize;
                item.canBeCollapsedThrough = false;
                continue;
            }

            float yMarginOffset = !item.isInSameBfc && (!isCollapsingWithFirstMarginSet || !ownMarginsCollapseWithChildren.start)
                ? activeCollapsibleMarginSet.copy()
                    .collapseWithMargin(itemNonAutoMargin.top).resolve()
                : 0f;
            float minimumY = committedYOffset + yMarginOffset;
            boolean hasActiveFloatsBeforeItem = hasActiveFloats || blockContext.hasActiveFloats(minimumY);
            boolean itemAvoidsFloats = !item.isInSameBfc && hasActiveFloatsBeforeItem;
            boolean itemPushedBelowFloat = false;
            ContentSlot flowSlot = null;
            BfcSlot bfcSlot = null;
            float stretchWidth;
            if (itemAvoidsFloats) {
                bfcSlot = findBfcSlotForBox(
                    blockContext,
                    minimumY,
                    itemNonAutoMargin,
                    item.size,
                    item.minSize,
                    item.maxSize,
                    direction,
                    item.clear
                );
                itemPushedBelowFloat = bfcSlot.y > minimumY + 0.001f;
                hasActiveFloats = bfcSlot.segmentId != null;
                stretchWidth = Math.max(bfcSlot.stretchWidth, -itemNonAutoXMarginSum);
            } else {
                if (item.isInSameBfc) {
                    // A block in the same formatting context keeps its own full-width box.
                    // Floats constrain descendants through the shared BlockContext, not this box.
                    stretchWidth = containerInnerWidth - itemNonAutoXMarginSum;
                } else {
                    float requestedOuterWidth = Float.isNaN(item.size.width)
                        ? 0f : item.size.width + itemNonAutoXMarginSum;
                    flowSlot = findContentSlotForBox(blockContext, minimumY, requestedOuterWidth, item.clear);
                    stretchWidth = containerInnerWidth - itemNonAutoXMarginSum;
                }
            }

            FloatSize knownDimensions;
            if (item.isTable || item.isReplaced) {
                knownDimensions = new FloatSize(NaN, NaN);
            } else {
                float width = resolveBlockWidth(item, stretchWidth, containerInnerWidth, parentSize, availableSpace.height);
                if (Float.isNaN(width)) width = stretchWidth;
                float height = resolveStretchHeight(item, percentageResolutionHeight, itemNonAutoMargin);
                knownDimensions = maybeClamp(new FloatSize(width, height), item.minSize, item.maxSize);
            }

            BlockContext childBlockContext = blockContext.subContext(
                yOffsetForAbsolute + itemNonAutoMargin.top,
                itemNonAutoMargin.left,
                itemNonAutoMargin.right
            );
            LayoutOutput itemOutput = layoutComputer.performBlockChildLayout(
                item.nodeId,
                knownDimensions,
                parentSize,
                new TaffySize<>(
                    itemAvoidsFloats ? AvailableSpace.definite(stretchWidth)
                        : subtractFromAvailable(availableSpace.width, itemNonAutoXMarginSum),
                    availableSpace.height
                ),
                SizingMode.INHERENT_SIZE,
                item.isInSameBfc ? new TaffyLine<>(true, true) : TaffyLine.FALSE,
                childBlockContext
            );
            if (item.isInSameBfc) {
                blockContext.mergeTopAdjoiningFloats(childBlockContext);
                hasActiveFloats = hasActiveFloats || blockContext.hasActiveFloats(committedYOffset);
            }

            FloatSize finalSize = itemOutput.size();

            if (!itemAvoidsFloats && !item.isInSameBfc) {
                flowSlot = findContentSlotForBox(
                    blockContext,
                    minimumY,
                    finalSize.width + itemNonAutoXMarginSum,
                    item.clear
                );
            }

            // Get margin collapse info from child layout
            CollapsibleMarginSet topMarginSet = itemOutput.topMargin().copy()
                                                          .collapseWithMargin(!Float.isNaN(itemMarginOpt.top) ? itemMarginOpt.top : 0f);
            CollapsibleMarginSet bottomMarginSet = itemOutput.bottomMargin().copy()
                                                             .collapseWithMargin(!Float.isNaN(itemMarginOpt.bottom) ? itemMarginOpt.bottom : 0f);

            // Expand auto margins
            float freeXSpace = Math.max(0, containerInnerWidth - finalSize.width - itemNonAutoXMarginSum);
            int autoMarginCount = (item.margin.left.isAuto() ? 1 : 0) + (item.margin.right.isAuto() ? 1 : 0);
            float xAxisAutoMarginSize = autoMarginCount > 0 ? freeXSpace / autoMarginCount : 0;

            FloatRect resolvedMargin = new FloatRect(
                Float.isNaN(itemMarginOpt.left) ? xAxisAutoMarginSize : itemMarginOpt.left,
                Float.isNaN(itemMarginOpt.right) ? xAxisAutoMarginSize : itemMarginOpt.right,
                topMarginSet.resolve(),
                bottomMarginSet.resolve()
            );
            if (item.isInSameBfc && (!isCollapsingWithFirstMarginSet || !ownMarginsCollapseWithChildren.start)) {
                yMarginOffset = activeCollapsibleMarginSet.copy().collapseWithSet(topMarginSet).resolve();
            } else if (!item.isInSameBfc && !(isCollapsingWithFirstMarginSet && ownMarginsCollapseWithChildren.start)) {
                yMarginOffset = activeCollapsibleMarginSet.copy().collapseWithMargin(resolvedMargin.top).resolve();
            }

            float insetOffsetX = 0f;
            float insetOffsetY = 0f;
            if (item.position == TaffyPosition.RELATIVE) {
                float insetLeft = item.inset.left.maybeResolve(containerInnerWidth, layoutComputer::resolveCalcValue);
                float insetRight = item.inset.right.maybeResolve(containerInnerWidth, layoutComputer::resolveCalcValue);
                float insetTop = item.inset.top.maybeResolve(percentageResolutionHeight, layoutComputer::resolveCalcValue);
                float insetBottom = item.inset.bottom.maybeResolve(percentageResolutionHeight, layoutComputer::resolveCalcValue);
                insetOffsetX = direction.isRtl() && !Float.isNaN(insetRight)
                               ? -insetRight
                               : (!Float.isNaN(insetLeft) ? insetLeft : (!Float.isNaN(insetRight) ? -insetRight : 0f));
                insetOffsetY = !Float.isNaN(insetTop) ? insetTop : (!Float.isNaN(insetBottom) ? -insetBottom : 0f);
            }

            // Compute y margin offset with margin collapse
            boolean hasClearance = false;
            if (item.isInSameBfc) {
                float clearThreshold = blockContext.clearedThreshold(item.clear);
                float hypotheticalY = committedYOffset + activeCollapsibleMarginSet.copy()
                    .collapseWithSet(topMarginSet).resolve();
                if (!Float.isInfinite(clearThreshold)
                    && (blockContext.hasAdjoiningFloat(item.clear) || hypotheticalY < clearThreshold)) {
                    hasClearance = true;
                    yMarginOffset = clearThreshold - committedYOffset;
                }
            }

            item.computedSize = finalSize;
            item.canBeCollapsedThrough = itemOutput.marginsCanCollapseThrough() && !hasClearance;

            // Update static position for RTL
            float staticX = isRtl
                ? (containerOuterWidth - contentBoxInset.right)
                : contentBoxInset.left;
            item.staticPosition = new FloatPoint(
                staticX,
                committedYOffset + activeCollapsibleMarginSet.resolve()
            );

            float y = itemAvoidsFloats
                ? bfcSlot.y + insetOffsetY
                : (!item.isInSameBfc && hasActiveFloatsBeforeItem)
                ? Math.max(committedYOffset + insetOffsetY + yMarginOffset, flowSlot.y + insetOffsetY)
                : committedYOffset + insetOffsetY + yMarginOffset;

            // Calculate x position based on direction
            float itemOuterWidth = finalSize.width + resolvedMargin.left + resolvedMargin.right;
            float freeSpace = containerInnerWidth - itemOuterWidth;
            float x;

            if (isRtl) {
                // RTL: Default alignment is to the right (START in RTL)
                // Calculate x so item aligns to right edge by default
                x = contentBoxInset.left + freeSpace + resolvedMargin.left + insetOffsetX;

                // Apply text alignment adjustments for RTL
                if (itemOuterWidth < containerInnerWidth) {
                    switch (textAlign) {
                        case LEFT:
                        case END:
                            // Align to left (end in RTL) - subtract freeSpace from the right-aligned position
                            x = contentBoxInset.left + resolvedMargin.left + insetOffsetX;
                            break;
                        case CENTER:
                            // Center alignment
                            x = contentBoxInset.left + freeSpace / 2 + resolvedMargin.left + insetOffsetX;
                            break;
                        default:
                            // START, RIGHT, or default - stay right-aligned (already calculated above)
                            break;
                    }
                }
            } else {
                // LTR: Default alignment is to the left
                x = contentBoxInset.left + insetOffsetX + resolvedMargin.left;

                // Apply text alignment adjustments for LTR
                if (itemOuterWidth < containerInnerWidth) {
                    switch (textAlign) {
                        case RIGHT:
                        case END:
                            x += freeSpace;
                            break;
                        case CENTER:
                            x += freeSpace / 2;
                            break;
                        default:
                            break;
                    }
                }
            }

            if (itemAvoidsFloats) {
                x = isRtl
                    ? contentBoxInset.left + bfcSlot.x + bfcSlot.borderWidth - finalSize.width + insetOffsetX
                    : contentBoxInset.left + bfcSlot.x + insetOffsetX;
            } else if (!item.isInSameBfc && hasActiveFloatsBeforeItem) {
                x = isRtl
                    ? contentBoxInset.left + flowSlot.x + flowSlot.width - finalSize.width
                        - resolvedMargin.right + insetOffsetX
                        : contentBoxInset.left + flowSlot.x + resolvedMargin.left + insetOffsetX;
            }

            if (isRtl && x < 0f && x > -1f) {
                x = 0f;
            }

            FloatSize scrollbarSize = new FloatSize(
                item.overflow.y == Overflow.SCROLL ? item.scrollbarWidth : 0f,
                item.overflow.x == Overflow.SCROLL ? item.scrollbarWidth : 0f
            );

            Layout layout = new Layout(
                item.order,
                new FloatPoint(x, y),
                finalSize,
                itemOutput.contentSize(),
                scrollbarSize,
                item.border,
                item.padding,
                resolvedMargin,
                itemOutput.scrollableOverflowRect(),
                itemOutput.baselines()
            );

            item.finalLayout = layout;

            if (Float.isNaN(firstVerticalBaseline)) {
                float childBaseline = itemOutput.baselines().first();
                if (item.overflow.y.isScrollContainer()) {
                    childBaseline = Float.isNaN(childBaseline) ? finalSize.height : childBaseline;
                    childBaseline = Math.max(0f, Math.min(childBaseline, finalSize.height));
                }
                if (!Float.isNaN(childBaseline)) {
                    firstVerticalBaseline = y + childBaseline;
                }
            }

            // Update first_child_top_margin_set
            if (isCollapsingWithFirstMarginSet && itemPushedBelowFloat) {
                isCollapsingWithFirstMarginSet = false;
            }
            if (isCollapsingWithFirstMarginSet) {
                if (hasClearance) {
                    isCollapsingWithFirstMarginSet = false;
                } else if (item.canBeCollapsedThrough) {
                    firstChildTopMarginSet
                        .collapseWithSet(topMarginSet)
                        .collapseWithSet(bottomMarginSet);
                } else {
                    firstChildTopMarginSet.collapseWithSet(topMarginSet);
                    isCollapsingWithFirstMarginSet = false;
                }
            }

            // Update active_collapsible_margin_set
            if (item.canBeCollapsedThrough) {
                activeCollapsibleMarginSet
                    .collapseWithSet(topMarginSet)
                    .collapseWithSet(bottomMarginSet);
                activeMarginSetHasClearance = false;
                yOffsetForAbsolute = committedYOffset + finalSize.height + yMarginOffset;
            } else {
                committedYOffset = y - insetOffsetY + finalSize.height;
                if (hasClearance && itemOutput.marginsCanCollapseThrough()) {
                    committedYOffset -= topMarginSet.resolve();
                    activeCollapsibleMarginSet = topMarginSet.copy().collapseWithSet(bottomMarginSet);
                    activeMarginSetHasClearance = true;
                } else {
                    activeCollapsibleMarginSet = bottomMarginSet;
                    activeMarginSetHasClearance = false;
                }
                yOffsetForAbsolute = committedYOffset + activeCollapsibleMarginSet.resolve();
                allChildrenCanBeCollapsedThrough = false;
                blockContext.commitStrut();
            }
        }

        CollapsibleMarginSet lastChildBottomMarginSet = activeCollapsibleMarginSet;
        if (activeMarginSetHasClearance) {
            lastChildBottomMarginSet = CollapsibleMarginSet.zero();
        }
        float bottomYMarginOffset = activeMarginSetHasClearance
                                    ? activeCollapsibleMarginSet.resolve()
                                    : ownMarginsCollapseWithChildren.end ? 0f : lastChildBottomMarginSet.resolve();

        committedYOffset += contentBoxInset.bottom + bottomYMarginOffset;
        float contentHeight = Math.max(0f, committedYOffset);

        return new InFlowLayoutResult(
            contentHeight,
            firstChildTopMarginSet,
            lastChildBottomMarginSet,
            allChildrenCanBeCollapsedThrough,
            firstVerticalBaseline,
            floatedContentHeight
        );
    }

    /**
     * Resolve margins returning null for auto margins.
     */
    private FloatRect resolveMarginOptional(TaffyRect<LengthPercentageAuto> margin, float contextWidth) {
        return new FloatRect(
            margin.left.isAuto() ? NaN : margin.left.maybeResolve(contextWidth, layoutComputer::resolveCalcValue),
            margin.right.isAuto() ? NaN : margin.right.maybeResolve(contextWidth, layoutComputer::resolveCalcValue),
            margin.top.isAuto() ? NaN : margin.top.maybeResolve(contextWidth, layoutComputer::resolveCalcValue),
            margin.bottom.isAuto() ? NaN : margin.bottom.maybeResolve(contextWidth, layoutComputer::resolveCalcValue)
        );
    }

    private float resolveBlockWidth(
        BlockItem item,
        float stretchWidth,
        float percentageBasisWidth,
        FloatSize parentSize,
        AvailableSpace verticalAvailableSpace) {
        TaffyDimension widthStyle = item.sizeStyle.width;
        if (widthStyle.isStretch()) return stretchWidth;
        if (widthStyle.isMinContent() || widthStyle.isMaxContent() || widthStyle.isFitContent()) {
            AvailableSpace constraint = measurementSpace(widthStyle, stretchWidth);
            FloatSize measured = layoutComputer.measureChildSize(
                item.nodeId,
                FloatSize.none(),
                parentSize,
                new TaffySize<>(constraint, verticalAvailableSpace),
                SizingMode.INHERENT_SIZE,
                TaffyLine.TRUE
            );
            return measured.width;
        }
        return item.size.width;
    }

    private AvailableSpace measurementSpace(TaffyDimension dimension, float stretchWidth) {
        if (dimension.isMinContent()) return AvailableSpace.minContent();
        if (dimension.isMaxContent()) return AvailableSpace.maxContent();
        if (dimension.isFitContent()) {
            LengthPercentage limit = dimension.getFitContentLimit();
            if (limit == null) return AvailableSpace.definite(stretchWidth);
            float resolved = limit.maybeResolve(stretchWidth, layoutComputer::resolveCalcValue);
            return Float.isNaN(resolved) ? AvailableSpace.definite(stretchWidth) : AvailableSpace.definite(resolved);
        }
        return AvailableSpace.definite(stretchWidth);
    }

    private static float resolveStretchHeight(
        BlockItem item,
        float percentageResolutionHeight,
        FloatRect nonAutoMargin) {
        if (!item.sizeStyle.height.isStretch() || Float.isNaN(percentageResolutionHeight)) return item.size.height;
        return Math.max(0f, percentageResolutionHeight - nonAutoMargin.top - nonAutoMargin.bottom);
    }

    private void performAbsoluteLayoutOnChildren(
        List<BlockItem> items,
        FloatSize areaSize,
        FloatRect areaInset,
        TaffyDirection direction) {

        LayoutPartialTree tree = layoutComputer.getTree();
        float areaWidth = areaSize.width - areaInset.left - areaInset.right;
        float areaHeight = areaSize.height - areaInset.top - areaInset.bottom;

        for (BlockItem item : items) {
            if (!item.position.isOutOfFlow()) continue;

            TaffyStyle childStyle = tree.getStyle(item.nodeId);
            if (childStyle.getBoxGenerationMode() == BoxGenerationMode.NONE) continue;

            float aspectRatio = childStyle.getAspectRatio();

            // Get margin style - need to track which are auto
            TaffyRect<LengthPercentageAuto> marginStyle = childStyle.getMargin();
            FloatRect marginOpt = Resolve.maybeResolveRectLpa(marginStyle, areaWidth, layoutComputer::resolveCalcValue);
            FloatRect itemPadding = Resolve.resolveRectOrZero(childStyle.getPadding(), areaWidth, layoutComputer::resolveCalcValue);
            FloatRect itemBorder = Resolve.resolveRectOrZero(childStyle.getBorder(), areaWidth, layoutComputer::resolveCalcValue);
            FloatSize paddingBorderSum = new FloatSize(
                itemPadding.left + itemPadding.right + itemBorder.left + itemBorder.right,
                itemPadding.top + itemPadding.bottom + itemBorder.top + itemBorder.bottom
            );

            FloatSize boxSizingAdj = childStyle.getBoxSizing() == BoxSizing.CONTENT_BOX
                                     ? paddingBorderSum
                                     : new FloatSize(0f, 0f);

            // Resolve inset
            TaffyRect<LengthPercentageAuto> insetStyle = childStyle.getInset();
            float left = insetStyle.left.maybeResolve(areaWidth, layoutComputer::resolveCalcValue);
            float right = insetStyle.right.maybeResolve(areaWidth, layoutComputer::resolveCalcValue);
            float top = insetStyle.top.maybeResolve(areaHeight, layoutComputer::resolveCalcValue);
            float bottom = insetStyle.bottom.maybeResolve(areaHeight, layoutComputer::resolveCalcValue);

            // Compute size from style
            FloatSize size2 = Resolve.maybeResolveSize(childStyle.getSize(), new FloatSize(areaWidth, areaHeight), layoutComputer::resolveCalcValue);
            FloatSize styleSize = maybeAdd(Resolve.maybeApplyAspectRatio(size2, aspectRatio), boxSizingAdj);
            FloatSize size1 = Resolve.maybeResolveSize(childStyle.getMinSize(), new FloatSize(areaWidth, areaHeight), layoutComputer::resolveCalcValue);
            FloatSize minSz = maybeAdd(Resolve.maybeApplyAspectRatio(size1, aspectRatio), boxSizingAdj);
            minSz = maybeMax(minSz, paddingBorderSum);
            FloatSize size = Resolve.maybeResolveSize(childStyle.getMaxSize(), new FloatSize(areaWidth, areaHeight), layoutComputer::resolveCalcValue);
            FloatSize maxSz = maybeAdd(Resolve.maybeApplyAspectRatio(size, aspectRatio), boxSizingAdj);

            FloatSize knownDimensions = maybeClamp(styleSize, minSz, maxSz);

            // For calculating width from inset, use non-auto margins only
            float nonAutoMarginLeft = Float.isNaN(marginOpt.left) ? 0f : marginOpt.left;
            float nonAutoMarginRight = Float.isNaN(marginOpt.right) ? 0f : marginOpt.right;
            float nonAutoMarginTop = Float.isNaN(marginOpt.top) ? 0f : marginOpt.top;
            float nonAutoMarginBottom = Float.isNaN(marginOpt.bottom) ? 0f : marginOpt.bottom;

            knownDimensions = AbsoluteSizing.resolveMeasurementKeywords(
                layoutComputer,
                item.nodeId,
                childStyle.getSize(),
                knownDimensions,
                new FloatSize(areaWidth, areaHeight),
                new FloatRect(left, right, top, bottom),
                new FloatRect(nonAutoMarginLeft, nonAutoMarginRight, nonAutoMarginTop, nonAutoMarginBottom),
                SizingMode.CONTENT_SIZE
            );

            // Fill in width from left/right if not set
            if (Float.isNaN(knownDimensions.width) && !Float.isNaN(left) && !Float.isNaN(right)) {
                float newWidth = areaWidth - nonAutoMarginLeft - nonAutoMarginRight - left - right;
                knownDimensions = new FloatSize(Math.max(newWidth, 0f), knownDimensions.height);
                knownDimensions = maybeClamp(Resolve.maybeApplyAspectRatio(knownDimensions, aspectRatio), minSz, maxSz);
            }

            // Fill in height from top/bottom if not set
            if (Float.isNaN(knownDimensions.height) && !Float.isNaN(top) && !Float.isNaN(bottom)) {
                float newHeight = areaHeight - nonAutoMarginTop - nonAutoMarginBottom - top - bottom;
                knownDimensions = new FloatSize(knownDimensions.width, Math.max(newHeight, 0f));
                knownDimensions = maybeClamp(Resolve.maybeApplyAspectRatio(knownDimensions, aspectRatio), minSz, maxSz);
            }

            LayoutOutput output = layoutComputer.performChildLayout(
                item.nodeId,
                knownDimensions,
                new FloatSize(areaWidth, areaHeight),
                new TaffySize<>(
                    AvailableSpace.definite(TaffyMath.clamp(areaWidth, minSz.width, maxSz.width)),
                    AvailableSpace.definite(TaffyMath.clamp(areaHeight, minSz.height, maxSz.height))
                ),
                SizingMode.CONTENT_SIZE,
                new TaffyLine<>(false, false)
            );

            FloatSize finalSize = maybeClamp(
                new FloatSize(
                    Float.isNaN(knownDimensions.width) ? output.size().width : knownDimensions.width,
                    Float.isNaN(knownDimensions.height) ? output.size().height : knownDimensions.height
                ),
                minSz, maxSz
            );

            // Ensure final size is at least padding + border
            finalSize = new FloatSize(
                Math.max(finalSize.width, paddingBorderSum.width),
                Math.max(finalSize.height, paddingBorderSum.height)
            );

            // Calculate non-auto margin (only count if inset is set on that side)
            FloatRect nonAutoMargin = new FloatRect(
                Float.isNaN(left) ? 0f : nonAutoMarginLeft,
                Float.isNaN(right) ? 0f : nonAutoMarginRight,
                Float.isNaN(top) ? 0f : nonAutoMarginTop,
                Float.isNaN(bottom) ? 0f : nonAutoMarginBottom
            );

            // Calculate auto margin space
            // Auto margins for absolutely positioned elements only resolve if inset is set
            float absoluteAutoMarginSpaceX = Float.isNaN(right) ? finalSize.width : areaWidth - right - (!Float.isNaN(left) ? left : 0f);
            float absoluteAutoMarginSpaceY = Float.isNaN(bottom) ? finalSize.height : areaHeight - bottom - (!Float.isNaN(top) ? top : 0f);

            float freeSpaceX = absoluteAutoMarginSpaceX - finalSize.width
                               - nonAutoMargin.left - nonAutoMargin.right;
            float freeSpaceY = absoluteAutoMarginSpaceY - finalSize.height
                               - nonAutoMargin.top - nonAutoMargin.bottom;

            // Calculate auto margin size
            float autoMarginSizeX = calcAutoMarginSize(marginOpt.left, marginOpt.right, freeSpaceX);

            float autoMarginSizeY = calcAutoMarginSize(marginOpt.top, marginOpt.bottom, freeSpaceY);

            FloatRect autoMargin = new FloatRect(
                Float.isNaN(marginOpt.left) ? autoMarginSizeX : 0f,
                Float.isNaN(marginOpt.right) ? autoMarginSizeX : 0f,
                Float.isNaN(marginOpt.top) ? autoMarginSizeY : 0f,
                Float.isNaN(marginOpt.bottom) ? autoMarginSizeY : 0f
            );

            FloatRect resolvedMargin = new FloatRect(
                Float.isNaN(marginOpt.left) ? autoMargin.left : marginOpt.left,
                Float.isNaN(marginOpt.right) ? autoMargin.right : marginOpt.right,
                Float.isNaN(marginOpt.top) ? autoMargin.top : marginOpt.top,
                Float.isNaN(marginOpt.bottom) ? autoMargin.bottom : marginOpt.bottom
            );

            // Position the item using resolved margins
            float x;
            if (!Float.isNaN(left) && !(direction.isRtl() && !Float.isNaN(right))) {
                x = areaInset.left + left + resolvedMargin.left;
            } else if (!Float.isNaN(right)) {
                x = areaInset.left + areaWidth - finalSize.width - right - resolvedMargin.right;
            } else {
                x = direction.isRtl()
                    ? item.staticPosition.x - finalSize.width - resolvedMargin.right
                    : item.staticPosition.x + resolvedMargin.left;
            }

            float y;
            if (!Float.isNaN(top)) {
                y = areaInset.top + top + resolvedMargin.top;
            } else if (!Float.isNaN(bottom)) {
                y = areaInset.top + areaHeight - finalSize.height - bottom - resolvedMargin.bottom;
            } else {
                y = item.staticPosition.y + resolvedMargin.top;
            }

            FloatSize scrollbarSize = new FloatSize(
                item.overflow.y == Overflow.SCROLL ? item.scrollbarWidth : 0f,
                item.overflow.x == Overflow.SCROLL ? item.scrollbarWidth : 0f
            );

            Layout layout = new Layout(
                item.order,
                new FloatPoint(x, y),
                finalSize,
                output.contentSize(),
                scrollbarSize,
                item.border,
                item.padding,
                resolvedMargin,
                output.scrollableOverflowRect(),
                output.baselines()
            );

            tree.setUnroundedLayout(item.nodeId, layout);
        }
    }

    private static float calcAutoMarginSize(float marginOpt, float marginOpt1, float freeSpace) {
        float autoMarginSizeX;
        {
            int autoMarginCountX = (Float.isNaN(marginOpt) ? 1 : 0) + (Float.isNaN(marginOpt1) ? 1 : 0);
            if (autoMarginCountX == 2 && freeSpace <= 0f) {
                autoMarginSizeX = 0f;
            } else if (autoMarginCountX > 0) {
                // Allow negative margins when child is larger than parent
                autoMarginSizeX = freeSpace / autoMarginCountX;
            } else {
                autoMarginSizeX = 0f;
            }
        }
        return autoMarginSizeX;
    }

    // Helper methods

    private FloatSize maybeAdd(FloatSize size, FloatSize addition) {
        return new FloatSize(
            TaffyMath.maybeAdd(size.width, addition.width),
            TaffyMath.maybeAdd(size.height, addition.height)
        );
    }

    private FloatSize maybeClamp(FloatSize size, FloatSize min, FloatSize max) {
        return new FloatSize(
            TaffyMath.maybeClamp(size.width, min.width, max.width),
            TaffyMath.maybeClamp(size.height, min.height, max.height)
        );
    }

    private FloatSize maybeMax(FloatSize size, FloatSize min) {
        return new FloatSize(
            TaffyMath.maybeMax(size.width, min.width),
            TaffyMath.maybeMax(size.height, min.height)
        );
    }

    private FloatSize orChain(FloatSize... sizes) {
        float width = NaN;
        float height = NaN;
        for (FloatSize size : sizes) {
            if (Float.isNaN(width) && !Float.isNaN(size.width)) width = size.width;
            if (Float.isNaN(height) && !Float.isNaN(size.height)) height = size.height;
            if (!Float.isNaN(width) && !Float.isNaN(height)) break;
        }
        return new FloatSize(width, height);
    }

    private AvailableSpace subtractFromAvailable(AvailableSpace available, float value) {
        if (available.isDefinite()) {
            return AvailableSpace.definite(Math.max(0, available.getValue() - value));
        }
        return available;
    }

    private float resolveMarginSumOrZero(TaffyRect<LengthPercentageAuto> margin, float contextWidth) {
        float left = margin.left.isAuto() ? 0f : margin.left.resolveOrZero(contextWidth, layoutComputer::resolveCalcValue);
        float right = margin.right.isAuto() ? 0f : margin.right.resolveOrZero(contextWidth, layoutComputer::resolveCalcValue);
        return left + right;
    }
}
