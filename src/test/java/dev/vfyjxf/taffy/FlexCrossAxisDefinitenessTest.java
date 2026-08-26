package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlexCrossAxisDefinitenessTest {
    @Test
    void stretchedFlexItemProvidesADefiniteHeightForPercentageDescendants() {
        TaffyTree tree = new TaffyTree();
        NodeId root = newRoot(tree, AlignItems.STRETCH);
        NodeId flexItem = tree.getChildren(root).get(0);
        NodeId percentageChild = tree.getChildren(flexItem).get(0);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(100f, tree.getLayout(flexItem).size().height, 0.01f);
        assertEquals(50f, tree.getLayout(percentageChild).size().height, 0.01f);
    }

    @Test
    void unstretchedFlexItemDoesNotProvideADefiniteHeightForPercentageDescendants() {
        TaffyTree tree = new TaffyTree();
        NodeId root = newRoot(tree, AlignItems.FLEX_START);
        NodeId flexItem = tree.getChildren(root).get(0);
        NodeId percentageChild = tree.getChildren(flexItem).get(0);

        tree.computeLayout(root, TaffySize.maxContent());

        Layout flexItemLayout = tree.getLayout(flexItem);
        Layout percentageChildLayout = tree.getLayout(percentageChild);
        assertEquals(20f, flexItemLayout.size().height, 0.01f);
        assertEquals(20f, percentageChildLayout.size().height, 0.01f);
    }

    private NodeId newRoot(TaffyTree tree, AlignItems alignItems) {
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.FLEX;
        rootStyle.alignItems = alignItems;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(100f));

        TaffyStyle flexItemStyle = new TaffyStyle();
        flexItemStyle.display = TaffyDisplay.BLOCK;
        flexItemStyle.size = new TaffySize<>(TaffyDimension.length(50f), TaffyDimension.AUTO);

        TaffyStyle percentageChildStyle = new TaffyStyle();
        percentageChildStyle.display = TaffyDisplay.BLOCK;
        percentageChildStyle.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.percent(0.5f));

        TaffyStyle leafStyle = new TaffyStyle();
        leafStyle.display = TaffyDisplay.BLOCK;
        leafStyle.size = new TaffySize<>(TaffyDimension.length(20f), TaffyDimension.length(20f));

        NodeId leaf = tree.newLeaf(leafStyle);
        NodeId percentageChild = tree.newWithChildren(percentageChildStyle, leaf);
        NodeId flexItem = tree.newWithChildren(flexItemStyle, percentageChild);
        return tree.newWithChildren(rootStyle, flexItem);
    }
}
