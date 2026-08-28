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
import dev.vfyjxf.taffy.tree.BlockLayoutComputeFunc;
import dev.vfyjxf.taffy.tree.CacheTree;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.LayoutAlgorithms;
import dev.vfyjxf.taffy.tree.LayoutOutput;
import dev.vfyjxf.taffy.tree.LayoutBlockContainer;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    @Test
    void cachedLayoutAcceptsAnIndependentCacheAndCallerComputeFunction() {
        NodeId node = NodeId.of(5L);
        LayoutInput input = sizingInput();
        CallbackCache cache = new CallbackCache();
        AtomicInteger calls = new AtomicInteger();
        LayoutOutput expected = LayoutOutput.fromOuterSize(new FloatSize(25f, 15f));

        LayoutOutput first = LayoutAlgorithms.computeCachedLayout(cache, node, input, (ignoredNode, ignoredInput) -> {
            calls.incrementAndGet();
            return expected;
        });
        LayoutOutput second = LayoutAlgorithms.computeCachedLayout(cache, node, input, (ignoredNode, ignoredInput) -> {
            calls.incrementAndGet();
            return LayoutOutput.hidden();
        });

        assertSame(expected, first);
        assertSame(expected, second);
        assertEquals(1, calls.get());
    }

    @Test
    void leafLayoutAcceptsACallerSuppliedMeasureFunction() {
        NodeId node = NodeId.of(6L);
        ExternalTree tree = new ExternalTree(node, new TaffyStyle());

        LayoutOutput output = LayoutAlgorithms.computeLeafLayout(
            tree,
            node,
            sizingInput(),
            (knownDimensions, availableSpace) -> new FloatSize(21f, 34f));

        assertEquals(21f, output.size().width, 0.01f);
        assertEquals(34f, output.size().height, 0.01f);
    }

    @Test
    void blockLayoutPassesSharedFormattingContextToExternalChildDispatch() {
        NodeId root = NodeId.of(7L);
        NodeId child = NodeId.of(8L);
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.BLOCK;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.length(40f));
        TaffyStyle childStyle = new TaffyStyle();
        childStyle.display = TaffyDisplay.BLOCK;
        ExternalBlockTree tree = new ExternalBlockTree(root, child, rootStyle, childStyle);
        AtomicInteger calls = new AtomicInteger();

        BlockLayoutComputeFunc<ExternalBlockTree> dispatch = (receivedTree, node, input, blockContext) -> {
            assertSame(tree, receivedTree);
            assertEquals(child, node);
            assertEquals(RunMode.PERFORM_LAYOUT, input.runMode());
            assertTrue(blockContext.hasActiveFloats(0f) || !blockContext.hasFloats());
            calls.incrementAndGet();
            return LayoutOutput.fromOuterSize(new FloatSize(20f, 10f));
        };

        LayoutOutput output = LayoutAlgorithms.computeBlockLayout(
            tree, root, layoutInput(100f, 40f), dispatch);

        assertEquals(100f, output.size().width, 0.01f);
        assertEquals(1, calls.get());
    }

    private LayoutInput sizingInput() {
        return new LayoutInput(
            RunMode.COMPUTE_SIZE,
            SizingMode.INHERENT_SIZE,
            RequestedAxis.BOTH,
            new FloatSize(Float.NaN, Float.NaN),
            new TaffySize<>(false, false),
            new FloatSize(Float.NaN, Float.NaN),
            TaffySize.maxContent(),
            new TaffyLine<>(false, false));
    }

    private LayoutInput layoutInput(float width, float height) {
        return new LayoutInput(
            RunMode.PERFORM_LAYOUT,
            SizingMode.INHERENT_SIZE,
            RequestedAxis.BOTH,
            new FloatSize(width, height),
            new TaffySize<>(true, true),
            new FloatSize(width, height),
            new TaffySize<>(AvailableSpace.definite(width), AvailableSpace.definite(height)),
            new TaffyLine<>(false, false));
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

        protected NodeId rootNode() {
            return root;
        }
    }

    private static class CallbackCache implements CacheTree {
        private LayoutInput input;
        private LayoutOutput output;

        @Override
        public LayoutOutput cacheGet(NodeId node, LayoutInput input) {
            return this.input != null && this.input.equals(input) ? output : null;
        }

        @Override
        public void cacheStore(NodeId node, LayoutInput input, LayoutOutput output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public void cacheClear(NodeId node) {
            input = null;
            output = null;
        }
    }

    private static class ExternalBlockTree extends ExternalTree implements LayoutBlockContainer {
        private final NodeId child;
        private final TaffyStyle childStyle;
        private Layout childLayout = new Layout();

        private ExternalBlockTree(NodeId root, NodeId child, TaffyStyle rootStyle, TaffyStyle childStyle) {
            super(root, rootStyle);
            this.child = child;
            this.childStyle = childStyle;
        }

        @Override
        public List<NodeId> getChildren(NodeId parent) {
            return rootNode().equals(parent) ? List.of(child) : List.of();
        }

        @Override
        public int childCount(NodeId parent) {
            return rootNode().equals(parent) ? 1 : 0;
        }

        @Override
        public TaffyStyle getStyle(NodeId node) {
            return child.equals(node) ? childStyle : super.getStyle(node);
        }

        @Override
        public void setUnroundedLayout(NodeId node, Layout layout) {
            if (child.equals(node)) {
                childLayout = layout;
            } else {
                super.setUnroundedLayout(node, layout);
            }
        }

        @Override
        public Layout getUnroundedLayout(NodeId node) {
            return child.equals(node) ? childLayout : super.getUnroundedLayout(node);
        }
    }
}
