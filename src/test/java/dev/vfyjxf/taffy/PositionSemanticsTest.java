package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.LayoutComputer;
import dev.vfyjxf.taffy.tree.LayoutOutput;
import dev.vfyjxf.taffy.tree.SizingMode;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PositionSemanticsTest {
    @Test
    void staticItemsIgnoreInsetWhileRelativeItemsApplyIt() {
        TaffyStyle staticStyle = itemStyle();
        TaffyStyle relativeStyle = itemStyle();
        relativeStyle.position = TaffyPosition.RELATIVE;

        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.flexDirection = FlexDirection.COLUMN;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(100f));

        TaffyTree tree = new TaffyTree();
        NodeId staticItem = tree.newLeaf(staticStyle);
        NodeId relativeItem = tree.newLeaf(relativeStyle);
        NodeId root = tree.newWithChildren(rootStyle, staticItem, relativeItem);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(0f, tree.getLayout(staticItem).location().y, 0.01f);
        assertEquals(20f, tree.getLayout(relativeItem).location().y, 0.01f);
    }

    @Test
    void positionCategoriesExposeContainingBlockAndFlowRules() {
        assertEquals(TaffyPosition.STATIC, new TaffyStyle().position);
        assertFalse(TaffyPosition.STATIC.isPositioned());
        assertTrue(TaffyPosition.RELATIVE.isPositioned());
        assertTrue(TaffyPosition.ABSOLUTE.isOutOfFlow());
        assertTrue(TaffyPosition.FIXED.isOutOfFlow());
        assertFalse(TaffyPosition.RELATIVE.isOutOfFlow());
    }

    @Test
    void fixedDescendantUsesTheRootContainingBlock() {
        TaffyStyle fixedStyle = sizedStyle(10f, 10f);
        fixedStyle.position = TaffyPosition.FIXED;
        fixedStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.AUTO);

        TaffyStyle parentStyle = sizedStyle(50f, 50f);
        parentStyle.position = TaffyPosition.RELATIVE;
        parentStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.length(50f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(50f),
            LengthPercentageAuto.AUTO);

        TaffyTree tree = new TaffyTree();
        NodeId fixedItem = tree.newLeaf(fixedStyle);
        NodeId parent = tree.newWithChildren(parentStyle, fixedItem);
        NodeId root = tree.newWithChildren(sizedStyle(200f, 200f), parent);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(5f, accumulatedY(tree, fixedItem), 0.01f);
    }

    @Test
    void absoluteDescendantSkipsItsStaticParentForExplicitInsets() {
        TaffyStyle absoluteStyle = sizedStyle(10f, 10f);
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(7f),
            LengthPercentageAuto.AUTO);

        TaffyStyle staticParentStyle = sizedStyle(30f, 30f);
        staticParentStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.length(40f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(40f),
            LengthPercentageAuto.AUTO);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(staticParentStyle, absolute);
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(5f, accumulatedX(tree, absolute), 0.01f);
        assertEquals(7f, accumulatedY(tree, absolute), 0.01f);
    }

    @Test
    void staticAncestorsBubbleOutOfFlowCandidatesToTheirContainingBlock() {
        TaffyStyle absoluteStyle = sizedStyle(10f, 10f);
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        NodeId absolute;
        TaffyTree tree = new TaffyTree();
        absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(40f, 40f), absolute);
        NodeId root = tree.newWithChildren(sizedStyle(100f, 100f), staticParent);

        LayoutComputer computer = new LayoutComputer(tree, null);
        LayoutOutput output = computer.performChildLayout(
            root,
            new FloatSize(Float.NaN, Float.NaN),
            new FloatSize(Float.NaN, Float.NaN),
            TaffySize.maxContent(),
            SizingMode.INHERENT_SIZE,
            new TaffyLine<>(false, false)
        );

        assertEquals(1, output.oofCandidates().size());
        assertEquals(absolute, output.oofCandidates().get(0).node());

        LayoutOutput cachedOutput = computer.performChildLayout(
            root,
            new FloatSize(Float.NaN, Float.NaN),
            new FloatSize(Float.NaN, Float.NaN),
            TaffySize.maxContent(),
            SizingMode.INHERENT_SIZE,
            new TaffyLine<>(false, false)
        );
        assertEquals(output.oofCandidates(), cachedOutput.oofCandidates());
    }

    private static TaffyStyle itemStyle() {
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(10f));
        style.inset = new TaffyRect<>(
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(10f),
            LengthPercentageAuto.AUTO);
        return style;
    }

    private static TaffyStyle sizedStyle(float width, float height) {
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(TaffyDimension.length(width), TaffyDimension.length(height));
        return style;
    }

    private static float accumulatedY(TaffyTree tree, NodeId node) {
        float y = 0f;
        NodeId current = node;
        while (current != null) {
            y += tree.getLayout(current).location().y;
            current = tree.getParent(current);
        }
        return y;
    }

    private static float accumulatedX(TaffyTree tree, NodeId node) {
        float x = 0f;
        NodeId current = node;
        while (current != null) {
            x += tree.getLayout(current).location().x;
            current = tree.getParent(current);
        }
        return x;
    }
}
