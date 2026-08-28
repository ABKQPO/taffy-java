package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import dev.vfyjxf.taffy.util.MeasureFunc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridTrackSizingParityTest {
    private static final float EPSILON = 0.01f;

    @Test
    void minmaxAutoFixedColumnUsesContentBaseSizeUnderMinContentConstraint() {
        TaffyTree tree = new TaffyTree();
        NodeId item = tree.newLeafWithMeasure(new TaffyStyle(), fixedMeasure(40f, 10f));

        TaffyStyle gridStyle = new TaffyStyle();
        gridStyle.display = TaffyDisplay.GRID;
        gridStyle.gridTemplateColumns.add(TrackSizingFunction.minmax(TrackSizingFunction.auto(), TrackSizingFunction.fixed(100f)));
        gridStyle.gridTemplateRows.add(TrackSizingFunction.fixed(10f));
        NodeId grid = tree.newWithChildren(gridStyle, item);

        tree.computeLayout(grid, TaffySize.minContent());

        Layout gridLayout = tree.getLayout(grid);
        Layout itemLayout = tree.getLayout(item);
        assertEquals(40f, gridLayout.size().width, EPSILON);
        assertEquals(40f, itemLayout.size().width, EPSILON);
    }

    @Test
    void gridItemBlockSizingKeywordsUseIntrinsicHeightInsteadOfStretch() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.gridTemplateRows.add(TrackSizingFunction.fixed(60f));
        for (int index = 0; index < 4; index++) {
            grid.gridTemplateColumns.add(TrackSizingFunction.fixed(40f));
        }

        NodeId stretch = tree.newLeafWithMeasure(sized(Float.NaN, Float.NaN), fixedMeasure(40f, 10f));
        tree.getStyle(stretch).size.height = TaffyDimension.stretch();
        NodeId fitContent = tree.newLeafWithMeasure(sized(Float.NaN, Float.NaN), fixedMeasure(40f, 10f));
        tree.getStyle(fitContent).size.height = TaffyDimension.fitContent();
        NodeId minContent = tree.newLeafWithMeasure(sized(Float.NaN, Float.NaN), fixedMeasure(40f, 10f));
        tree.getStyle(minContent).size.height = TaffyDimension.minContent();
        NodeId maxContent = tree.newLeafWithMeasure(sized(Float.NaN, Float.NaN), fixedMeasure(40f, 10f));
        tree.getStyle(maxContent).size.height = TaffyDimension.maxContent();
        NodeId root = tree.newWithChildren(grid, stretch, fitContent, minContent, maxContent);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(60f, tree.getLayout(stretch).size().height, EPSILON);
        assertEquals(10f, tree.getLayout(fitContent).size().height, EPSILON);
        assertEquals(10f, tree.getLayout(minContent).size().height, EPSILON);
        assertEquals(10f, tree.getLayout(maxContent).size().height, EPSILON);
    }

    @Test
    void minmaxAutoFixedRowUsesContentBaseSizeUnderMinContentConstraint() {
        TaffyTree tree = new TaffyTree();
        NodeId item = tree.newLeafWithMeasure(new TaffyStyle(), fixedMeasure(10f, 40f));

        TaffyStyle gridStyle = new TaffyStyle();
        gridStyle.display = TaffyDisplay.GRID;
        gridStyle.gridTemplateColumns.add(TrackSizingFunction.fixed(10f));
        gridStyle.gridTemplateRows.add(TrackSizingFunction.minmax(TrackSizingFunction.auto(), TrackSizingFunction.fixed(100f)));
        NodeId grid = tree.newWithChildren(gridStyle, item);

        tree.computeLayout(grid, TaffySize.minContent());

        Layout gridLayout = tree.getLayout(grid);
        Layout itemLayout = tree.getLayout(item);
        assertEquals(40f, gridLayout.size().height, EPSILON);
        assertEquals(40f, itemLayout.size().height, EPSILON);
    }

    @Test
    void spanningMinContentRowsUseTheContentContributionBeyondAnExplicitMinimum() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.minSize = new TaffySize<>(LengthPercentageAuto.length(12f), LengthPercentageAuto.length(12f));
        itemStyle.gridRow = new TaffyLine<>(GridPlacement.line(1), GridPlacement.span(2));
        itemStyle.gridColumn = new TaffyLine<>(GridPlacement.line(1), GridPlacement.span(2));
        NodeId item = tree.newLeafWithMeasure(itemStyle, fixedMeasure(30f, 20f));

        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.size = new TaffySize<>(TaffyDimension.length(120f), TaffyDimension.length(120f));
        grid.border = TaffyRect.all(LengthPercentage.length(3f));
        grid.gridTemplateColumns.add(TrackSizingFunction.minContent());
        grid.gridTemplateColumns.add(TrackSizingFunction.minContent());
        grid.gridTemplateRows.add(TrackSizingFunction.minContent());
        grid.gridTemplateRows.add(TrackSizingFunction.minContent());
        NodeId root = tree.newWithChildren(grid, item);

        tree.computeLayout(root, TaffySize.maxContent());

        assertTrackSizes(tree, root, 15f, 15f, 10f, 10f);
    }

    @Test
    void minmaxZeroMinContentUsesMeasuredContributionsForBothAxes() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.minSize = new TaffySize<>(LengthPercentageAuto.length(12f), LengthPercentageAuto.length(12f));
        NodeId item = tree.newLeafWithMeasure(itemStyle, fixedMeasure(30f, 20f));

        NodeId root = tree.newWithChildren(oneTrackGrid(
            TrackSizingFunction.minmax(TrackSizingFunction.fixed(0f), TrackSizingFunction.minContent())), item);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(30f, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(0), EPSILON);
        assertEquals(20f, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(0), EPSILON);
    }

    @Test
    void minmaxZeroMaxContentUsesMaximumContributionAsItsGrowthLimit() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.minSize = new TaffySize<>(LengthPercentageAuto.length(12f), LengthPercentageAuto.length(12f));
        NodeId item = tree.newLeafWithMeasure(itemStyle, fixedMeasure(60f, 12f));

        NodeId root = tree.newWithChildren(oneTrackGrid(
            TrackSizingFunction.minmax(TrackSizingFunction.fixed(0f), TrackSizingFunction.maxContent())), item);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(60f, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(0), EPSILON);
        assertEquals(12f, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(0), EPSILON);
    }

    @Test
    void minmaxAutoFixedFloorsItsMaximumAtTheIntrinsicBaseSize() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.minSize = new TaffySize<>(LengthPercentageAuto.length(12f), LengthPercentageAuto.length(12f));
        NodeId item = tree.newLeafWithMeasure(itemStyle, fixedMeasure(12f, 12f));

        NodeId root = tree.newWithChildren(oneTrackGrid(
            TrackSizingFunction.minmax(TrackSizingFunction.auto(), TrackSizingFunction.fixed(10f))), item);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(12f, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(0), EPSILON);
        assertEquals(12f, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(0), EPSILON);
    }

    @Test
    void minmaxAutoFixedUsesTheMinimumContributionBeforeWrappedContent() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.minSize = new TaffySize<>(LengthPercentageAuto.length(12f), LengthPercentageAuto.length(12f));
        NodeId item = tree.newLeafWithMeasure(itemStyle, (knownDimensions, availableSpace) -> {
            float width = Float.isNaN(knownDimensions.width)
                ? (availableSpace.width.isMinContent() ? 12f : 60f) : knownDimensions.width;
            float height = width <= 12f ? 20f : 12f;
            return new FloatSize(width, Float.isNaN(knownDimensions.height) ? height : knownDimensions.height);
        });

        NodeId root = tree.newWithChildren(oneTrackGrid(
            TrackSizingFunction.minmax(TrackSizingFunction.auto(), TrackSizingFunction.fixed(10f))), item);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(12f, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(0), EPSILON);
        assertEquals(12f, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(0), EPSILON);
    }

    @Test
    void minmaxAutoFixedUsesSpecifiedMinimumInsteadOfTheMinContentWidth() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.minSize = new TaffySize<>(LengthPercentageAuto.length(12f), LengthPercentageAuto.length(12f));
        NodeId item = tree.newLeafWithMeasure(itemStyle, fixedMeasure(30f, 12f));

        NodeId root = tree.newWithChildren(oneTrackGrid(
            TrackSizingFunction.minmax(TrackSizingFunction.auto(), TrackSizingFunction.fixed(10f))), item);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(12f, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(0), EPSILON);
    }

    @Test
    void spanningItemGrowsAnIntrinsicMaximumPastAnExplicitMinimumTrack() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = spanningStyle();
        itemStyle.minSize = new TaffySize<>(LengthPercentageAuto.length(12f), LengthPercentageAuto.length(12f));
        NodeId item = tree.newLeafWithMeasure(itemStyle, fixedMeasure(30f, 20f));

        TaffyStyle grid = gridBase();
        grid.size = new TaffySize<>(TaffyDimension.length(120f), TaffyDimension.length(120f));
        TrackSizingFunction intrinsicMaximum = TrackSizingFunction.minmax(
            TrackSizingFunction.fixed(0f), TrackSizingFunction.minContent());
        grid.gridTemplateColumns.add(TrackSizingFunction.fixed(20f));
        grid.gridTemplateColumns.add(intrinsicMaximum);
        grid.gridTemplateRows.add(TrackSizingFunction.fixed(20f));
        grid.gridTemplateRows.add(intrinsicMaximum);
        NodeId root = tree.newWithChildren(grid, item,
            tree.newLeaf(placed(1, 1)), tree.newLeaf(placed(2, 2)));

        tree.computeLayout(root, TaffySize.maxContent());

        assertTrackSizes(tree, root, 20f, 10f, 20f, 0f);
    }

    @Test
    void automaticMinimumSpanningFlexibleTracksUseTheContainerSize() {
        TaffyTree tree = new TaffyTree();
        NodeId content = tree.newLeaf(sized(100f, 100f));

        TaffyStyle spanning = spanningStyle();
        NodeId spanningItem = tree.newWithChildren(spanning, content);
        NodeId firstCell = tree.newLeaf(placed(1, 1));
        NodeId secondCell = tree.newLeaf(placed(2, 2));

        TaffyStyle grid = twoByTwoFlexibleGrid();
        NodeId root = tree.newWithChildren(grid, spanningItem, firstCell, secondCell);
        tree.computeLayout(root, TaffySize.maxContent());

        assertTrackSizes(tree, root, 27f, 27f, 27f, 27f);
    }

    @Test
    void fixedMinimumSpanningFlexibleTrackDoesNotGrowTheAutoTrack() {
        TaffyTree tree = new TaffyTree();
        NodeId content = tree.newLeaf(sized(100f, 100f));

        TaffyStyle spanning = spanningStyle();
        spanning.minSize = new TaffySize<>(LengthPercentageAuto.length(50f), LengthPercentageAuto.length(50f));
        NodeId spanningItem = tree.newWithChildren(spanning, content);
        NodeId firstCell = tree.newLeaf(placed(1, 1));
        NodeId secondCell = tree.newLeaf(placed(2, 2));

        TaffyStyle grid = gridBase();
        grid.gridTemplateColumns.add(TrackSizingFunction.minmax(TrackSizingFunction.fixed(25f), TrackSizingFunction.flex(1f)));
        grid.gridTemplateColumns.add(TrackSizingFunction.auto());
        grid.gridTemplateRows.add(TrackSizingFunction.minmax(TrackSizingFunction.fixed(25f), TrackSizingFunction.flex(1f)));
        grid.gridTemplateRows.add(TrackSizingFunction.auto());
        NodeId root = tree.newWithChildren(grid, spanningItem, firstCell, secondCell);
        tree.computeLayout(root, TaffySize.maxContent());

        assertTrackSizes(tree, root, 54f, 0f, 54f, 0f);
    }

    @Test
    void zeroFlexibleTrackUsesTheOtherFlexibleTrackForIntrinsicSizing() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle item = sized(Float.NaN, 100f);
        item.gridRow = new TaffyLine<>(GridPlacement.line(1), GridPlacement.line(2));
        NodeId child = tree.newLeaf(item);

        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.gridTemplateColumns.add(TrackSizingFunction.fixed(20f));
        grid.gridTemplateRows.add(TrackSizingFunction.flex(0f));
        grid.gridTemplateRows.add(TrackSizingFunction.flex(1f));
        NodeId root = tree.newWithChildren(grid, child);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(200f, tree.getLayout(root).size().height, EPSILON);
        assertEquals(100f, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(0), EPSILON);
        assertEquals(100f, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(1), EPSILON);
    }

    @Test
    void zeroFactorSpanOnlyGrowsFlexibleTracksWithIntrinsicMinimums() {
        TaffyTree tree = new TaffyTree();
        NodeId spanningItem = tree.newLeaf(sized(100f, 100f));
        TaffyStyle spanning = tree.getStyle(spanningItem);
        spanning.gridRow = new TaffyLine<>(GridPlacement.line(1), GridPlacement.span(2));
        spanning.gridColumn = new TaffyLine<>(GridPlacement.line(1), GridPlacement.span(2));
        tree.setStyle(spanningItem, spanning);
        NodeId firstCell = tree.newLeaf(placed(1, 1));
        NodeId secondCell = tree.newLeaf(placed(2, 2));

        TaffyStyle grid = gridBase();
        grid.size = new TaffySize<>(TaffyDimension.length(50f), TaffyDimension.length(50f));
        TrackSizingFunction fixedZeroFlexible = TrackSizingFunction.minmax(
            TrackSizingFunction.fixed(0f), TrackSizingFunction.flex(0f));
        grid.gridTemplateColumns.add(TrackSizingFunction.flex(0f));
        grid.gridTemplateColumns.add(fixedZeroFlexible);
        grid.gridTemplateRows.add(TrackSizingFunction.flex(0f));
        grid.gridTemplateRows.add(fixedZeroFlexible);
        NodeId root = tree.newWithChildren(grid, spanningItem, firstCell, secondCell);
        tree.computeLayout(root, TaffySize.maxContent());

        assertTrackSizes(tree, root, 100f, 0f, 100f, 0f);
    }

    @Test
    void spanningFlexibleItemsUseSharedBaseSizesBeforeApplyingIncreases() {
        TaffyTree tree = new TaffyTree();
        NodeId firstSpan = tree.newLeaf(spanningSized(60f, 1, 3));
        NodeId secondSpan = tree.newLeaf(spanningSized(150f, 2, 5));

        TaffyStyle grid = fourTrackGrid(1f, 1f, 1f, 1f);
        NodeId root = tree.newWithChildren(grid, firstSpan, secondSpan,
            tree.newLeaf(placed(1, 1)), tree.newLeaf(placed(2, 2)),
            tree.newLeaf(placed(3, 3)), tree.newLeaf(placed(4, 4)));
        tree.computeLayout(root, TaffySize.maxContent());

        assertFourTrackSizes(tree, root, 30f, 50f, 50f, 50f);
    }

    @Test
    void spanningFlexibleItemsRespectFactorsWhenApplyingPlannedIncreases() {
        TaffyTree tree = new TaffyTree();
        NodeId firstSpan = tree.newLeaf(spanningSized(60f, 1, 3));
        NodeId secondSpan = tree.newLeaf(spanningSized(150f, 2, 5));

        TaffyStyle grid = fourTrackGrid(1f, 1f, 1f, 4f);
        NodeId root = tree.newWithChildren(grid, firstSpan, secondSpan,
            tree.newLeaf(placed(1, 1)), tree.newLeaf(placed(2, 2)),
            tree.newLeaf(placed(3, 3)), tree.newLeaf(placed(4, 4)));
        tree.computeLayout(root, TaffySize.maxContent());

        assertFourTrackSizes(tree, root, 30f, 30f, 25f, 100f);
    }

    @Test
    void fixedMinimumZeroFlexibleTrackLeavesSpanningSpaceForAutoTrack() {
        TaffyTree tree = new TaffyTree();
        NodeId spanningItem = tree.newLeaf(spanningSized(100f, 1, 3));
        NodeId firstCell = tree.newLeaf(placed(1, 1));
        NodeId secondCell = tree.newLeaf(placed(2, 2));

        TaffyStyle grid = gridBase();
        grid.size = new TaffySize<>(TaffyDimension.length(50f), TaffyDimension.length(50f));
        TrackSizingFunction fixedZeroFlexible = TrackSizingFunction.minmax(
            TrackSizingFunction.fixed(0f), TrackSizingFunction.flex(0f));
        grid.gridTemplateColumns.add(fixedZeroFlexible);
        grid.gridTemplateColumns.add(TrackSizingFunction.auto());
        grid.gridTemplateRows.add(fixedZeroFlexible);
        grid.gridTemplateRows.add(TrackSizingFunction.auto());
        NodeId root = tree.newWithChildren(grid, spanningItem, firstCell, secondCell);
        tree.computeLayout(root, TaffySize.maxContent());

        assertTrackSizes(tree, root, 0f, 44f, 0f, 44f);
    }

    @Test
    void nonzeroFixedMinimumZeroFlexibleTrackKeepsItsFixedBaseSize() {
        TaffyTree tree = new TaffyTree();
        NodeId spanningItem = tree.newLeaf(spanningSized(100f, 1, 3));
        NodeId firstCell = tree.newLeaf(placed(1, 1));
        NodeId secondCell = tree.newLeaf(placed(2, 2));

        TaffyStyle grid = gridBase();
        grid.size = new TaffySize<>(TaffyDimension.length(50f), TaffyDimension.length(50f));
        TrackSizingFunction fixedZeroFlexible = TrackSizingFunction.minmax(
            TrackSizingFunction.fixed(25f), TrackSizingFunction.flex(0f));
        grid.gridTemplateColumns.add(fixedZeroFlexible);
        grid.gridTemplateColumns.add(TrackSizingFunction.auto());
        grid.gridTemplateRows.add(fixedZeroFlexible);
        grid.gridTemplateRows.add(TrackSizingFunction.auto());
        NodeId root = tree.newWithChildren(grid, spanningItem, firstCell, secondCell);
        tree.computeLayout(root, TaffySize.maxContent());

        assertTrackSizes(tree, root, 25f, 19f, 25f, 19f);
    }

    @Test
    void zeroFlexibleTrackDoesNotAbsorbAutomaticMinimumAcrossAnAutoTrack() {
        TaffyTree tree = new TaffyTree();
        NodeId content = tree.newLeaf(sized(100f, 100f));
        NodeId spanningItem = tree.newWithChildren(spanningStyle(), content);
        NodeId firstCell = tree.newLeaf(placed(1, 1));
        NodeId secondCell = tree.newLeaf(placed(2, 2));

        TaffyStyle grid = gridBase();
        grid.gridTemplateColumns.add(TrackSizingFunction.flex(0f));
        grid.gridTemplateColumns.add(TrackSizingFunction.auto());
        grid.gridTemplateRows.add(TrackSizingFunction.flex(0f));
        grid.gridTemplateRows.add(TrackSizingFunction.auto());
        NodeId root = tree.newWithChildren(grid, spanningItem, firstCell, secondCell);
        tree.computeLayout(root, TaffySize.maxContent());

        assertTrackSizes(tree, root, 0f, 54f, 0f, 54f);
    }

    @Test
    void nonFlexibleSpanningRowsConstrainTheFlexibleSpanContribution() {
        TaffyTree tree = new TaffyTree();
        NodeId firstSpan = tree.newLeaf(spanningSized(60f, 1, 3));
        NodeId secondSpan = tree.newLeaf(spanningSized(150f, 2, 5));

        TaffyStyle grid = fourTrackGridWithFirstTrack(TrackSizingFunction.flex(1f));
        NodeId root = tree.newWithChildren(grid, firstSpan, secondSpan,
            tree.newLeaf(placed(1, 1)), tree.newLeaf(placed(2, 2)),
            tree.newLeaf(placed(3, 3)), tree.newLeaf(placed(4, 4)));
        tree.computeLayout(root, TaffySize.maxContent());

        assertFourTrackSizes(tree, root, 10f, 50f, 50f, 50f);
    }

    @Test
    void fixedMinimumFlexibleTrackDoesNotReceiveAutoSpanContribution() {
        TaffyTree tree = new TaffyTree();
        NodeId firstSpan = tree.newLeaf(spanningSized(60f, 1, 3));
        NodeId secondSpan = tree.newLeaf(spanningSized(150f, 2, 5));

        TaffyStyle grid = fourTrackGridWithFirstTrack(TrackSizingFunction.minmax(
            TrackSizingFunction.fixed(0f), TrackSizingFunction.flex(1f)));
        NodeId root = tree.newWithChildren(grid, firstSpan, secondSpan,
            tree.newLeaf(placed(1, 1)), tree.newLeaf(placed(2, 2)),
            tree.newLeaf(placed(3, 3)), tree.newLeaf(placed(4, 4)));
        tree.computeLayout(root, TaffySize.maxContent());

        assertFourTrackSizes(tree, root, 0f, 50f, 50f, 50f);
    }

    @Test
    void nonFlexibleSingleTrackContributionsPrecedeSpanningContributions() {
        TaffyTree tree = new TaffyTree();
        NodeId first = tree.newLeaf(placedSized(100f, 50f, 1, 2, 1, 1));
        NodeId second = tree.newLeaf(placedSized(40f, 30f, 1, 2, 2, 2));
        NodeId third = tree.newLeaf(placedSized(120f, 20f, 3, 1, 1, 2));

        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.size = new TaffySize<>(TaffyDimension.length(320f), TaffyDimension.length(640f));
        grid.gridTemplateColumns.add(TrackSizingFunction.auto());
        grid.gridTemplateColumns.add(TrackSizingFunction.auto());
        grid.gridTemplateColumns.add(TrackSizingFunction.flex(1f));
        grid.gridTemplateRows.add(TrackSizingFunction.auto());
        grid.gridTemplateRows.add(TrackSizingFunction.auto());
        grid.gridTemplateRows.add(TrackSizingFunction.auto());
        grid.gridTemplateRows.add(TrackSizingFunction.flex(1f));
        NodeId root = tree.newWithChildren(grid, first, second, third);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(100f, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(0), EPSILON);
        assertEquals(20f, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(1), EPSILON);
        assertEquals(100f, tree.getLayout(second).location().x, EPSILON);
    }

    @Test
    void flexibleSpanningContributionKeepsLargerSingleTrackBaseSize() {
        TaffyTree tree = new TaffyTree();
        NodeId first = tree.newLeaf(spanningSized(60f, 1, 2));
        NodeId second = tree.newLeaf(spanningSized(150f, 1, 4));

        TaffyStyle grid = threeFlexibleTrackGrid();
        NodeId root = tree.newWithChildren(grid, first, second,
            tree.newLeaf(placed(1, 1)), tree.newLeaf(placed(2, 2)), tree.newLeaf(placed(3, 3)));
        tree.computeLayout(root, TaffySize.maxContent());

        assertThreeTrackSizes(tree, root, 60f, 50f, 50f);
    }

    @Test
    void fixedMinimumConstrainsZeroFlexibleTrackBeforeContent() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.minSize = new TaffySize<>(LengthPercentageAuto.length(50f), LengthPercentageAuto.length(50f));
        itemStyle.gridRow = new TaffyLine<>(GridPlacement.line(1), GridPlacement.span(1));
        itemStyle.gridColumn = new TaffyLine<>(GridPlacement.line(1), GridPlacement.span(1));
        NodeId item = tree.newWithChildren(itemStyle, tree.newLeaf(sized(100f, 100f)));

        TaffyStyle grid = gridBase();
        grid.gridTemplateColumns.add(TrackSizingFunction.flex(0f));
        grid.gridTemplateRows.add(TrackSizingFunction.flex(0f));
        NodeId root = tree.newWithChildren(grid, item, tree.newLeaf(placed(1, 1)));
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(50f, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(0), EPSILON);
        assertEquals(50f, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(0), EPSILON);
    }

    @Test
    void fixedMinimumAllowsPositiveFlexibleTrackToFillAvailableSpace() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.minSize = new TaffySize<>(LengthPercentageAuto.length(50f), LengthPercentageAuto.length(50f));
        itemStyle.gridRow = new TaffyLine<>(GridPlacement.line(1), GridPlacement.span(1));
        itemStyle.gridColumn = new TaffyLine<>(GridPlacement.line(1), GridPlacement.span(1));
        NodeId item = tree.newWithChildren(itemStyle, tree.newLeaf(sized(100f, 100f)));

        TaffyStyle grid = gridBase();
        grid.gridTemplateColumns.add(TrackSizingFunction.flex(1f));
        grid.gridTemplateRows.add(TrackSizingFunction.flex(1f));
        NodeId root = tree.newWithChildren(grid, item, tree.newLeaf(placed(1, 1)));
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(54f, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(0), EPSILON);
        assertEquals(54f, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(0), EPSILON);
    }

    private TaffyStyle twoByTwoFlexibleGrid() {
        TaffyStyle grid = gridBase();
        grid.gridTemplateColumns.add(TrackSizingFunction.flex(1f));
        grid.gridTemplateColumns.add(TrackSizingFunction.flex(1f));
        grid.gridTemplateRows.add(TrackSizingFunction.flex(1f));
        grid.gridTemplateRows.add(TrackSizingFunction.flex(1f));
        return grid;
    }

    private TaffyStyle fourTrackGrid(float first, float second, float third, float fourth) {
        TaffyStyle grid = gridBase();
        float[] factors = {first, second, third, fourth};
        for (float factor : factors) {
            grid.gridTemplateColumns.add(TrackSizingFunction.flex(factor));
            grid.gridTemplateRows.add(TrackSizingFunction.flex(factor));
        }
        return grid;
    }

    private TaffyStyle threeFlexibleTrackGrid() {
        TaffyStyle grid = gridBase();
        for (int index = 0; index < 3; index++) {
            grid.gridTemplateColumns.add(TrackSizingFunction.flex(1f));
            grid.gridTemplateRows.add(TrackSizingFunction.flex(1f));
        }
        return grid;
    }

    private TaffyStyle fourTrackGridWithFirstTrack(TrackSizingFunction firstTrack) {
        TaffyStyle grid = gridBase();
        grid.gridTemplateColumns.add(firstTrack);
        grid.gridTemplateRows.add(firstTrack);
        for (int index = 0; index < 3; index++) {
            grid.gridTemplateColumns.add(TrackSizingFunction.auto());
            grid.gridTemplateRows.add(TrackSizingFunction.auto());
        }
        return grid;
    }

    private TaffyStyle gridBase() {
        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.size = new TaffySize<>(TaffyDimension.length(60f), TaffyDimension.length(60f));
        grid.border = TaffyRect.all(LengthPercentage.length(3f));
        return grid;
    }

    private TaffyStyle oneTrackGrid(TrackSizingFunction track) {
        TaffyStyle grid = gridBase();
        grid.size = new TaffySize<>(TaffyDimension.length(120f), TaffyDimension.length(120f));
        grid.gridTemplateColumns.add(track);
        grid.gridTemplateRows.add(track);
        return grid;
    }

    private TaffyStyle spanningStyle() {
        TaffyStyle style = new TaffyStyle();
        style.gridRow = new TaffyLine<>(GridPlacement.line(1), GridPlacement.span(2));
        style.gridColumn = new TaffyLine<>(GridPlacement.line(1), GridPlacement.span(2));
        return style;
    }

    private TaffyStyle placed(int row, int column) {
        TaffyStyle style = new TaffyStyle();
        style.gridRow = new TaffyLine<>(GridPlacement.line(row), GridPlacement.auto());
        style.gridColumn = new TaffyLine<>(GridPlacement.line(column), GridPlacement.auto());
        return style;
    }

    private TaffyStyle sized(float width, float height) {
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(
            Float.isNaN(width) ? TaffyDimension.AUTO : TaffyDimension.length(width),
            Float.isNaN(height) ? TaffyDimension.AUTO : TaffyDimension.length(height));
        return style;
    }

    private TaffyStyle spanningSized(float size, int start, int end) {
        TaffyStyle style = sized(size, size);
        style.gridRow = new TaffyLine<>(GridPlacement.line(start), GridPlacement.line(end));
        style.gridColumn = new TaffyLine<>(GridPlacement.line(start), GridPlacement.line(end));
        return style;
    }

    private TaffyStyle placedSized(
        float width,
        float height,
        int rowStart,
        int rowSpan,
        int columnStart,
        int columnSpan) {
        TaffyStyle style = sized(width, height);
        style.gridRow = new TaffyLine<>(GridPlacement.line(rowStart), GridPlacement.span(rowSpan));
        style.gridColumn = new TaffyLine<>(GridPlacement.line(columnStart), GridPlacement.span(columnSpan));
        return style;
    }

    private void assertTrackSizes(TaffyTree tree, NodeId root,
                                  float firstColumn, float secondColumn,
                                  float firstRow, float secondRow) {
        assertEquals(firstColumn, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(0), EPSILON);
        assertEquals(secondColumn, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(1), EPSILON);
        assertEquals(firstRow, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(0), EPSILON);
        assertEquals(secondRow, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(1), EPSILON);
    }

    private void assertFourTrackSizes(TaffyTree tree, NodeId root,
                                      float first, float second, float third, float fourth) {
        for (int index = 0; index < 4; index++) {
            float expected = switch (index) {
                case 0 -> first;
                case 1 -> second;
                case 2 -> third;
                default -> fourth;
            };
            assertEquals(expected, tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(index), EPSILON);
            assertEquals(expected, tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(index), EPSILON);
        }
    }

    private void assertThreeTrackSizes(TaffyTree tree, NodeId root, float first, float second, float third) {
        float[] expected = {first, second, third};
        for (int index = 0; index < expected.length; index++) {
            assertEquals(expected[index], tree.getDetailedLayoutInfo(root).grid().columns().sizes().get(index), EPSILON);
            assertEquals(expected[index], tree.getDetailedLayoutInfo(root).grid().rows().sizes().get(index), EPSILON);
        }
    }

    private MeasureFunc fixedMeasure(float width, float height) {
        return (knownDimensions, availableSpace) -> new FloatSize(
            Float.isNaN(knownDimensions.width) ? width : knownDimensions.width,
            Float.isNaN(knownDimensions.height) ? height : knownDimensions.height);
    }
}
