package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.GridTemplateComponent;
import dev.vfyjxf.taffy.style.NamedGridLine;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.tree.DetailedGridInfo;
import dev.vfyjxf.taffy.tree.DetailedLayoutInfo;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DetailedGridInfoTest {
    @Test
    void gridLayoutPublishesTrackAndItemDetails() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(100f));
        grid.gridTemplateColumns.add(TrackSizingFunction.fixed(40f));
        grid.gridTemplateColumns.add(TrackSizingFunction.fixed(60f));
        grid.gridTemplateRows.add(TrackSizingFunction.fixed(50f));
        grid.gridTemplateRows.add(TrackSizingFunction.fixed(50f));
        grid.gridTemplateColumnNames.add(new NamedGridLine("content-start", 1));
        grid.gridTemplateColumnNames.add(new NamedGridLine("content-end", 2));
        grid.gridTemplateRowNames.add(new NamedGridLine("content-start", 1));
        grid.gridTemplateRowNames.add(new NamedGridLine("content-end", 2));

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.size = new TaffySize<>(TaffyDimension.length(20f), TaffyDimension.length(20f));
        childStyle.gridColumn = new TaffyLine<>(GridPlacement.line(1), GridPlacement.line(2));
        childStyle.gridRow = new TaffyLine<>(GridPlacement.line(1), GridPlacement.line(2));
        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(grid, child);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(100f), AvailableSpace.definite(100f)));

        DetailedLayoutInfo details = tree.getDetailedLayoutInfo(root);
        assertTrue(details.isGrid());
        DetailedGridInfo gridInfo = details.grid();
        assertEquals(2, gridInfo.columns().sizes().size());
        assertEquals(40f, gridInfo.columns().sizes().get(0), 0.01f);
        assertEquals(1, gridInfo.items().size());
        FloatRect area = gridInfo.itemGridArea(0);
        assertNotNull(area);
        assertEquals(40f, area.right, 0.01f);
        assertEquals(50f, area.bottom, 0.01f);
        assertTrue(gridInfo.columns().namesForLine(0).contains("content-start"));
        assertTrue(gridInfo.gridTemplateColumns().contains("[content-start] 40.0000px"));

        FloatRect absoluteArea = gridInfo.resolveAbsoluteGridArea(
            new TaffyLine<>(GridPlacement.namedLine("content-start"), GridPlacement.namedLine("content-end")),
            new TaffyLine<>(GridPlacement.namedLine("content-start"), GridPlacement.namedLine("content-end")),
            TaffyDirection.LTR,
            FloatRect.ltrb(0f, 0f, 100f, 100f));
        assertEquals(40f, absoluteArea.right, 0.01f);
        assertEquals(50f, absoluteArea.bottom, 0.01f);
    }

    @Test
    void gridExplicitTracksAreClampedToRustMaximum() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(10f));
        for (int index = 0; index < 10001; index++) {
            grid.gridTemplateColumns.add(TrackSizingFunction.fixed(1f));
        }
        NodeId child = tree.newLeaf(new TaffyStyle());
        NodeId root = tree.newWithChildren(grid, child);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(100f), AvailableSpace.definite(10f)));

        assertEquals(10000, tree.getDetailedLayoutInfo(root).grid().columns().sizes().size());
    }

    @Test
    void repeatedTemplateResolvesNegativeLinesAgainstExpandedTracks() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.size = new TaffySize<>(TaffyDimension.length(2f), TaffyDimension.length(1f));
        grid.gridTemplateColumnsWithRepeat.add(GridTemplateComponent.repeatCount(
            2, TrackSizingFunction.fixed(1f)));
        grid.gridTemplateRows.add(TrackSizingFunction.fixed(1f));

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.gridColumn = new TaffyLine<>(GridPlacement.line(-2), GridPlacement.line(-1));
        childStyle.gridRow = new TaffyLine<>(GridPlacement.line(1), GridPlacement.line(2));
        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(grid, child);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(2f), AvailableSpace.definite(1f)));

        FloatRect area = tree.getDetailedLayoutInfo(root).grid().itemGridArea(0);
        assertEquals(1f, area.left, 0.01f);
        assertEquals(2f, area.right, 0.01f);
    }

    @Test
    void removeChildrenRangeDetachesWithoutDeletingNodes() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle style = new TaffyStyle();
        NodeId first = tree.newLeaf(style);
        NodeId second = tree.newLeaf(style);
        NodeId third = tree.newLeaf(style);
        NodeId parent = tree.newWithChildren(style, first, second, third);

        tree.removeChildrenRange(parent, 1, 3);

        assertEquals(1, tree.childCount(parent));
        assertEquals(first, tree.getChildAtIndex(parent, 0));
        assertTrue(tree.containsNode(second));
        assertEquals(null, tree.getParent(second));
    }

    @Test
    void detailedGridAreasUsePhysicalCoordinatesInRtl() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.direction = TaffyDirection.RTL;
        grid.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(50f));
        grid.gridTemplateColumns.add(TrackSizingFunction.fixed(40f));
        grid.gridTemplateColumns.add(TrackSizingFunction.fixed(60f));
        grid.gridTemplateRows.add(TrackSizingFunction.fixed(50f));

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.gridColumn = new TaffyLine<>(GridPlacement.line(1), GridPlacement.line(2));
        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(grid, child);
        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(100f), AvailableSpace.definite(50f)));

        DetailedGridInfo details = tree.getDetailedLayoutInfo(root).grid();
        FloatRect area = details.itemGridArea(0);
        assertEquals(60f, area.left, 0.01f);
        assertEquals(100f, area.right, 0.01f);

        FloatRect absoluteArea = details.resolveAbsoluteGridArea(
            new TaffyLine<>(GridPlacement.line(1), GridPlacement.line(2)),
            new TaffyLine<>(GridPlacement.line(1), GridPlacement.line(2)),
            TaffyDirection.RTL,
            FloatRect.ltrb(0f, 0f, 100f, 50f));
        assertEquals(60f, absoluteArea.left, 0.01f);
        assertEquals(100f, absoluteArea.right, 0.01f);
    }

    @Test
    void detailedGridAreaNormalizesUnknownNamedLineBeforeBoundsFallback() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.size = new TaffySize<>(TaffyDimension.length(200f), TaffyDimension.length(150f));
        grid.gridTemplateColumns.add(TrackSizingFunction.fixed(40f));
        grid.gridTemplateColumns.add(TrackSizingFunction.fixed(60f));
        grid.gridTemplateRows.add(TrackSizingFunction.fixed(20f));
        grid.gridTemplateRows.add(TrackSizingFunction.fixed(50f));
        NodeId child = tree.newLeaf(new TaffyStyle());
        NodeId root = tree.newWithChildren(grid, child);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(200f), AvailableSpace.definite(150f)));

        DetailedGridInfo details = tree.getDetailedLayoutInfo(root).grid();
        FloatRect area = details.resolveAbsoluteGridArea(
            new TaffyLine<>(GridPlacement.namedLine("missing"), GridPlacement.line(3)),
            new TaffyLine<>(GridPlacement.namedLine("missing"), GridPlacement.line(3)),
            TaffyDirection.LTR,
            FloatRect.ltrb(0f, 0f, 200f, 150f));

        assertEquals(100f, area.left, 0.01f);
        assertEquals(70f, area.top, 0.01f);
        assertEquals(200f, area.right, 0.01f);
        assertEquals(150f, area.bottom, 0.01f);
    }

    @Test
    void autoStartDefiniteEndDoesNotCreatePhantomImplicitTracks() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.size = new TaffySize<>(TaffyDimension.length(10f), TaffyDimension.length(10f));

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.size = new TaffySize<>(TaffyDimension.length(10f), TaffyDimension.length(10f));
        childStyle.gridColumn = new TaffyLine<>(GridPlacement.auto(), GridPlacement.line(1));
        childStyle.gridRow = new TaffyLine<>(GridPlacement.auto(), GridPlacement.line(1));
        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(grid, child);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.maxContent(), AvailableSpace.maxContent()));

        DetailedGridInfo details = tree.getDetailedLayoutInfo(root).grid();
        assertEquals(1, details.columns().sizes().size());
        assertEquals(1, details.rows().sizes().size());
        assertEquals("10.0000px", details.gridTemplateColumns());
        assertEquals("10.0000px", details.gridTemplateRows());
    }
}
