package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyPoint;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.Overflow;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RtlScrollbarGutterTest {
    @Test
    void verticalScrollbarOccupiesInlineStartAcrossLayoutAlgorithms() {
        for (TaffyDisplay display : new TaffyDisplay[]{TaffyDisplay.BLOCK, TaffyDisplay.FLEX, TaffyDisplay.GRID}) {
            TaffyStyle containerStyle = sizedStyle(100f, 100f);
            containerStyle.display = display;
            containerStyle.direction = TaffyDirection.RTL;
            containerStyle.overflow = new TaffyPoint<>(Overflow.VISIBLE, Overflow.SCROLL);
            containerStyle.scrollbarWidth = 15f;
            if (display == TaffyDisplay.GRID) {
                containerStyle.gridTemplateColumns.add(TrackSizingFunction.percent(1f));
            }

            TaffyStyle childStyle = new TaffyStyle();
            childStyle.direction = TaffyDirection.RTL;
            childStyle.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(200f));
            if (display == TaffyDisplay.FLEX) {
                childStyle.flexGrow = 1f;
            }

            TaffyTree tree = new TaffyTree();
            NodeId child = tree.newLeaf(childStyle);
            NodeId root = tree.newWithChildren(containerStyle, child);
            tree.computeLayout(root, TaffySize.maxContent());

            assertEquals(15f, tree.getLayout(child).location().x, 0.01f, display.toString());
            assertEquals(85f, tree.getLayout(child).size().width, 0.01f, display.toString());
        }
    }

    private static TaffyStyle sizedStyle(float width, float height) {
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(TaffyDimension.length(width), TaffyDimension.length(height));
        return style;
    }
}
