package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlexContainerBaselineTest {

    @Test
    @DisplayName("Column-reverse container baseline comes from its visual start item")
    void columnReverseContainerBaselineComesFromVisualStartItem() {
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.FLEX;
        rootStyle.alignItems = AlignItems.BASELINE;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(200.0f), TaffyDimension.length(200.0f));

        TaffyStyle siblingStyle = new TaffyStyle();
        siblingStyle.size = new TaffySize<>(TaffyDimension.length(50.0f), TaffyDimension.length(100.0f));

        TaffyStyle nestedStyle = new TaffyStyle();
        nestedStyle.display = TaffyDisplay.FLEX;
        nestedStyle.flexDirection = FlexDirection.COLUMN_REVERSE;
        nestedStyle.size = new TaffySize<>(TaffyDimension.length(60.0f), TaffyDimension.length(90.0f));

        TaffyStyle firstNestedChildStyle = new TaffyStyle();
        firstNestedChildStyle.size = new TaffySize<>(TaffyDimension.length(60.0f), TaffyDimension.length(20.0f));
        TaffyStyle secondNestedChildStyle = new TaffyStyle();
        secondNestedChildStyle.size = new TaffySize<>(TaffyDimension.length(60.0f), TaffyDimension.length(30.0f));

        TaffyTree tree = new TaffyTree();
        NodeId sibling = tree.newLeaf(siblingStyle);
        NodeId firstNestedChild = tree.newLeaf(firstNestedChildStyle);
        NodeId secondNestedChild = tree.newLeaf(secondNestedChildStyle);
        NodeId nested = tree.newWithChildren(nestedStyle, firstNestedChild, secondNestedChild);
        NodeId root = tree.newWithChildren(rootStyle, sibling, nested);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(200.0f), AvailableSpace.definite(200.0f)));

        Layout nestedLayout = tree.getLayout(nested);
        assertEquals(50.0f, nestedLayout.location().x, 0.01f);
        assertEquals(30.0f, nestedLayout.location().y, 0.01f);
        assertEquals(70.0f, tree.getLayout(firstNestedChild).location().y, 0.01f);
        assertEquals(40.0f, tree.getLayout(secondNestedChild).location().y, 0.01f);
    }
}
