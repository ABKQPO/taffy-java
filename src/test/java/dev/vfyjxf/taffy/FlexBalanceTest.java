package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.FlexWrap;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlexBalanceTest {
    @Test
    void balanceSplitsItemsIntoEvenLines() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.FLEX;
        rootStyle.flexWrap = FlexWrap.BALANCE;
        rootStyle.flexLineCount = 2;
        rootStyle.size = new TaffySize<>(
            TaffyDimension.length(100f), TaffyDimension.AUTO
        );

        NodeId[] children = new NodeId[4];
        for (int i = 0; i < children.length; i++) {
            TaffyStyle childStyle = new TaffyStyle();
            childStyle.display = TaffyDisplay.BLOCK;
            childStyle.size = new TaffySize<>(
                TaffyDimension.length(30f), TaffyDimension.length(10f)
            );
            children[i] = tree.newLeaf(childStyle);
        }
        NodeId root = tree.newWithChildren(rootStyle, children);

        tree.computeLayout(root, new TaffySize<>(
            AvailableSpace.definite(100f), AvailableSpace.maxContent()
        ));

        assertEquals(20f, tree.getLayout(root).size().height, 0.01f);
        assertEquals(0f, tree.getLayout(children[0]).location().y, 0.01f);
        assertEquals(0f, tree.getLayout(children[1]).location().y, 0.01f);
        assertEquals(10f, tree.getLayout(children[2]).location().y, 0.01f);
        assertEquals(10f, tree.getLayout(children[3]).location().y, 0.01f);
    }

    @Test
    void balanceReverseReportsReverseCrossAxisMode() {
        assertTrue(FlexWrap.BALANCE_REVERSE.isReverse());
        assertTrue(FlexWrap.BALANCE_REVERSE.isBalance());
        assertEquals(1, new TaffyStyle().flexLineCount);
        assertEquals(FlexWrap.BALANCE, FlexWrap.parse("wrap balance"));
        assertEquals(FlexWrap.BALANCE_REVERSE, FlexWrap.parse("balance wrap-reverse"));
    }

    @Test
    void balanceUsesRequestedLineCountForIntrinsicContainers() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.FLEX;
        rootStyle.flexWrap = FlexWrap.BALANCE;
        rootStyle.flexLineCount = 2;

        NodeId[] children = new NodeId[4];
        for (int index = 0; index < children.length; index++) {
            TaffyStyle childStyle = new TaffyStyle();
            childStyle.display = TaffyDisplay.BLOCK;
            childStyle.size = new TaffySize<>(
                TaffyDimension.length(30f), TaffyDimension.length(30f)
            );
            children[index] = tree.newLeaf(childStyle);
        }
        NodeId root = tree.newWithChildren(rootStyle, children);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.maxContent(), AvailableSpace.maxContent()));

        assertEquals(60f, tree.getLayout(root).size().width, 0.01f);
        assertEquals(60f, tree.getLayout(root).size().height, 0.01f);
        assertEquals(0f, tree.getLayout(children[2]).location().x, 0.01f);
        assertEquals(30f, tree.getLayout(children[2]).location().y, 0.01f);
    }

    @Test
    void balanceKeepsZeroSizedItemsOnThePrecedingLine() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.FLEX;
        rootStyle.flexWrap = FlexWrap.BALANCE;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.AUTO);

        float[] widths = {40f, 0f, 40f, 0f, 40f};
        float[] heights = {30f, 10f, 30f, 10f, 30f};
        NodeId[] children = new NodeId[widths.length];
        for (int index = 0; index < children.length; index++) {
            TaffyStyle childStyle = new TaffyStyle();
            childStyle.display = TaffyDisplay.BLOCK;
            childStyle.size = new TaffySize<>(
                TaffyDimension.length(widths[index]), TaffyDimension.length(heights[index])
            );
            children[index] = tree.newLeaf(childStyle);
        }
        NodeId root = tree.newWithChildren(rootStyle, children);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.maxContent(), AvailableSpace.maxContent()));

        assertEquals(60f, tree.getLayout(root).size().height, 0.01f);
        assertEquals(0f, tree.getLayout(children[1]).location().y, 0.01f);
        assertEquals(0f, tree.getLayout(children[3]).location().y, 0.01f);
        assertEquals(30f, tree.getLayout(children[4]).location().y, 0.01f);
    }

    @Test
    void balanceDividesDefiniteCrossSpaceWhileMeasuringItems() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.FLEX;
        rootStyle.flexDirection = FlexDirection.COLUMN;
        rootStyle.flexWrap = FlexWrap.BALANCE;
        rootStyle.flexLineCount = 2;
        rootStyle.alignItems = AlignItems.START;
        rootStyle.alignContent = AlignContent.START;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(210f), TaffyDimension.length(100f));
        rootStyle.gap = new TaffySize<>(LengthPercentage.length(10f), LengthPercentage.ZERO);

        TaffyStyle measuredStyle = new TaffyStyle();
        measuredStyle.display = TaffyDisplay.BLOCK;
        NodeId measured = tree.newLeafWithMeasure(measuredStyle, (knownDimensions, availableSpace) -> {
            float width = Float.isNaN(knownDimensions.width)
                ? availableSpace.width.unwrapOr(300f)
                : knownDimensions.width;
            return new FloatSize(width, 30f);
        });

        TaffyStyle fixedStyle = new TaffyStyle();
        fixedStyle.display = TaffyDisplay.BLOCK;
        fixedStyle.size = new TaffySize<>(TaffyDimension.length(30f), TaffyDimension.length(60f));
        NodeId fixed = tree.newLeaf(fixedStyle);
        NodeId root = tree.newWithChildren(rootStyle, measured, fixed);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.maxContent(), AvailableSpace.maxContent()));

        assertEquals(100f, tree.getLayout(measured).size().width, 0.01f);
        assertEquals(110f, tree.getLayout(fixed).location().x, 0.01f);
    }

    @Test
    void balanceFitContentUsesTheRequestedLineCountForItsMaxContentSize() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle outerStyle = new TaffyStyle();
        outerStyle.display = TaffyDisplay.FLEX;
        outerStyle.flexDirection = FlexDirection.COLUMN;
        outerStyle.alignItems = AlignItems.FLEX_START;
        outerStyle.size = new TaffySize<>(TaffyDimension.length(200f), TaffyDimension.length(200f));

        TaffyStyle balancedStyle = new TaffyStyle();
        balancedStyle.display = TaffyDisplay.FLEX;
        balancedStyle.flexWrap = FlexWrap.BALANCE;
        balancedStyle.flexLineCount = 2;
        balancedStyle.size = new TaffySize<>(TaffyDimension.fitContent(), TaffyDimension.AUTO);
        balancedStyle.gap = new TaffySize<>(LengthPercentage.length(20f), LengthPercentage.length(20f));

        NodeId[] children = new NodeId[3];
        for (int index = 0; index < children.length; index++) {
            TaffyStyle childStyle = new TaffyStyle();
            childStyle.display = TaffyDisplay.BLOCK;
            childStyle.size = new TaffySize<>(TaffyDimension.length(40f), TaffyDimension.length(25f));
            children[index] = tree.newLeaf(childStyle);
        }
        NodeId balanced = tree.newWithChildren(balancedStyle, children);
        NodeId outer = tree.newWithChildren(outerStyle, balanced);

        tree.computeLayout(outer, new TaffySize<>(AvailableSpace.maxContent(), AvailableSpace.maxContent()));

        assertEquals(100f, tree.getLayout(balanced).size().width, 0.01f);
        assertEquals(70f, tree.getLayout(balanced).size().height, 0.01f);
    }
}
