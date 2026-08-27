package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.DetailedLayoutInfo;
import dev.vfyjxf.taffy.tree.Baselines;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.LayoutAlgorithms;
import dev.vfyjxf.taffy.tree.LayoutOutput;
import dev.vfyjxf.taffy.tree.LayoutPartialTree;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.RequestedAxis;
import dev.vfyjxf.taffy.tree.RoundTree;
import dev.vfyjxf.taffy.tree.RunMode;
import dev.vfyjxf.taffy.tree.LayoutInput;
import dev.vfyjxf.taffy.tree.SizingMode;
import dev.vfyjxf.taffy.tree.TaffyTree;
import dev.vfyjxf.taffy.tree.TreePrinter;
import dev.vfyjxf.taffy.util.MeasureFunc;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LowLevelLayoutApiTest {
    @Test
    void standaloneLayoutAlgorithmsAcceptAnExternalTreeAdapter() {
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.BLOCK;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(80f), TaffyDimension.length(30f));
        NodeId root = NodeId.of(1L);
        ExternalTree tree = new ExternalTree(root, rootStyle);

        LayoutAlgorithms.computeRootLayout(tree, root, new TaffySize<>(
            AvailableSpace.definite(80f), AvailableSpace.definite(30f)
        ));

        assertEquals(80f, tree.getUnroundedLayout(root).size().width, 0.01f);
        assertEquals(30f, tree.getUnroundedLayout(root).size().height, 0.01f);
        LayoutOutput output = tree.computeChildLayout(root, new LayoutInput(
            RunMode.COMPUTE_SIZE,
            SizingMode.INHERENT_SIZE,
            RequestedAxis.BOTH,
            new FloatSize(Float.NaN, Float.NaN),
            new TaffySize<>(false, false),
            new FloatSize(Float.NaN, Float.NaN),
            TaffySize.maxContent(),
            new TaffyLine<>(false, false)));
        assertEquals(80f, output.size().width, 0.01f);
    }

    @Test
    void treePrinterWritesLayoutMetadataToAnAppendable() throws IOException {
        TaffyTree tree = new TaffyTree();
        TaffyStyle style = new TaffyStyle();
        style.size = new TaffySize<>(TaffyDimension.length(10f), TaffyDimension.length(20f));
        NodeId root = tree.newLeaf(style);
        tree.computeLayout(root, new TaffySize<>(AvailableSpace.definite(10f), AvailableSpace.definite(20f)));

        StringBuilder output = new StringBuilder();
        TreePrinter.writeTree(output, tree, root);

        assertTrue(output.toString().contains("overflow:"));
        assertTrue(output.toString().contains(root.toString()));
    }

    @Test
    void roundTreePreservesOverflowAndBaselines() {
        TaffyTree tree = new TaffyTree();
        NodeId root = tree.newLeaf(new TaffyStyle());
        Baselines baselines = new Baselines(4f, 8f);
        tree.setUnroundedLayout(root, new Layout(
            0,
            new FloatPoint(0.25f, 0.25f),
            new FloatSize(10.5f, 10.5f),
            new FloatSize(10.5f, 10.5f),
            FloatSize.zero(),
            FloatRect.zero(),
            FloatRect.zero(),
            FloatRect.zero(),
            FloatRect.ltrb(-2f, -1f, 12f, 13f),
            baselines));

        LayoutAlgorithms.roundLayout(tree, root);

        assertEquals(FloatRect.ltrb(-2f, -1f, 12f, 13f), tree.getFinalLayout(root).scrollableOverflowRect());
        assertEquals(baselines, tree.getFinalLayout(root).baselines());
    }

    private static class ExternalTree implements LayoutPartialTree, RoundTree {
        private final NodeId root;
        private final TaffyStyle style;
        private Layout unroundedLayout = new Layout();
        private Layout finalLayout = new Layout();

        private ExternalTree(NodeId root, TaffyStyle style) {
            this.root = root;
            this.style = style;
        }

        @Override
        public List<NodeId> getChildren(NodeId parent) {
            return List.of();
        }

        @Override
        public int childCount(NodeId parent) {
            return 0;
        }

        @Override
        public TaffyStyle getStyle(NodeId node) {
            assertRoot(node);
            return style;
        }

        @Override
        public void setUnroundedLayout(NodeId node, Layout layout) {
            assertRoot(node);
            unroundedLayout = layout;
        }

        @Override
        public Layout getUnroundedLayout(NodeId node) {
            assertRoot(node);
            return unroundedLayout;
        }

        @Override
        public MeasureFunc getMeasureFunc(NodeId node) {
            return null;
        }

        @Override
        public LayoutOutput getCacheEntry(NodeId node, LayoutInput input) {
            return null;
        }

        @Override
        public void storeCacheEntry(NodeId node, LayoutInput input, LayoutOutput output) {
        }

        @Override
        public void clearCache(NodeId node) {
        }

        @Override
        public NodeId getParent(NodeId node) {
            return null;
        }

        @Override
        public void setDetailedLayoutInfo(NodeId node, DetailedLayoutInfo info) {
        }

        @Override
        public void setFinalLayout(NodeId node, Layout layout) {
            assertRoot(node);
            finalLayout = layout;
        }

        private void assertRoot(NodeId node) {
            if (!root.equals(node)) throw new IllegalArgumentException("Unknown node: " + node);
        }
    }
}
