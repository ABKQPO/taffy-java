package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.FlexWrap;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import dev.vfyjxf.taffy.util.MeasureFunc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlexIntrinsicSizingTest {
    @Test
    void maxContentCapsAnUnshrinkableFlexItemAtItsBasis() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.flexShrink = 0f;
        itemStyle.flexBasis = TaffyDimension.length(75f);
        NodeId item = tree.newLeafWithMeasure(itemStyle, ahemTextMeasure());
        NodeId root = tree.newWithChildren(flexStyle(), item);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(75f, tree.getLayout(root).size().width, 0.01f);
        assertEquals(20f, tree.getLayout(root).size().height, 0.01f);
        assertEquals(75f, tree.getLayout(item).size().width, 0.01f);
        assertEquals(20f, tree.getLayout(item).size().height, 0.01f);
    }

    @Test
    void minContentOfWrappedRowUsesItsLargestItemContribution() {
        TaffyTree tree = new TaffyTree();
        NodeId first = tree.newLeafWithMeasure(flexItem(75f, 1f), ahemTextMeasure());
        NodeId second = tree.newLeafWithMeasure(flexItem(150f, 1f), ahemTextMeasure());
        TaffyStyle rootStyle = flexStyle();
        rootStyle.flexWrap = FlexWrap.WRAP;
        NodeId root = tree.newWithChildren(rootStyle, first, second);

        tree.computeLayout(root, new TaffySize<>(AvailableSpace.minContent(), AvailableSpace.maxContent()));

        assertEquals(30f, tree.getLayout(root).size().width, 0.01f);
        assertEquals(80f, tree.getLayout(root).size().height, 0.01f);
        assertEquals(30f, tree.getLayout(first).size().width, 0.01f);
        assertEquals(40f, tree.getLayout(first).size().height, 0.01f);
        assertEquals(30f, tree.getLayout(second).size().width, 0.01f);
        assertEquals(40f, tree.getLayout(second).size().height, 0.01f);
        assertEquals(40f, tree.getLayout(second).location().y, 0.01f);
    }

    @Test
    void maxContentIncludesNegativeFlexItemMargins() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = flexItem(75f, 1f);
        itemStyle.margin = new TaffyRect<>(
            LengthPercentageAuto.length(-20f),
            LengthPercentageAuto.length(0f),
            LengthPercentageAuto.length(0f),
            LengthPercentageAuto.length(0f)
        );
        NodeId item = tree.newLeafWithMeasure(itemStyle, ahemTextMeasure());
        NodeId root = tree.newWithChildren(flexStyle(), item);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(80f, tree.getLayout(root).size().width, 0.01f);
        assertEquals(10f, tree.getLayout(root).size().height, 0.01f);
        assertEquals(-20f, tree.getLayout(item).location().x, 0.01f);
        assertEquals(100f, tree.getLayout(item).size().width, 0.01f);
        assertEquals(10f, tree.getLayout(item).size().height, 0.01f);
    }

    @Test
    void intrinsicRowSizeTransfersStretchedCrossSizeThroughAspectRatio() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle itemStyle = new TaffyStyle();
        itemStyle.aspectRatio = 2f;
        NodeId item = tree.newLeaf(itemStyle);

        TaffyStyle rootStyle = flexStyle();
        rootStyle.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(100f));
        NodeId root = tree.newWithChildren(rootStyle, item);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(200f, tree.getLayout(root).size().width, 0.01f);
        assertEquals(200f, tree.getLayout(item).size().width, 0.01f);
        assertEquals(100f, tree.getLayout(item).size().height, 0.01f);
    }

    private static TaffyStyle flexStyle() {
        TaffyStyle style = new TaffyStyle();
        style.display = TaffyDisplay.FLEX;
        return style;
    }

    private static TaffyStyle flexItem(float basis, float grow) {
        TaffyStyle style = new TaffyStyle();
        style.flexBasis = TaffyDimension.length(basis);
        style.flexGrow = grow;
        return style;
    }

    private static MeasureFunc ahemTextMeasure() {
        return (knownDimensions, availableSpace) -> {
            float width = !Float.isNaN(knownDimensions.width)
                ? knownDimensions.width
                : availableSpace.width.isDefinite() ? availableSpace.width.getValue()
                : availableSpace.width.isMinContent() ? 30f : 100f;
            float height = !Float.isNaN(knownDimensions.height)
                ? knownDimensions.height
                : width <= 30f ? 40f : width < 100f ? 20f : 10f;
            return new FloatSize(width, height);
        };
    }
}
