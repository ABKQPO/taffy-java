package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LengthPercentageAutoConstraintTest {
    @Test
    void percentageMinimumUsesContainingBlockWidth() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle rootStyle = blockStyle(100f, 40f);
        TaffyStyle childStyle = blockStyle(20f, 10f);
        childStyle.setMinSize(new TaffySize<>(LengthPercentageAuto.percent(0.5f), LengthPercentageAuto.AUTO));

        NodeId child = tree.newLeaf(childStyle);
        NodeId root = tree.newWithChildren(rootStyle, child);
        tree.computeLayout(root, new TaffySize<>(
            AvailableSpace.definite(100f), AvailableSpace.definite(40f)
        ));

        assertEquals(50f, tree.getLayout(child).size().width, 0.01f);
    }

    @Test
    void legacyDimensionAssignmentsRemainSupported() {
        TaffyStyle style = new TaffyStyle();
        style.minSize = new TaffySize<>(TaffyDimension.length(12f), TaffyDimension.AUTO);
        assertEquals(12f, style.getMinSize().width.maybeResolve(100f), 0.01f);
        assertEquals(LengthPercentageAuto.Type.AUTO, style.getMinSize().height.getType());
    }

    private static TaffyStyle blockStyle(float width, float height) {
        TaffyStyle style = new TaffyStyle();
        style.display = TaffyDisplay.BLOCK;
        style.size = new TaffySize<>(
            Float.isNaN(width) ? TaffyDimension.AUTO : TaffyDimension.length(width),
            Float.isNaN(height) ? TaffyDimension.AUTO : TaffyDimension.length(height)
        );
        return style;
    }
}
