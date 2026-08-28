package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridPercentageContributionTest {

    @Test
    void percentageSizedItemsDoNotExpandAutoTracksBeyondTheirGridAreas() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle grid = new TaffyStyle();
        grid.display = TaffyDisplay.GRID;
        grid.size = new TaffySize<>(TaffyDimension.length(200f), TaffyDimension.AUTO);
        grid.gridTemplateColumns = List.of(TrackSizingFunction.auto(), TrackSizingFunction.auto());
        grid.gridTemplateRows = List.of(
            TrackSizingFunction.fixed(LengthPercentage.length(50f)),
            TrackSizingFunction.fixed(LengthPercentage.length(50f))
        );

        TaffyStyle firstItem = new TaffyStyle();
        firstItem.size = new TaffySize<>(TaffyDimension.percent(1f), TaffyDimension.length(50f));
        TaffyStyle secondItem = firstItem.copy();
        secondItem.gridColumn = new TaffyLine<>(GridPlacement.line(2), GridPlacement.auto());
        secondItem.gridRow = new TaffyLine<>(GridPlacement.line(2), GridPlacement.auto());

        NodeId first = tree.newLeaf(firstItem);
        NodeId second = tree.newLeaf(secondItem);
        NodeId root = tree.newWithChildren(grid, List.of(first, second));
        tree.computeLayout(root, new TaffySize<>(AvailableSpace.maxContent(), AvailableSpace.maxContent()));

        assertEquals(100f, tree.getLayout(first).size().width, 0.001f,
            () -> "tracks=" + tree.getDetailedLayoutInfo(root).grid().gridTemplateColumns()
                + ", root=" + tree.getLayout(root).size());
        assertEquals(100f, tree.getLayout(second).size().width, 0.001f);
        assertEquals(100f, tree.getLayout(second).location().x, 0.001f);
    }
}
