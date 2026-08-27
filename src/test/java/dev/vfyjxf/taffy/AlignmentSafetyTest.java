package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlignmentSafetyTest {
    @Test
    public void safeFlexCenterFallsBackWhenItemOverflows() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.FLEX;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(50f));
        rootStyle.alignItems = AlignItems.SAFE_CENTER;
        NodeId child = tree.newLeaf(new TaffyStyle());
        TaffyStyle childStyle = tree.getStyle(child);
        childStyle.size = new TaffySize<>(TaffyDimension.length(20f), TaffyDimension.length(100f));
        NodeId root = tree.newWithChildren(rootStyle, child);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(100f), AvailableSpace.definite(50f)));

        assertEquals(0f, tree.getLayout(child).location().y, 0.001f);
    }

    @Test
    public void unsafeFlexCenterKeepsOverflowingCenterOffset() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.FLEX;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(50f));
        rootStyle.alignItems = AlignItems.CENTER;
        NodeId child = tree.newLeaf(new TaffyStyle());
        TaffyStyle childStyle = tree.getStyle(child);
        childStyle.size = new TaffySize<>(TaffyDimension.length(20f), TaffyDimension.length(100f));
        NodeId root = tree.newWithChildren(rootStyle, child);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(100f), AvailableSpace.definite(50f)));

        assertEquals(-25f, tree.getLayout(child).location().y, 0.001f);
    }
}
