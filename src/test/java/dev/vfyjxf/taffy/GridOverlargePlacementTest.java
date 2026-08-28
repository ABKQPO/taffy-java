package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridOverlargePlacementTest {

    @Test
    void clampsExtremeGridLinesWithoutAllocatingAGridSizedMatrix() {
        TaffyTree tree = new TaffyTree();

        TaffyStyle firstStyle = sizedItem();
        firstStyle.gridRow = new TaffyLine<>(GridPlacement.line(-32768), GridPlacement.auto());
        firstStyle.gridColumn = new TaffyLine<>(GridPlacement.line(32767), GridPlacement.auto());
        NodeId first = tree.newLeaf(firstStyle);

        TaffyStyle secondStyle = sizedItem();
        secondStyle.gridRow = new TaffyLine<>(GridPlacement.line(32767), GridPlacement.span(10000));
        secondStyle.gridColumn = new TaffyLine<>(GridPlacement.span(10000), GridPlacement.line(-32768));
        NodeId second = tree.newLeaf(secondStyle);

        TaffyStyle gridStyle = new TaffyStyle();
        gridStyle.display = TaffyDisplay.GRID;
        NodeId grid = tree.newWithChildren(gridStyle, first, second);

        tree.computeLayout(grid, TaffySize.maxContent());

        assertLayout(tree.getLayout(grid), 0f, 0f, 40f, 40f);
        assertLayout(tree.getLayout(first), 20f, 0f, 20f, 20f);
        assertLayout(tree.getLayout(second), 0f, 20f, 20f, 20f);
    }

    private static TaffyStyle sizedItem() {
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(TaffyDimension.length(20f), TaffyDimension.length(20f));
        return style;
    }

    private static void assertLayout(Layout layout, float x, float y, float width, float height) {
        assertEquals(x, layout.location().x, 0.1f);
        assertEquals(y, layout.location().y, 0.1f);
        assertEquals(width, layout.size().width, 0.1f);
        assertEquals(height, layout.size().height, 0.1f);
    }
}
