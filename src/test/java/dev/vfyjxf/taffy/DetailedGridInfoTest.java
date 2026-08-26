package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.GridPlacement;
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
}
