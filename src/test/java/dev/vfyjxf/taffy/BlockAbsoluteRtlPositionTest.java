package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockAbsoluteRtlPositionTest {

    @Test
    void rtlBlockUsesRightStaticEdgeForUninsetAbsoluteChild() {
        TaffyTree tree = new TaffyTree();
        NodeId leftInset = tree.newLeaf(absoluteStyle(10f, null));
        NodeId rightInset = tree.newLeaf(absoluteStyle(null, 10f));
        NodeId staticPosition = tree.newLeaf(absoluteStyle(null, null));

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.display = TaffyDisplay.BLOCK;
        containerStyle.direction = TaffyDirection.RTL;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(100f));
        NodeId container = tree.newWithChildren(containerStyle, leftInset, rightInset, staticPosition);

        tree.computeLayout(container, TaffySize.maxContent());

        assertEquals(10f, tree.getLayout(leftInset).location().x, 0.001f);
        assertEquals(70f, tree.getLayout(rightInset).location().x, 0.001f);
        assertEquals(80f, tree.getLayout(staticPosition).location().x, 0.001f);
    }

    private TaffyStyle absoluteStyle(Float left, Float right) {
        TaffyStyle style = new TaffyStyle();
        style.direction = TaffyDirection.RTL;
        style.position = TaffyPosition.ABSOLUTE;
        style.size = new TaffySize<>(TaffyDimension.length(20f), TaffyDimension.length(20f));
        style.inset = TaffyRect.ltrb(
            left == null ? LengthPercentageAuto.AUTO : LengthPercentageAuto.length(left),
            LengthPercentageAuto.length(10f),
            right == null ? LengthPercentageAuto.AUTO : LengthPercentageAuto.length(right),
            LengthPercentageAuto.AUTO
        );
        return style;
    }
}
