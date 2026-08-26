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
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridAbsoluteGridTest {

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
}
