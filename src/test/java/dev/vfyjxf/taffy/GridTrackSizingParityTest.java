package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.TaffyDisplay;
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

    private MeasureFunc fixedMeasure(float width, float height) {
        return (knownDimensions, availableSpace) -> new FloatSize(
            Float.isNaN(knownDimensions.width) ? width : knownDimensions.width,
            Float.isNaN(knownDimensions.height) ? height : knownDimensions.height);
    }
}
