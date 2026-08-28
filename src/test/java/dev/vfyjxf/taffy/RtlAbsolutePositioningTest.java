package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RtlAbsolutePositioningTest {
    @Test
    void flexStartAlignmentUsesTheInlineStartInRtl() {
        TaffyStyle containerStyle = sizedStyle(100f, 40f);
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.direction = TaffyDirection.RTL;

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.position = TaffyPosition.ABSOLUTE;

        TaffyTree tree = new TaffyTree();
        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(containerStyle, child);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(100f, tree.getLayout(child).location().x, 0.01f);
    }

    @Test
    void gridStartAlignmentUsesTheInlineStartInRtl() {
        TaffyStyle containerStyle = sizedStyle(100f, 40f);
        containerStyle.display = TaffyDisplay.GRID;
        containerStyle.direction = TaffyDirection.RTL;
        containerStyle.gridTemplateColumns.add(TrackSizingFunction.fixed(40f));

        TaffyStyle childStyle = sizedStyle(20f, 20f);
        childStyle.position = TaffyPosition.ABSOLUTE;
        childStyle.gridColumn = new TaffyLine<>(GridPlacement.line(1), GridPlacement.line(2));

        TaffyTree tree = new TaffyTree();
        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(containerStyle, child);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(80f, tree.getLayout(child).location().x, 0.01f);
    }

    @Test
    void gridAbsoluteColumnStartUsesTheOppositePaddingEdgeInRtl() {
        TaffyStyle containerStyle = sizedStyle(180f, 160f);
        containerStyle.display = TaffyDisplay.GRID;
        containerStyle.direction = TaffyDirection.RTL;
        containerStyle.padding = new TaffyRect<>(
            LengthPercentage.length(40f),
            LengthPercentage.length(20f),
            LengthPercentage.length(10f),
            LengthPercentage.length(30f));
        for (int index = 0; index < 3; index++) {
            containerStyle.gridTemplateColumns.add(TrackSizingFunction.fixed(40f));
            containerStyle.gridTemplateRows.add(TrackSizingFunction.fixed(40f));
        }

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.position = TaffyPosition.ABSOLUTE;
        childStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(4f),
            LengthPercentageAuto.length(3f),
            LengthPercentageAuto.length(1f),
            LengthPercentageAuto.length(2f));
        childStyle.gridColumn = new TaffyLine<>(GridPlacement.line(1), GridPlacement.auto());

        TaffyTree tree = new TaffyTree();
        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(containerStyle, child);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(4f, tree.getLayout(child).location().x, 0.01f);
        assertEquals(153f, tree.getLayout(child).size().width, 0.01f);
    }

    private static TaffyStyle sizedStyle(float width, float height) {
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(TaffyDimension.length(width), TaffyDimension.length(height));
        return style;
    }
}
