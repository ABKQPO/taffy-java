package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.NamedGridLine;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.tree.DetailedGridInfo;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridAbsoluteGridTest {

    @Test
    @DisplayName("Absolute grid placement normalizes named lines before grid bounds")
    void absoluteGridChildNormalizesNamedLinesBeforeGridBounds() {
        TaffyStyle rootStyle = absoluteGridContainer();

        TaffyStyle swappedNamedLines = absoluteChild(
            GridPlacement.namedLine("missing"), GridPlacement.line(3),
            GridPlacement.namedLine("missing"), GridPlacement.line(3));
        TaffyStyle missingNamedLines = absoluteChild(
            GridPlacement.namedLine("missing"), GridPlacement.namedLine("another"),
            GridPlacement.namedLine("missing"), GridPlacement.namedLine("another"));
        TaffyStyle spanBeforeFirstLine = absoluteChild(
            GridPlacement.span(2), GridPlacement.line(1),
            GridPlacement.span(2), GridPlacement.line(1));

        TaffyTree tree = new TaffyTree();
        NodeId swapped = tree.newLeaf(swappedNamedLines);
        NodeId missing = tree.newLeaf(missingNamedLines);
        NodeId span = tree.newLeaf(spanBeforeFirstLine);
        NodeId root = tree.newWithChildren(rootStyle, swapped, missing, span);
        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(200f), AvailableSpace.definite(150f)));

        assertLayout(tree.getLayout(swapped), 110f, 80f, 90f, 70f);
        assertLayout(tree.getLayout(missing), 0f, 0f, 200f, 150f);
        assertLayout(tree.getLayout(span), 0f, 0f, 10f, 10f);
    }

    @Test
    @DisplayName("Auto-start definite-end placement creates a negative implicit track")
    void autoStartDefiniteEndPlacementCreatesNegativeImplicitTrack() {
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.GRID;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(10f), TaffyDimension.length(10f));

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.gridColumn = new TaffyLine<>(GridPlacement.auto(), GridPlacement.line(1));
        childStyle.size = new TaffySize<>(TaffyDimension.length(10f), TaffyDimension.length(10f));

        TaffyTree tree = new TaffyTree();
        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(rootStyle, child);
        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(10f), AvailableSpace.definite(10f)));

        DetailedGridInfo grid = tree.getDetailedLayoutInfo(root).grid();
        assertEquals(1, grid.columns().counts().negativeImplicit);
        assertEquals(0, grid.columns().counts().explicit);
        assertEquals(0, grid.columns().counts().positiveImplicit);
        assertEquals("10.0000px", grid.gridTemplateColumns());
    }

    @Test
    @DisplayName("Absolute grid child resolves known named grid lines")
    void absoluteGridChildResolvesKnownNamedGridLines() {
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.GRID;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(200.0f), TaffyDimension.length(150.0f));
        rootStyle.padding = TaffyRect.all(LengthPercentage.length(10.0f));
        rootStyle.gridTemplateColumns = List.of(
            TrackSizingFunction.fixed(LengthPercentage.length(40.0f)),
            TrackSizingFunction.fixed(LengthPercentage.length(60.0f))
        );
        rootStyle.gridTemplateRows = List.of(
            TrackSizingFunction.fixed(LengthPercentage.length(20.0f)),
            TrackSizingFunction.fixed(LengthPercentage.length(50.0f))
        );
        rootStyle.gridTemplateColumnNames = List.of(
            new NamedGridLine("start", 1),
            new NamedGridLine("end", 3)
        );
        rootStyle.gridTemplateRowNames = List.of(
            new NamedGridLine("start", 1),
            new NamedGridLine("end", 3)
        );

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.position = TaffyPosition.ABSOLUTE;
        childStyle.gridColumn = new TaffyLine<>(
            GridPlacement.namedLine("start"),
            GridPlacement.namedLine("end")
        );
        childStyle.gridRow = new TaffyLine<>(
            GridPlacement.namedLine("start"),
            GridPlacement.namedLine("end")
        );
        childStyle.inset = TaffyRect.all(LengthPercentageAuto.ZERO);

        TaffyTree tree = new TaffyTree();
        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(rootStyle, child);
        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(200.0f), AvailableSpace.definite(150.0f)));

        Layout childLayout = tree.getLayout(child);
        assertEquals(10.0f, childLayout.location().x, 0.01f);
        assertEquals(10.0f, childLayout.location().y, 0.01f);
        assertEquals(100.0f, childLayout.size().width, 0.01f);
        assertEquals(70.0f, childLayout.size().height, 0.01f);
    }

    private static TaffyStyle absoluteGridContainer() {
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.GRID;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(200f), TaffyDimension.length(150f));
        rootStyle.padding = TaffyRect.all(LengthPercentage.length(10f));
        rootStyle.gridTemplateColumns = List.of(
            TrackSizingFunction.fixed(LengthPercentage.length(40f)),
            TrackSizingFunction.fixed(LengthPercentage.length(60f)));
        rootStyle.gridTemplateRows = List.of(
            TrackSizingFunction.fixed(LengthPercentage.length(20f)),
            TrackSizingFunction.fixed(LengthPercentage.length(50f)));
        return rootStyle;
    }

    private static TaffyStyle absoluteChild(
        GridPlacement columnStart,
        GridPlacement columnEnd,
        GridPlacement rowStart,
        GridPlacement rowEnd) {
        TaffyStyle style = new TaffyStyle();
        style.position = TaffyPosition.ABSOLUTE;
        style.gridColumn = new TaffyLine<>(columnStart, columnEnd);
        style.gridRow = new TaffyLine<>(rowStart, rowEnd);
        style.inset = TaffyRect.all(LengthPercentageAuto.ZERO);
        return style;
    }

    private static void assertLayout(Layout layout, float x, float y, float width, float height) {
        assertEquals(x, layout.location().x, 0.01f);
        assertEquals(y, layout.location().y, 0.01f);
        assertEquals(width, layout.size().width, 0.01f);
        assertEquals(height, layout.size().height, 0.01f);
    }
}
