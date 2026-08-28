package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlexContentSizeConstraintTest {

    @Test
    void finalFlexLayoutKeepsMinSizeWhenItExceedsMaxSize() {
        TaffyStyle childStyle = new TaffyStyle();
        childStyle.minSize = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.AUTO);
        childStyle.maxSize = new TaffySize<>(TaffyDimension.length(50f), TaffyDimension.AUTO);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.display = TaffyDisplay.FLEX;

        TaffyTree tree = new TaffyTree();
        NodeId child = tree.newLeaf(childStyle);
        NodeId container = tree.newWithChildren(containerStyle, child);
        tree.computeLayout(container, TaffySize.maxContent());

        assertEquals(100f, tree.getLayout(child).size().width, 0.001f);
    }

    @Test
    void columnStretchKeepsAnExplicitMainSizeWithAspectRatio() {
        TaffyStyle childStyle = new TaffyStyle();
        childStyle.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(40f));
        childStyle.aspectRatio = 2f;

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.COLUMN;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(100f));

        TaffyTree tree = new TaffyTree();
        NodeId child = tree.newLeaf(childStyle);
        NodeId container = tree.newWithChildren(containerStyle, child);
        tree.computeLayout(container, TaffySize.maxContent());

        assertEquals(100f, tree.getLayout(child).size().width, 0.001f);
        assertEquals(40f, tree.getLayout(child).size().height, 0.001f);
    }
}
