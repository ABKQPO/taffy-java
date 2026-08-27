package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.BoxSizing;
import dev.vfyjxf.taffy.style.CalcExpression;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.OutOfFlowPositioner;
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
    void outOfFlowPositionerRepositionsNestedAbsoluteDescendants() {
        TaffyStyle absoluteStyle = sizedStyle(10f, 10f);
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(7f),
            LengthPercentageAuto.AUTO);

        TaffyTree tree = new TaffyTree();
        tree.disableRounding();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        tree.getUnroundedLayout(absolute).location().x = 33f;
        tree.getUnroundedLayout(absolute).location().y = 44f;
        new OutOfFlowPositioner().reposition(tree, containingBlock);

        assertEquals(5f, accumulatedX(tree, absolute), 0.01f);
        assertEquals(7f, accumulatedY(tree, absolute), 0.01f);
    }

    @Test
    void absoluteDescendantResolvesEndInsetsAgainstItsPositionedAncestor() {
        TaffyStyle absoluteStyle = sizedStyle(10f, 20f);
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.length(7f));

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

        assertEquals(85f, accumulatedX(tree, absolute), 0.01f);
        assertEquals(73f, accumulatedY(tree, absolute), 0.01f);
    }

    @Test
    void absoluteDescendantStretchesBetweenInsetsInItsPositionedAncestor() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(15f),
            LengthPercentageAuto.length(7f),
            LengthPercentageAuto.length(13f));

        TaffyStyle staticParentStyle = sizedStyle(30f, 30f);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(staticParentStyle, absolute);
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(80f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(80f, tree.getLayout(absolute).size().height, 0.01f);
    }

    @Test
    void absoluteDescendantAppliesAspectRatioAfterInsetDerivedWidth() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.aspectRatio = 2f;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(5f));

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(90f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(45f, tree.getLayout(absolute).size().height, 0.01f);
    }

    @Test
    void absoluteDescendantUsesEndInsetForOverconstrainedRtlContainingBlock() {
        TaffyStyle absoluteStyle = sizedStyle(20f, 10f);
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(15f),
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO);

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        containingBlockStyle.direction = TaffyDirection.RTL;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(65f, accumulatedX(tree, absolute), 0.01f);
    }

    @Test
    void absoluteDescendantResolvesPercentageSizeAgainstItsPositionedAncestor() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.size = new TaffySize<>(TaffyDimension.percent(0.5f), TaffyDimension.percent(0.4f));

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(50f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(40f, tree.getLayout(absolute).size().height, 0.01f);
    }

    @Test
    void absoluteDescendantAddsPaddingAndBorderToContentBoxPercentageSize() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.boxSizing = BoxSizing.CONTENT_BOX;
        absoluteStyle.size = new TaffySize<>(TaffyDimension.percent(0.5f), TaffyDimension.percent(0.5f));
        absoluteStyle.padding = new TaffyRect<>(
            LengthPercentage.length(2f),
            LengthPercentage.length(3f),
            LengthPercentage.length(4f),
            LengthPercentage.length(1f));
        absoluteStyle.border = new TaffyRect<>(
            LengthPercentage.length(4f),
            LengthPercentage.length(1f),
            LengthPercentage.length(2f),
            LengthPercentage.length(3f));

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(60f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(60f, tree.getLayout(absolute).size().height, 0.01f);
    }

    @Test
    void absoluteDescendantAddsPaddingAndBorderToContentBoxMinimumSize() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.boxSizing = BoxSizing.CONTENT_BOX;
        absoluteStyle.minSize = new TaffySize<>(LengthPercentageAuto.length(50f), LengthPercentageAuto.length(50f));
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(30f),
            LengthPercentageAuto.length(30f),
            LengthPercentageAuto.length(30f),
            LengthPercentageAuto.length(30f));
        absoluteStyle.padding = new TaffyRect<>(
            LengthPercentage.length(2f),
            LengthPercentage.length(3f),
            LengthPercentage.length(4f),
            LengthPercentage.length(1f));
        absoluteStyle.border = new TaffyRect<>(
            LengthPercentage.length(4f),
            LengthPercentage.length(1f),
            LengthPercentage.length(2f),
            LengthPercentage.length(3f));

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(60f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(60f, tree.getLayout(absolute).size().height, 0.01f);
    }

    @Test
    void absoluteDescendantResolvesCalcSizeAgainstItsPositionedAncestor() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.size = new TaffySize<>(
            TaffyDimension.calc(CalcExpression.percentPlusLength(0.5f, 10f)),
            TaffyDimension.length(10f));

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(60f, tree.getLayout(absolute).size().width, 0.01f);
    }

    @Test
    void absoluteStretchUsesContainingBlockAcrossLayoutAlgorithms() {
        TaffyDisplay[] displays = {TaffyDisplay.BLOCK, TaffyDisplay.FLEX, TaffyDisplay.GRID};
        for (TaffyDisplay display : displays) {
            TaffyStyle absoluteStyle = new TaffyStyle();
            absoluteStyle.position = TaffyPosition.ABSOLUTE;
            absoluteStyle.size = new TaffySize<>(TaffyDimension.stretch(), TaffyDimension.stretch());
            absoluteStyle.margin = new TaffyRect<>(
                LengthPercentageAuto.length(3f),
                LengthPercentageAuto.length(4f),
                LengthPercentageAuto.length(5f),
                LengthPercentageAuto.length(6f));

            TaffyStyle rootStyle = sizedStyle(100f, 80f);
            rootStyle.display = display;
            rootStyle.position = TaffyPosition.RELATIVE;
            TaffyTree tree = new TaffyTree();
            NodeId absolute = tree.newLeaf(absoluteStyle);
            NodeId root = tree.newWithChildren(rootStyle, absolute);
            tree.computeLayout(root, TaffySize.maxContent());

            assertEquals(93f, tree.getLayout(absolute).size().width, 0.01f, display.toString());
            assertEquals(69f, tree.getLayout(absolute).size().height, 0.01f, display.toString());
        }
    }

    @Test
    void nestedAbsoluteStretchUsesItsPositionedAncestor() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.size = new TaffySize<>(TaffyDimension.stretch(), TaffyDimension.stretch());
        absoluteStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.length(3f),
            LengthPercentageAuto.length(4f),
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(6f));

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 80f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(93f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(69f, tree.getLayout(absolute).size().height, 0.01f);
    }

    @Test
    void absoluteIntrinsicKeywordsUseMeasurementConstraintsAcrossLayoutAlgorithms() {
        TaffyDisplay[] displays = {TaffyDisplay.BLOCK, TaffyDisplay.FLEX, TaffyDisplay.GRID};
        for (TaffyDisplay display : displays) {
            TaffyStyle absoluteStyle = new TaffyStyle();
            absoluteStyle.position = TaffyPosition.ABSOLUTE;
            absoluteStyle.size = new TaffySize<>(TaffyDimension.minContent(), TaffyDimension.maxContent());

            TaffyStyle rootStyle = sizedStyle(100f, 80f);
            rootStyle.display = display;
            rootStyle.position = TaffyPosition.RELATIVE;
            TaffyTree tree = new TaffyTree();
            NodeId absolute = tree.newLeafWithMeasure(absoluteStyle, (knownDimensions, availableSpace) -> new FloatSize(
                availableSpace.width.isMinContent() ? 30f : availableSpace.width.isMaxContent() ? 70f : 10f,
                availableSpace.height.isMaxContent() ? 50f : availableSpace.height.isMinContent() ? 20f : 10f
            ));
            NodeId root = tree.newWithChildren(rootStyle, absolute);
            tree.computeLayout(root, TaffySize.maxContent());

            assertEquals(30f, tree.getLayout(absolute).size().width, 0.01f, display.toString());
            assertEquals(50f, tree.getLayout(absolute).size().height, 0.01f, display.toString());
        }
    }

    @Test
    void absoluteFitContentUsesItsLimitAndStretchConstraint() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.size = new TaffySize<>(
            TaffyDimension.fitContent(LengthPercentage.length(40f)),
            TaffyDimension.fitContent());

        TaffyStyle rootStyle = sizedStyle(100f, 80f);
        rootStyle.position = TaffyPosition.RELATIVE;
        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeafWithMeasure(absoluteStyle, (knownDimensions, availableSpace) -> new FloatSize(
            availableSpace.width.isDefinite() ? availableSpace.width.getValue() : 0f,
            availableSpace.height.isDefinite() ? availableSpace.height.getValue() : 0f
        ));
        NodeId root = tree.newWithChildren(rootStyle, absolute);
        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(40f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(80f, tree.getLayout(absolute).size().height, 0.01f);
    }

    @Test
    void nestedAbsoluteFitContentUsesItsPositionedAncestorConstraint() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.size = new TaffySize<>(TaffyDimension.fitContent(), TaffyDimension.fitContent());

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeafWithMeasure(absoluteStyle, (knownDimensions, availableSpace) -> new FloatSize(
            availableSpace.width.isDefinite() ? availableSpace.width.getValue() : 0f,
            availableSpace.height.isDefinite() ? availableSpace.height.getValue() : 0f
        ));
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 80f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(100f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(80f, tree.getLayout(absolute).size().height, 0.01f);
    }

    @Test
    void absoluteDescendantClampsInsetDerivedSizeAgainstItsPositionedAncestor() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(5f));
        absoluteStyle.maxSize = new TaffySize<>(TaffyDimension.length(60f), TaffyDimension.length(70f));

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(60f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(70f, tree.getLayout(absolute).size().height, 0.01f);
    }

    @Test
    void absoluteDescendantAppliesMarginsInItsPositionedAncestor() {
        TaffyStyle absoluteStyle = sizedStyle(10f, 10f);
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(7f),
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(9f));
        absoluteStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.length(3f),
            LengthPercentageAuto.length(4f),
            LengthPercentageAuto.length(2f),
            LengthPercentageAuto.length(6f));

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(8f, accumulatedX(tree, absolute), 0.01f);
        assertEquals(7f, accumulatedY(tree, absolute), 0.01f);
    }

    @Test
    void absoluteDescendantSubtractsFixedMarginsFromInsetDerivedSize() {
        TaffyStyle absoluteStyle = new TaffyStyle();
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(5f),
            LengthPercentageAuto.length(5f));
        absoluteStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.length(3f),
            LengthPercentageAuto.length(4f),
            LengthPercentageAuto.length(2f),
            LengthPercentageAuto.length(6f));

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(83f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(82f, tree.getLayout(absolute).size().height, 0.01f);
    }

    @Test
    void absoluteDescendantDistributesAutoMarginsInItsPositionedAncestor() {
        TaffyStyle absoluteStyle = sizedStyle(20f, 20f);
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(10f),
            LengthPercentageAuto.length(10f),
            LengthPercentageAuto.length(10f),
            LengthPercentageAuto.length(10f));
        absoluteStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO);

        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(30f, 30f), absolute);
        TaffyStyle containingBlockStyle = sizedStyle(100f, 100f);
        containingBlockStyle.position = TaffyPosition.RELATIVE;
        NodeId containingBlock = tree.newWithChildren(containingBlockStyle, staticParent);
        tree.computeLayout(containingBlock, TaffySize.maxContent());

        assertEquals(40f, accumulatedX(tree, absolute), 0.01f);
        assertEquals(40f, accumulatedY(tree, absolute), 0.01f);
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

    @Test
    void rootLayoutOutputContainsBubbledOutOfFlowCandidates() {
        TaffyStyle absoluteStyle = sizedStyle(10f, 10f);
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId staticParent = tree.newWithChildren(sizedStyle(40f, 40f), absolute);
        NodeId root = tree.newWithChildren(sizedStyle(100f, 100f), staticParent);

        LayoutOutput output = new LayoutComputer(tree, null).computeLayoutWithOutput(root, TaffySize.maxContent());

        assertEquals(1, output.oofCandidates().size());
        assertEquals(absolute, output.oofCandidates().get(0).node());
    }

    @Test
    void absoluteAutoMarginsCenterUsingMaxWidthClampedSize() {
        TaffyStyle absoluteStyle = sizedStyle(180f, 40f);
        absoluteStyle.position = TaffyPosition.ABSOLUTE;
        absoluteStyle.maxSize = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.AUTO);
        absoluteStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO,
            LengthPercentageAuto.AUTO
        );
        absoluteStyle.inset = new TaffyRect<>(
            LengthPercentageAuto.length(0f),
            LengthPercentageAuto.length(0f),
            LengthPercentageAuto.length(20f),
            LengthPercentageAuto.AUTO
        );

        TaffyStyle rootStyle = sizedStyle(200f, 100f);
        rootStyle.display = TaffyDisplay.BLOCK;
        TaffyTree tree = new TaffyTree();
        NodeId absolute = tree.newLeaf(absoluteStyle);
        NodeId root = tree.newWithChildren(rootStyle, absolute);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(100f, tree.getLayout(absolute).size().width, 0.01f);
        assertEquals(50f, tree.getLayout(absolute).location().x, 0.01f);
        assertEquals(20f, tree.getLayout(absolute).location().y, 0.01f);
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
