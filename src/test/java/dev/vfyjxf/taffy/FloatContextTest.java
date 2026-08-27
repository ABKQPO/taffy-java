package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.Clear;
import dev.vfyjxf.taffy.style.FloatDirection;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyFloat;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.BfcSlot;
import dev.vfyjxf.taffy.tree.ContentSlot;
import dev.vfyjxf.taffy.tree.FloatContext;
import dev.vfyjxf.taffy.tree.FloatIntrinsicWidthCalculator;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatContextTest {
    @Test
    void placesFloatsAndFindsContentSlot() {
        FloatContext context = new FloatContext(100f);
        FloatPoint left = context.placeFloatedBox(
            new FloatSize(30f, 20f), 0f, new float[] {0f, 0f}, FloatDirection.LEFT, Clear.NONE);
        assertEquals(0f, left.x, 0.001f);
        assertEquals(0f, left.y, 0.001f);

        ContentSlot slot = context.findContentSlot(0f, new float[] {0f, 0f}, Clear.NONE, null);
        assertEquals(30f, slot.x, 0.001f);
        assertEquals(70f, slot.width, 0.001f);

        ContentSlot cleared = context.findContentSlot(0f, new float[] {0f, 0f}, Clear.LEFT, null);
        assertEquals(20f, cleared.y, 0.001f);
        assertEquals(100f, cleared.width, 0.001f);
    }

    @Test
    void bfcSlotAccountsForMarginsAndDirection() {
        FloatContext context = new FloatContext(100f);
        context.placeFloatedBox(
            new FloatSize(40f, 20f), 0f, new float[] {0f, 0f}, FloatDirection.LEFT, Clear.NONE);

        BfcSlot slot = context.findBfcSlot(
            0f, new float[] {0f, 0f}, new float[] {5f, 5f}, TaffyDirection.LTR, Clear.NONE, null);
        assertEquals(40f, slot.x, 0.001f);
        assertEquals(60f, slot.borderWidth, 0.001f);

        FloatContext rtlContext = new FloatContext(100f);
        rtlContext.placeFloatedBox(
            new FloatSize(40f, 20f), 0f, new float[] {0f, 0f}, FloatDirection.RIGHT, Clear.NONE);
        BfcSlot rtlSlot = rtlContext.findBfcSlot(
            0f, new float[] {0f, 0f}, new float[] {5f, 5f}, TaffyDirection.RTL, Clear.NONE, null);
        assertEquals(0f, rtlSlot.x, 0.001f);
        assertEquals(60f, rtlSlot.borderWidth, 0.001f);
    }

    @Test
    void laterFloatDoesNotMoveAboveEarlierFloat() {
        FloatContext context = new FloatContext(100f);
        context.placeFloatedBox(
            new FloatSize(10f, 10f), 20f, new float[] {0f, 0f}, FloatDirection.LEFT, Clear.NONE);
        FloatPoint later = context.placeFloatedBox(
            new FloatSize(10f, 10f), 0f, new float[] {0f, 0f}, FloatDirection.RIGHT, Clear.NONE);
        assertEquals(20f, later.y, 0.001f);
    }

    @Test
    void computesIntrinsicFloatWidth() {
        FloatIntrinsicWidthCalculator calculator =
            new FloatIntrinsicWidthCalculator(AvailableSpace.definite(45f));
        calculator.addFloat(30f, FloatDirection.LEFT, Clear.NONE);
        calculator.addFloat(25f, FloatDirection.RIGHT, Clear.NONE);
        assertEquals(45f, calculator.result(), 0.001f);

        FloatIntrinsicWidthCalculator minContent =
            new FloatIntrinsicWidthCalculator(AvailableSpace.minContent());
        minContent.addFloat(30f, FloatDirection.LEFT, Clear.NONE);
        minContent.addFloat(25f, FloatDirection.RIGHT, Clear.NONE);
        assertEquals(30f, minContent.result(), 0.001f);
    }

    @Test
    void blockIntrinsicWidthAccumulatesAdjacentFloats() {
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.BLOCK;

        TaffyStyle leftStyle = new TaffyStyle();
        leftStyle.floatMode = TaffyFloat.LEFT;
        leftStyle.size = new TaffySize<>(
            TaffyDimension.length(30f), TaffyDimension.length(10f));
        TaffyStyle rightStyle = new TaffyStyle();
        rightStyle.floatMode = TaffyFloat.RIGHT;
        rightStyle.size = new TaffySize<>(
            TaffyDimension.length(25f), TaffyDimension.length(10f));

        TaffyTree tree = new TaffyTree();
        NodeId left = tree.newLeaf(leftStyle);
        NodeId right = tree.newLeaf(rightStyle);
        NodeId root = tree.newWithChildren(rootStyle, left, right);
        tree.computeLayout(root, new TaffySize<>(
            AvailableSpace.maxContent(), AvailableSpace.maxContent()));

        assertEquals(55f, tree.getLayout(root).size().width, 0.001f);
    }
}
