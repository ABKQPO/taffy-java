package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyFloat;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockAlignContentTest {
    @Test
    void endOffsetsTheEntireInFlowStack() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, 120f);
        rootStyle.alignContent = AlignContent.END;
        NodeId first = tree.newLeaf(blockStyle(50f, 20f));
        NodeId second = tree.newLeaf(blockStyle(50f, 20f));
        NodeId third = tree.newLeaf(blockStyle(50f, 20f));
        NodeId root = tree.newWithChildren(rootStyle, first, second, third);

        compute(tree, root);

        assertEquals(60f, tree.getLayout(first).location().y, 0.01f);
        assertEquals(80f, tree.getLayout(second).location().y, 0.01f);
        assertEquals(100f, tree.getLayout(third).location().y, 0.01f);
    }

    @Test
    void spaceAroundFallsBackToCenterForTheSingleBlockStack() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, 120f);
        rootStyle.alignContent = AlignContent.SPACE_AROUND;
        NodeId first = tree.newLeaf(blockStyle(50f, 20f));
        NodeId second = tree.newLeaf(blockStyle(50f, 20f));
        NodeId third = tree.newLeaf(blockStyle(50f, 20f));
        NodeId root = tree.newWithChildren(rootStyle, first, second, third);

        compute(tree, root);

        assertEquals(30f, tree.getLayout(first).location().y, 0.01f);
        assertEquals(50f, tree.getLayout(second).location().y, 0.01f);
        assertEquals(70f, tree.getLayout(third).location().y, 0.01f);
    }

    @Test
    void endOffsetsFloatedChildrenAlongsideNormalFlow() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, 120f);
        rootStyle.alignContent = AlignContent.END;

        TaffyStyle floatStyle = blockStyle(30f, 20f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);
        NodeId inFlow = tree.newLeaf(blockStyle(50f, 20f));
        NodeId root = tree.newWithChildren(rootStyle, floated, inFlow);

        compute(tree, root);

        assertEquals(100f, tree.getLayout(floated).location().y, 0.01f);
        assertEquals(100f, tree.getLayout(inFlow).location().y, 0.01f);
    }

    @Test
    void nonNormalAlignContentPreventsChildMarginCollapse() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);
        TaffyStyle containerStyle = blockStyle(100f, Float.NaN);
        containerStyle.alignContent = AlignContent.START;

        TaffyStyle childStyle = blockStyle(40f, 10f);
        childStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.length(20f),
            LengthPercentageAuto.length(30f));
        NodeId child = tree.newLeaf(childStyle);
        NodeId container = tree.newWithChildren(containerStyle, child);
        NodeId root = tree.newWithChildren(rootStyle, container);

        compute(tree, root);

        assertEquals(60f, tree.getLayout(container).size().height, 0.01f);
        assertEquals(20f, tree.getLayout(child).location().y, 0.01f);
    }

    @Test
    void safeEndFallsBackToStartWhenTheStackOverflows() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, 100f);
        rootStyle.alignContent = AlignContent.SAFE_END;
        NodeId first = tree.newLeaf(blockStyle(50f, 80f));
        NodeId second = tree.newLeaf(blockStyle(50f, 80f));
        NodeId root = tree.newWithChildren(rootStyle, first, second);

        compute(tree, root);

        assertEquals(0f, tree.getLayout(first).location().y, 0.01f);
        assertEquals(80f, tree.getLayout(second).location().y, 0.01f);
    }

    @Test
    void stretchUsesTheSingleStackFallbackWithoutResizingTheChild() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, 120f);
        rootStyle.alignContent = AlignContent.STRETCH;
        NodeId child = tree.newLeaf(blockStyle(50f, 20f));
        NodeId root = tree.newWithChildren(rootStyle, child);

        compute(tree, root);

        assertEquals(0f, tree.getLayout(child).location().y, 0.01f);
        assertEquals(20f, tree.getLayout(child).size().height, 0.01f);
    }

    @Test
    void absoluteChildrenDoNotParticipateInBlockAlignment() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, 120f);
        rootStyle.alignContent = AlignContent.END;
        NodeId inFlow = tree.newLeaf(blockStyle(50f, 20f));

        TaffyStyle absoluteStyle = blockStyle(30f, 30f);
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(10f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.AUTO);
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId root = tree.newWithChildren(rootStyle, inFlow, absolute);

        compute(tree, root);

        assertEquals(100f, tree.getLayout(inFlow).location().y, 0.01f);
        assertEquals(10f, tree.getLayout(absolute).location().x, 0.01f);
        assertEquals(5f, tree.getLayout(absolute).location().y, 0.01f);
    }

    @Test
    void flowRootContainsTheHeightOfItsFloatedChildren() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);
        rootStyle.display = TaffyDisplay.FLOW_ROOT;

        TaffyStyle floatStyle = blockStyle(20f, 30f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);
        NodeId root = tree.newWithChildren(rootStyle, floated);

        compute(tree, root);

        assertEquals(30f, tree.getLayout(root).size().height, 0.01f);
        assertEquals(0f, tree.getLayout(floated).location().y, 0.01f);
    }

    private static TaffyStyle blockStyle(float width, float height) {
        TaffyStyle style = new TaffyStyle();
        style.display = TaffyDisplay.BLOCK;
        style.size = new TaffySize<>(
            Float.isNaN(width) ? TaffyDimension.AUTO : TaffyDimension.length(width),
            Float.isNaN(height) ? TaffyDimension.AUTO : TaffyDimension.length(height));
        return style;
    }

    private static void compute(TaffyTree tree, NodeId root) {
        tree.computeLayout(root, new TaffySize<>(
            AvailableSpace.definite(100f), AvailableSpace.maxContent()));
    }
}
