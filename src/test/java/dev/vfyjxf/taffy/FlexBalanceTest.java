package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.FlexWrap;
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
}
