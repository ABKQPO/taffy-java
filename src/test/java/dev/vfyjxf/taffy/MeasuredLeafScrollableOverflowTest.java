package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyPoint;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.Overflow;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MeasuredLeafScrollableOverflowTest {

    @Test
    void horizontalScrollbarKeepsMeasuredLeafOverflowDuringLayout() {
        Layout layout = layoutWithOverflow(new TaffyPoint<>(Overflow.SCROLL, Overflow.VISIBLE));

        assertEquals(210f, layout.scrollableOverflowRect().right, 0.001f);
        assertEquals(165f, layout.scrollWidth(), 0.001f);
        assertEquals(0f, layout.scrollHeight(), 0.001f);
    }

    @Test
    void verticalScrollbarKeepsMeasuredLeafOverflowDuringLayout() {
        Layout layout = layoutWithOverflow(new TaffyPoint<>(Overflow.VISIBLE, Overflow.SCROLL));

        assertEquals(210f, layout.scrollableOverflowRect().right, 0.001f);
        assertEquals(180f, layout.scrollWidth(), 0.001f);
        assertEquals(0f, layout.scrollHeight(), 0.001f);
    }

    private Layout layoutWithOverflow(TaffyPoint<Overflow> overflow) {
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(TaffyDimension.length(45f), TaffyDimension.length(45f));
        style.overflow = overflow;
        style.scrollbarWidth = 15f;

        TaffyTree tree = new TaffyTree();
        NodeId node = tree.newLeafWithMeasure(style, (knownDimensions, availableSpace) -> new FloatSize(210f, 10f));
        tree.computeLayout(node, TaffySize.maxContent());
        return tree.getLayout(node);
    }
}
