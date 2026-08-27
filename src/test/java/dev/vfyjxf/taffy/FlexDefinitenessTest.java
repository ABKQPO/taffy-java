package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import dev.vfyjxf.taffy.util.MeasureFunc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlexDefinitenessTest {
    @Test
    void percentChildOfDefiniteFlexBasisItemResolves() {
        TaffyTree tree = new TaffyTree();
        NodeId grandchild = tree.newLeaf(percentHeightBlock());
        TaffyStyle itemStyle = blockStyle();
        itemStyle.flexGrow = 1f;
        itemStyle.flexBasis = TaffyDimension.length(200f);
        NodeId item = tree.newWithChildren(itemStyle, grandchild);
        NodeId root = tree.newWithChildren(columnFlex(200f), item);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(200f, tree.getLayout(item).size().height, 0.01f);
        assertEquals(200f, tree.getLayout(grandchild).size().height, 0.01f);
    }

    @Test
    void percentChildOfIndefiniteFlexBasisItemRemainsAuto() {
        TaffyTree tree = new TaffyTree();
        NodeId grandchild = tree.newLeaf(percentHeightBlock());
        TaffyStyle itemStyle = blockStyle();
        itemStyle.flexGrow = 1f;
        NodeId item = tree.newWithChildren(itemStyle, grandchild);
        NodeId root = tree.newWithChildren(columnFlex(200f), item);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(200f, tree.getLayout(item).size().height, 0.01f);
        assertEquals(0f, tree.getLayout(grandchild).size().height, 0.01f);
    }

    @Test
    void indefinitenessPropagatesThroughNestedFlexContainer() {
        TaffyTree tree = new TaffyTree();
        NodeId grandchild = tree.newLeaf(percentHeightBlock());
        TaffyStyle nestedStyle = columnFlex(Float.NaN);
        nestedStyle.flexGrow = 1f;
        NodeId nested = tree.newWithChildren(nestedStyle, grandchild);
        NodeId root = tree.newWithChildren(columnFlex(200f), nested);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(200f, tree.getLayout(nested).size().height, 0.01f);
        assertEquals(0f, tree.getLayout(grandchild).size().height, 0.01f);
    }

    @Test
    void percentChildOfGrownItemInDefiniteContainerResolves() {
        TaffyTree tree = new TaffyTree();
        NodeId grandchild = tree.newLeaf(percentHeightBlock());
        TaffyStyle itemStyle = blockStyle();
        itemStyle.flexGrow = 1f;
        NodeId item = tree.newWithChildren(itemStyle, grandchild);
        TaffyStyle rootStyle = columnFlex(Float.NaN);
        rootStyle.size = new TaffySize<>(TaffyDimension.length(200f), TaffyDimension.length(200f));
        NodeId root = tree.newWithChildren(rootStyle, item);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(200f, tree.getLayout(item).size().height, 0.01f);
        assertEquals(200f, tree.getLayout(grandchild).size().height, 0.01f);
    }

    @Test
    void percentChildOfAspectRatioStretchedItemResolves() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle grandchildStyle = blockStyle();
        grandchildStyle.size = new TaffySize<>(TaffyDimension.length(20f), TaffyDimension.percent(1f));
        NodeId grandchild = tree.newLeaf(grandchildStyle);
        TaffyStyle itemStyle = blockStyle();
        itemStyle.aspectRatio = 1f;
        NodeId item = tree.newWithChildren(itemStyle, grandchild);
        TaffyStyle rootStyle = columnFlex(Float.NaN);
        rootStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.AUTO);
        NodeId root = tree.newWithChildren(rootStyle, item);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(100f, tree.getLayout(item).size().height, 0.01f);
        assertEquals(100f, tree.getLayout(grandchild).size().height, 0.01f);
    }

    @Test
    void percentHeightPropagatesThroughNestedGridContainer() {
        TaffyTree tree = new TaffyTree();
        NodeId grandchild = tree.newLeaf(percentHeightBlock());
        NodeId block = tree.newWithChildren(blockStyle(), grandchild);
        TaffyStyle gridStyle = new TaffyStyle();
        gridStyle.display = TaffyDisplay.GRID;
        gridStyle.flexGrow = 1f;
        gridStyle.gridTemplateColumns.add(TrackSizingFunction.fr(1f));
        gridStyle.gridTemplateRows.add(TrackSizingFunction.fr(1f));
        NodeId grid = tree.newWithChildren(gridStyle, block);
        NodeId root = tree.newWithChildren(columnFlex(200f), grid);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(200f, tree.getLayout(grid).size().height, 0.01f);
        assertEquals(200f, tree.getLayout(block).size().height, 0.01f);
        assertEquals(200f, tree.getLayout(grandchild).size().height, 0.01f);
    }

    @Test
    void wrapColumnWithDefiniteMainSizeResolvesPercentFlexBasis() {
        TaffyTree tree = new TaffyTree();
        NodeId firstText = tree.newLeafWithMeasure(textStyleWithPercentFlexBasis(), ahemTextMeasure());
        NodeId secondText = tree.newLeafWithMeasure(blockStyle(), ahemTextMeasure());
        NodeId inner = tree.newWithChildren(wrappingColumnFlex(), firstText, secondText);
        TaffyStyle outerStyle = columnFlex(Float.NaN);
        outerStyle.size = new TaffySize<>(TaffyDimension.length(300f), TaffyDimension.length(500f));
        NodeId outer = tree.newWithChildren(outerStyle, inner);

        tree.computeLayout(outer, TaffySize.maxContent());

        assertEquals(500f, tree.getLayout(outer).size().height, 0.01f);
        assertEquals(20f, tree.getLayout(inner).size().height, 0.01f);
        assertEquals(200f, tree.getLayout(firstText).size().width, 0.01f);
        assertEquals(20f, tree.getLayout(firstText).size().height, 0.01f);
        assertEquals(0f, tree.getLayout(firstText).location().x, 0.01f);
        assertEquals(200f, tree.getLayout(secondText).location().x, 0.01f);
        assertEquals(0f, tree.getLayout(secondText).location().y, 0.01f);
        assertEquals(200f, tree.getLayout(secondText).size().width, 0.01f);
        assertEquals(10f, tree.getLayout(secondText).size().height, 0.01f);
    }

    @Test
    void wrapColumnWithIndefiniteMainSizeKeepsPercentFlexBasisAuto() {
        TaffyTree tree = new TaffyTree();
        NodeId firstText = tree.newLeafWithMeasure(textStyleWithPercentFlexBasis(), ahemTextMeasure());
        NodeId secondText = tree.newLeafWithMeasure(blockStyle(), ahemTextMeasure());
        NodeId inner = tree.newWithChildren(wrappingColumnFlex(), firstText, secondText);
        TaffyStyle outerStyle = columnFlex(Float.NaN);
        outerStyle.size = new TaffySize<>(TaffyDimension.length(300f), TaffyDimension.AUTO);
        NodeId outer = tree.newWithChildren(outerStyle, inner);

        tree.computeLayout(outer, TaffySize.maxContent());

        assertEquals(20f, tree.getLayout(outer).size().height, 0.01f);
        assertEquals(300f, tree.getLayout(inner).size().width, 0.01f);
        assertEquals(20f, tree.getLayout(inner).size().height, 0.01f);
        assertEquals(300f, tree.getLayout(firstText).size().width, 0.01f);
        assertEquals(10f, tree.getLayout(firstText).size().height, 0.01f);
        assertEquals(0f, tree.getLayout(firstText).location().x, 0.01f);
        assertEquals(300f, tree.getLayout(secondText).size().width, 0.01f);
        assertEquals(10f, tree.getLayout(secondText).size().height, 0.01f);
        assertEquals(0f, tree.getLayout(secondText).location().x, 0.01f);
        assertEquals(10f, tree.getLayout(secondText).location().y, 0.01f);
    }

    private static TaffyStyle columnFlex(float minHeight) {
        TaffyStyle style = new TaffyStyle();
        style.display = TaffyDisplay.FLEX;
        style.flexDirection = FlexDirection.COLUMN;
        style.size = new TaffySize<>(TaffyDimension.length(200f), TaffyDimension.AUTO);
        style.minSize = new TaffySize<>(TaffyDimension.AUTO,
            Float.isNaN(minHeight) ? TaffyDimension.AUTO : TaffyDimension.length(minHeight));
        return style;
    }

    private static TaffyStyle blockStyle() {
        TaffyStyle style = new TaffyStyle();
        style.display = TaffyDisplay.BLOCK;
        return style;
    }

    private static TaffyStyle percentHeightBlock() {
        TaffyStyle style = blockStyle();
        style.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.percent(1f));
        return style;
    }

    private static TaffyStyle wrappingColumnFlex() {
        TaffyStyle style = columnFlex(Float.NaN);
        style.flexWrap = FlexWrap.WRAP;
        style.size = new TaffySize<>(TaffyDimension.length(300f), TaffyDimension.AUTO);
        return style;
    }

    private static TaffyStyle textStyleWithPercentFlexBasis() {
        TaffyStyle style = blockStyle();
        style.flexShrink = 0f;
        style.flexBasis = TaffyDimension.percent(1f);
        return style;
    }

    private static MeasureFunc ahemTextMeasure() {
        return (knownDimensions, availableSpace) -> {
            float width = !Float.isNaN(knownDimensions.width)
                ? knownDimensions.width
                : availableSpace.width.isMinContent() ? 100f : 200f;
            float height = !Float.isNaN(knownDimensions.height)
                ? knownDimensions.height
                : width <= 100f ? 20f : 10f;
            return new FloatSize(width, height);
        };
    }
}
