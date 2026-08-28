package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.Clear;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.Overflow;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyFloat;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatClearTest {
    @Test
    void leftFloatDoesNotShrinkTheFollowingBlockBox() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);

        TaffyStyle floatStyle = blockStyle(30f, 20f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);

        TaffyStyle followingStyle = blockStyle(Float.NaN, 10f);
        NodeId following = tree.newLeaf(followingStyle);
        NodeId root = tree.newWithChildren(rootStyle, floated, following);

        compute(tree, root);

        assertEquals(0f, tree.getLayout(floated).location().x, 0.01f);
        assertEquals(0f, tree.getLayout(following).location().x, 0.01f);
        assertEquals(100f, tree.getLayout(following).size().width, 0.01f);
    }

    @Test
    void clearBothMovesBlockBelowExistingFloat() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);

        TaffyStyle floatStyle = blockStyle(30f, 20f);
        floatStyle.floatMode = TaffyFloat.RIGHT;
        NodeId floated = tree.newLeaf(floatStyle);

        TaffyStyle clearedStyle = blockStyle(Float.NaN, 10f);
        clearedStyle.clear = Clear.BOTH;
        NodeId cleared = tree.newLeaf(clearedStyle);
        NodeId root = tree.newWithChildren(rootStyle, floated, cleared);

        compute(tree, root);

        assertEquals(20f, tree.getLayout(cleared).location().y, 0.01f);
        assertEquals(0f, tree.getLayout(cleared).location().x, 0.01f);
        assertEquals(TaffyFloat.LEFT, TaffyFloat.parse("left"));
        assertEquals(Clear.BOTH, Clear.parse("both"));
    }

    @Test
    void oppositeFloatsDoNotShrinkTheFollowingBlockBox() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);

        TaffyStyle leftStyle = blockStyle(30f, 20f);
        leftStyle.floatMode = TaffyFloat.LEFT;
        NodeId left = tree.newLeaf(leftStyle);

        TaffyStyle rightStyle = blockStyle(20f, 20f);
        rightStyle.floatMode = TaffyFloat.RIGHT;
        NodeId right = tree.newLeaf(rightStyle);

        TaffyStyle contentStyle = blockStyle(Float.NaN, 10f);
        NodeId content = tree.newLeaf(contentStyle);
        NodeId root = tree.newWithChildren(rootStyle, left, right, content);

        compute(tree, root);

        assertEquals(0f, tree.getLayout(left).location().x, 0.01f);
        assertEquals(80f, tree.getLayout(right).location().x, 0.01f);
        assertEquals(0f, tree.getLayout(content).location().x, 0.01f);
        assertEquals(100f, tree.getLayout(content).size().width, 0.01f);
    }

    @Test
    void fixedWidthBlockBoxCanOverlapAFloat() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);

        TaffyStyle floatStyle = blockStyle(30f, 20f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);

        TaffyStyle followingStyle = blockStyle(80f, 10f);
        NodeId following = tree.newLeaf(followingStyle);
        NodeId root = tree.newWithChildren(rootStyle, floated, following);

        compute(tree, root);

        assertEquals(0f, tree.getLayout(following).location().x, 0.01f);
        assertEquals(0f, tree.getLayout(following).location().y, 0.01f);
    }

    @Test
    void rtlBlockBoxDoesNotShrinkForAFloatAtThePhysicalStartEdge() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);
        rootStyle.direction = TaffyDirection.RTL;

        TaffyStyle floatStyle = blockStyle(30f, 20f);
        floatStyle.floatMode = TaffyFloat.RIGHT;
        NodeId floated = tree.newLeaf(floatStyle);

        TaffyStyle followingStyle = blockStyle(Float.NaN, 10f);
        NodeId following = tree.newLeaf(followingStyle);
        NodeId root = tree.newWithChildren(rootStyle, floated, following);

        compute(tree, root);

        assertEquals(0f, tree.getLayout(following).location().x, 0.01f);
        assertEquals(100f, tree.getLayout(following).size().width, 0.01f);
    }

    @Test
    void independentFormattingContextAvoidsFloatFromPreviousSiblingSubtree() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);

        TaffyStyle floatStyle = blockStyle(50f, 100f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);
        NodeId floatSubtree = tree.newWithChildren(blockStyle(Float.NaN, Float.NaN), floated);

        TaffyStyle bfcStyle = blockStyle(Float.NaN, 50f);
        bfcStyle.overflow.x = Overflow.HIDDEN;
        bfcStyle.overflow.y = Overflow.HIDDEN;
        NodeId bfc = tree.newLeaf(bfcStyle);
        NodeId root = tree.newWithChildren(rootStyle, floatSubtree, bfc);

        compute(tree, root);

        assertEquals(50f, tree.getLayout(bfc).location().x, 0.01f);
        assertEquals(50f, tree.getLayout(bfc).size().width, 0.01f);
        assertEquals(100f, tree.getLayout(root).size().height, 0.01f);
    }

    @Test
    void sharedFloatContextPreservesRootContentBoxCoordinates() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(120f, Float.NaN);
        rootStyle.padding = new TaffyRect<>(
            LengthPercentage.length(10f),
            LengthPercentage.length(10f),
            LengthPercentage.ZERO,
            LengthPercentage.ZERO
        );

        TaffyStyle floatStyle = blockStyle(50f, 100f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);
        NodeId floatSubtree = tree.newWithChildren(blockStyle(Float.NaN, Float.NaN), floated);

        TaffyStyle bfcStyle = blockStyle(Float.NaN, 50f);
        bfcStyle.overflow.x = Overflow.HIDDEN;
        bfcStyle.overflow.y = Overflow.HIDDEN;
        NodeId bfc = tree.newLeaf(bfcStyle);
        NodeId root = tree.newWithChildren(rootStyle, floatSubtree, bfc);

        compute(tree, root);

        assertEquals(60f, tree.getLayout(bfc).location().x, 0.01f);
        assertEquals(50f, tree.getLayout(bfc).size().width, 0.01f);
    }

    @Test
    void sharedFloatContextPreservesNestedVerticalOffset() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);
        NodeId leading = tree.newLeaf(blockStyle(100f, 20f));

        TaffyStyle floatStyle = blockStyle(50f, 100f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);
        NodeId floatSubtree = tree.newWithChildren(blockStyle(Float.NaN, Float.NaN), floated);

        TaffyStyle bfcStyle = blockStyle(Float.NaN, 50f);
        bfcStyle.overflow.x = Overflow.HIDDEN;
        bfcStyle.overflow.y = Overflow.HIDDEN;
        NodeId bfc = tree.newLeaf(bfcStyle);
        NodeId root = tree.newWithChildren(rootStyle, leading, floatSubtree, bfc);

        compute(tree, root);

        assertEquals(50f, tree.getLayout(bfc).location().x, 0.01f);
        assertEquals(20f, tree.getLayout(bfc).location().y, 0.01f);
        assertEquals(120f, tree.getLayout(root).size().height, 0.01f);
    }

    @Test
    void independentFormattingContextMovesBelowFloatWhenNegativeMarginHidesItsBorderWidth() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);

        TaffyStyle floatStyle = blockStyle(50f, 100f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);

        TaffyStyle bfcStyle = blockStyle(Float.NaN, 50f);
        bfcStyle.overflow.x = Overflow.HIDDEN;
        bfcStyle.overflow.y = Overflow.HIDDEN;
        bfcStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.length(-60f),
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO
        );
        NodeId bfc = tree.newLeaf(bfcStyle);
        NodeId root = tree.newWithChildren(rootStyle, floated, bfc);

        compute(tree, root);

        assertEquals(-60f, tree.getLayout(bfc).location().x, 0.01f);
        assertEquals(100f, tree.getLayout(bfc).location().y, 0.01f);
        assertEquals(160f, tree.getLayout(bfc).size().width, 0.01f);
        assertEquals(150f, tree.getLayout(root).size().height, 0.01f);
    }

    @Test
    void independentFormattingContextWithFixedWidthMovesBelowFloat() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(300f, Float.NaN);

        TaffyStyle floatStyle = blockStyle(150f, 150f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);

        TaffyStyle bfcStyle = blockStyle(200f, 150f);
        bfcStyle.overflow.x = Overflow.HIDDEN;
        bfcStyle.overflow.y = Overflow.HIDDEN;
        NodeId bfc = tree.newLeaf(bfcStyle);
        NodeId root = tree.newWithChildren(rootStyle, floated, bfc);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(300f), AvailableSpace.maxContent()));

        assertEquals(0f, tree.getLayout(bfc).location().x, 0.01f);
        assertEquals(150f, tree.getLayout(bfc).location().y, 0.01f);
        assertEquals(300f, tree.getLayout(root).size().height, 0.01f);
    }

    @Test
    void independentFormattingContextAbsorbsLeadingMarginAtFloatEdge() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);

        TaffyStyle floatStyle = blockStyle(50f, 100f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);

        TaffyStyle bfcStyle = blockStyle(Float.NaN, 50f);
        bfcStyle.overflow.x = Overflow.HIDDEN;
        bfcStyle.overflow.y = Overflow.HIDDEN;
        bfcStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.length(20f),
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO
        );
        NodeId bfc = tree.newLeaf(bfcStyle);
        NodeId root = tree.newWithChildren(rootStyle, floated, bfc);

        compute(tree, root);

        assertEquals(50f, tree.getLayout(bfc).location().x, 0.01f);
        assertEquals(50f, tree.getLayout(bfc).size().width, 0.01f);
    }

    @Test
    void independentFormattingContextAppliesTrailingMarginToAutomaticWidth() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);

        TaffyStyle floatStyle = blockStyle(50f, 100f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);

        TaffyStyle bfcStyle = blockStyle(Float.NaN, 50f);
        bfcStyle.overflow.x = Overflow.HIDDEN;
        bfcStyle.overflow.y = Overflow.HIDDEN;
        bfcStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.length(10f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO
        );
        NodeId bfc = tree.newLeaf(bfcStyle);
        NodeId root = tree.newWithChildren(rootStyle, floated, bfc);

        compute(tree, root);

        assertEquals(50f, tree.getLayout(bfc).location().x, 0.01f);
        assertEquals(40f, tree.getLayout(bfc).size().width, 0.01f);
    }

    @Test
    void clearSeparatesFromFloatAdjoiningAnUnresolvedMarginStrut() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);

        TaffyStyle floatStyle = blockStyle(100f, 50f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);
        NodeId floatContainer = tree.newWithChildren(blockStyle(Float.NaN, Float.NaN), floated);

        TaffyStyle clearedStyle = blockStyle(Float.NaN, 50f);
        clearedStyle.clear = Clear.LEFT;
        clearedStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.length(400f),
            LengthPercentageAuto.ZERO
        );
        NodeId cleared = tree.newLeaf(clearedStyle);
        NodeId parent = tree.newWithChildren(blockStyle(Float.NaN, Float.NaN), floatContainer, cleared);
        NodeId root = tree.newWithChildren(rootStyle, parent);

        compute(tree, root);

        assertEquals(50f, tree.getLayout(cleared).location().y, 0.01f);
        assertEquals(100f, tree.getLayout(parent).size().height, 0.01f);
        assertEquals(100f, tree.getLayout(root).size().height, 0.01f);
    }

    @Test
    void selfCollapsingClearDoesNotPropagateMarginAfterAnAdjoiningFloat() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, Float.NaN);

        TaffyStyle floatStyle = blockStyle(100f, 50f);
        floatStyle.floatMode = TaffyFloat.LEFT;
        NodeId floated = tree.newLeaf(floatStyle);

        TaffyStyle clearStyle = blockStyle(Float.NaN, Float.NaN);
        clearStyle.clear = Clear.LEFT;
        clearStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.ZERO,
            LengthPercentageAuto.length(200f),
            LengthPercentageAuto.ZERO
        );
        NodeId cleared = tree.newLeaf(clearStyle);
        NodeId floatContainer = tree.newWithChildren(blockStyle(Float.NaN, Float.NaN), floated, cleared);
        NodeId following = tree.newLeaf(blockStyle(100f, 50f));
        NodeId parent = tree.newWithChildren(blockStyle(Float.NaN, Float.NaN), floatContainer, following);
        NodeId root = tree.newWithChildren(rootStyle, parent);

        compute(tree, root);

        assertEquals(50f, tree.getLayout(cleared).location().y, 0.01f);
        assertEquals(50f, tree.getLayout(floatContainer).size().height, 0.01f);
        assertEquals(50f, tree.getLayout(following).location().y, 0.01f);
        assertEquals(100f, tree.getLayout(root).size().height, 0.01f);
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

    private static void compute(TaffyTree tree, NodeId root) {
        tree.computeLayout(root, new TaffySize<>(
            AvailableSpace.definite(100f), AvailableSpace.maxContent()
        ));
    }
}
