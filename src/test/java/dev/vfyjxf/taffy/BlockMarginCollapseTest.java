package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockMarginCollapseTest {
    @Test
    void minHeightPreventsLastChildBottomMarginFromCollapsingOutward() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(50f, Float.NaN);

        TaffyStyle firstStyle = blockStyle(Float.NaN, Float.NaN);
        firstStyle.minSize = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(40f));
        firstStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(10f)
        );
        TaffyStyle grandchildStyle = blockStyle(Float.NaN, 10f);
        grandchildStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(20f)
        );
        NodeId grandchild = tree.newLeaf(grandchildStyle);
        NodeId first = tree.newWithChildren(firstStyle, grandchild);
        NodeId second = tree.newLeaf(blockStyle(Float.NaN, 10f));
        NodeId root = tree.newWithChildren(rootStyle, first, second);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(50f), AvailableSpace.maxContent()));

        assertEquals(40f, tree.getLayout(first).size().height, 0.01f);
        assertEquals(50f, tree.getLayout(second).location().y, 0.01f);
        assertEquals(60f, tree.getLayout(root).size().height, 0.01f);
    }

    @Test
    void parentMinHeightPreventsMarginCollapseThroughAnEmptyChild() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(50f, Float.NaN);

        TaffyStyle firstStyle = blockStyle(Float.NaN, Float.NaN);
        firstStyle.minSize = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(30f));
        TaffyStyle grandchildStyle = blockStyle(Float.NaN, Float.NaN);
        grandchildStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(20f)
        );
        NodeId grandchild = tree.newLeaf(grandchildStyle);
        NodeId first = tree.newWithChildren(firstStyle, grandchild);
        NodeId second = tree.newLeaf(blockStyle(Float.NaN, 10f));
        NodeId root = tree.newWithChildren(rootStyle, first, second);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(50f), AvailableSpace.maxContent()));

        assertEquals(20f, tree.getLayout(first).location().y, 0.01f);
        assertEquals(30f, tree.getLayout(first).size().height, 0.01f);
        assertEquals(50f, tree.getLayout(second).location().y, 0.01f);
        assertEquals(60f, tree.getLayout(root).size().height, 0.01f);
    }

    @Test
    void smallerMinHeightStillAllowsLastChildBottomMarginToCollapseOutward() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(50f, Float.NaN);

        TaffyStyle firstStyle = blockStyle(Float.NaN, Float.NaN);
        firstStyle.minSize = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(10f));
        firstStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(10f)
        );
        TaffyStyle grandchildStyle = blockStyle(Float.NaN, 20f);
        grandchildStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(30f)
        );
        NodeId grandchild = tree.newLeaf(grandchildStyle);
        NodeId first = tree.newWithChildren(firstStyle, grandchild);
        NodeId second = tree.newLeaf(blockStyle(Float.NaN, 10f));
        NodeId root = tree.newWithChildren(rootStyle, first, second);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(50f), AvailableSpace.maxContent()));

        assertEquals(20f, tree.getLayout(first).size().height, 0.01f);
        assertEquals(50f, tree.getLayout(second).location().y, 0.01f);
        assertEquals(60f, tree.getLayout(root).size().height, 0.01f);
    }

    private static TaffyStyle blockStyle(float width, float height) {
        TaffyStyle style = new TaffyStyle();
        style.display = TaffyDisplay.BLOCK;
        style.size = new TaffySize<>(
            Float.isNaN(width) ? TaffyDimension.AUTO : TaffyDimension.length(width),
            Float.isNaN(height) ? TaffyDimension.AUTO : TaffyDimension.length(height)
        );
        return style;
    }
}
