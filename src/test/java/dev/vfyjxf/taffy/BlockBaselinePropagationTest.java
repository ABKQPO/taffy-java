package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockBaselinePropagationTest {
    @Test
    void blockContainerPropagatesItsFirstFlexDescendantBaseline() {
        TaffyStyle leafStyle = sizedStyle(50f, 10f);
        TaffyStyle flexStyle = sizedStyle(50f, 20f);
        flexStyle.display = TaffyDisplay.FLEX;
        TaffyStyle blockStyle = sizedStyle(100f, 100f);
        blockStyle.display = TaffyDisplay.BLOCK;

        TaffyTree tree = new TaffyTree();
        NodeId leaf = tree.newLeaf(leafStyle);
        NodeId flex = tree.newWithChildren(flexStyle, leaf);
        NodeId root = tree.newWithChildren(blockStyle, flex);
        tree.computeLayout(root, TaffySize.maxContent());

        assertTrue(tree.getLayout(root).baselines().hasFirst());
    }

    private static TaffyStyle sizedStyle(float width, float height) {
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(TaffyDimension.length(width), TaffyDimension.length(height));
        return style;
    }
}
