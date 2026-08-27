package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.LayoutInput;
import dev.vfyjxf.taffy.tree.LayoutOutput;
import dev.vfyjxf.taffy.tree.LayoutCache;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.ClearState;
import dev.vfyjxf.taffy.tree.NodeData;
import dev.vfyjxf.taffy.tree.RequestedAxis;
import dev.vfyjxf.taffy.tree.RunMode;
import dev.vfyjxf.taffy.tree.SizingMode;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LayoutCacheParityTest {
    @Test
    void clearingCacheAndMarkingNodeDirtyReportNamedClearStates() {
        LayoutCache cache = new LayoutCache();
        LayoutInput input = input(100f);
        cache.store(input, LayoutOutput.fromOuterSize(new FloatSize(20f, 10f)));

        assertEquals(ClearState.CLEARED, cache.clear());
        assertEquals(ClearState.ALREADY_EMPTY, cache.clear());

        NodeData node = new NodeData(new TaffyStyle());
        node.getCache().store(input, LayoutOutput.fromOuterSize(new FloatSize(20f, 10f)));
        assertEquals(ClearState.CLEARED, node.markDirty());
        assertEquals(ClearState.ALREADY_EMPTY, node.markDirty());
    }

    @Test
    void percentageSizeMeasurementDoesNotReuseAResultForAnotherParentWidth() {
        TaffyTree tree = new TaffyTree();
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(TaffyDimension.percent(0.5f), TaffyDimension.AUTO);
        NodeId node = tree.newLeaf(style);

        LayoutOutput first = tree.computeChildLayout(node, input(100f));
        LayoutOutput second = tree.computeChildLayout(node, input(200f));

        assertEquals(50f, first.size().width, 0.01f);
        assertEquals(100f, second.size().width, 0.01f);
    }

    private LayoutInput input(float parentWidth) {
        return new LayoutInput(
            RunMode.COMPUTE_SIZE,
            SizingMode.INHERENT_SIZE,
            RequestedAxis.BOTH,
            FloatSize.none(),
            new TaffySize<>(false, false),
            new FloatSize(parentWidth, Float.NaN),
            new TaffySize<>(AvailableSpace.MAX_CONTENT, AvailableSpace.MAX_CONTENT),
            TaffyLine.FALSE
        );
    }
}
