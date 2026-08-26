package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyPoint;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.Contain;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.Overflow;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.ScrollableOverflow;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScrollableOverflowTest {
    @Test
    void absoluteChildrenPreserveNegativeScrollableOverflowAcrossLayoutAlgorithms() {
        TaffyDisplay[] displays = {TaffyDisplay.BLOCK, TaffyDisplay.FLEX, TaffyDisplay.GRID};
        for (TaffyDisplay display : displays) {
            TaffyStyle rootStyle = new TaffyStyle();
            rootStyle.display = display;
            rootStyle.position = TaffyPosition.RELATIVE;
            rootStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(100f));

            TaffyStyle absoluteStyle = new TaffyStyle();
            absoluteStyle.position = TaffyPosition.ABSOLUTE;
            absoluteStyle.size = new TaffySize<>(TaffyDimension.length(20f), TaffyDimension.length(30f));
            absoluteStyle.inset = new TaffyRect<>(
                LengthPercentageAuto.length(-30f),
                LengthPercentageAuto.AUTO,
                LengthPercentageAuto.length(10f),
                LengthPercentageAuto.AUTO
            );

            TaffyTree tree = new TaffyTree();
            NodeId absolute = tree.newLeaf(absoluteStyle);
            NodeId root = tree.newWithChildren(rootStyle, absolute);
            tree.computeLayout(root, TaffySize.maxContent());

            assertEquals(FloatRect.ltrb(-30f, 0f, 0f, 40f), tree.getLayout(root).scrollableOverflowRect(),
                display.toString());
        }
    }

    @Test
    void overflowContributionRespectsContainmentAndUnreachableScrollOrigins() {
        TaffyPoint<Overflow> visibleOverflow = new TaffyPoint<>(Overflow.VISIBLE, Overflow.VISIBLE);
        FloatRect childOverflow = FloatRect.ltrb(-5f, -7f, 30f, 40f);

        assertEquals(
            FloatRect.ltrb(0f, -2f, 35f, 45f),
            ScrollableOverflow.contribution(
                new FloatPoint(5f, 5f),
                new FloatSize(10f, 10f),
                childOverflow,
                visibleOverflow,
                Contain.NONE,
                false
            )
        );
        assertEquals(
            FloatRect.ltrb(5f, 5f, 15f, 15f),
            ScrollableOverflow.contribution(
                new FloatPoint(5f, 5f),
                new FloatSize(10f, 10f),
                childOverflow,
                visibleOverflow,
                Contain.LAYOUT,
                false
            )
        );
        assertEquals(
            FloatRect.zero(),
            ScrollableOverflow.contribution(
                new FloatPoint(-40f, 5f),
                new FloatSize(20f, 20f),
                FloatRect.zero(),
                visibleOverflow,
                Contain.NONE,
                true
            )
        );
    }

    @Test
    void nestedAbsoluteRepositionRefreshesAncestorScrollableOverflow() {
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.BLOCK;
        rootStyle.position = TaffyPosition.RELATIVE;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(100f));

        TaffyStyle staticParentStyle = new TaffyStyle();
        staticParentStyle.size = new TaffySize<>(TaffyDimension.length(30f), TaffyDimension.length(30f));
        staticParentStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.length(50f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.ZERO
        );

        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.size = new TaffySize<>(TaffyDimension.length(20f), TaffyDimension.length(20f));
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(120f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.AUTO
        );

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(staticParentStyle, absolute);
        NodeId root = tree.newWithChildren(rootStyle, staticParent);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(FloatRect.ltrb(0f, 0f, 140f, 30f), tree.getLayout(root).scrollableOverflowRect());
    }
}
