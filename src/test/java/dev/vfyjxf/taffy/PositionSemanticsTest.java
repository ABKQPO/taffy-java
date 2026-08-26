package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
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
}
