package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.LayoutCache;
import dev.vfyjxf.taffy.tree.LayoutInput;
import dev.vfyjxf.taffy.tree.LayoutOutput;
import dev.vfyjxf.taffy.tree.RequestedAxis;
import dev.vfyjxf.taffy.tree.RunMode;
import dev.vfyjxf.taffy.tree.SizingMode;
import dev.vfyjxf.taffy.tree.TaffyTree;
import dev.vfyjxf.taffy.util.MeasureFunc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Diagnostic test to understand caching behavior
 */
public class CacheDiagnosticTest {

    @Test
    @DisplayName("single-axis cache entries do not satisfy another axis")
    void singleAxisCacheEntriesDoNotSatisfyAnotherAxis() {
        LayoutCache cache = new LayoutCache();
        FloatSize knownDimensions = FloatSize.none();
        TaffySize<AvailableSpace> availableSpace = TaffySize.maxContent();
        TaffySize<Boolean> knownDimensionsAreDefinite = new TaffySize<>(true, true);
        LayoutOutput horizontalOutput = LayoutOutput.fromOuterSize(new FloatSize(100.0f, 0.0f));

        cache.store(
            cacheInput(knownDimensions, availableSpace, RequestedAxis.HORIZONTAL, knownDimensionsAreDefinite),
            horizontalOutput
        );

        assertNull(cache.get(cacheInput(
            knownDimensions, availableSpace, RequestedAxis.VERTICAL, knownDimensionsAreDefinite
        )));

        cache.store(
            cacheInput(knownDimensions, availableSpace, RequestedAxis.BOTH, knownDimensionsAreDefinite),
            LayoutOutput.fromOuterSize(new FloatSize(100.0f, 50.0f))
        );

        assertNotNull(cache.get(cacheInput(
            knownDimensions, availableSpace, RequestedAxis.VERTICAL, knownDimensionsAreDefinite
        )));
    }

    private LayoutInput cacheInput(
        FloatSize knownDimensions,
        TaffySize<AvailableSpace> availableSpace,
        RequestedAxis axis,
        TaffySize<Boolean> knownDimensionsAreDefinite
    ) {
        return new LayoutInput(
            RunMode.COMPUTE_SIZE,
            SizingMode.INHERENT_SIZE,
            axis,
            knownDimensions,
            knownDimensionsAreDefinite,
            FloatSize.none(),
            availableSpace,
            TaffyLine.FALSE
        );
    }

    @Test
    @DisplayName("simple_single_node_measure")
    void simpleSingleNodeMeasure() {
        TaffyTree tree = new TaffyTree();
        
        AtomicInteger measureCount = new AtomicInteger(0);
        
        MeasureFunc countingMeasure = (knownDimensions, availableSpace) -> {
            int count = measureCount.incrementAndGet();
            System.out.println("Measure called: count=" + count + 
                ", knownDimensions=" + knownDimensions + 
                ", availableSpace=" + availableSpace);
            return new FloatSize(50.0f, 50.0f);
        };
        
        TaffyStyle leafStyle = new TaffyStyle();
        NodeId leaf = tree.newLeafWithMeasure(leafStyle, countingMeasure);
        
        System.out.println("=== First computeLayout ===");
        tree.computeLayout(leaf, TaffySize.maxContent());
        int firstCount = measureCount.get();
        System.out.println("After first layout: measureCount=" + firstCount);
        
        // Second layout without changes should hit cache
        System.out.println("=== Second computeLayout (same params, should hit cache) ===");
        tree.computeLayout(leaf, TaffySize.maxContent());
        int secondCount = measureCount.get();
        System.out.println("After second layout: measureCount=" + secondCount);
        
        assertEquals(firstCount, secondCount, 
            "Measure should not be called again for identical layout");
    }

    @Test
    @DisplayName("one_level_nesting_measure")
    void oneLevelNestingMeasure() {
        TaffyTree tree = new TaffyTree();
        
        AtomicInteger measureCount = new AtomicInteger(0);
        
        MeasureFunc countingMeasure = (knownDimensions, availableSpace) -> {
            int count = measureCount.incrementAndGet();
            System.out.println("Measure#" + count + ": known=" + knownDimensions + ", avail=" + availableSpace);
            return new FloatSize(50.0f, 50.0f);
        };
        
        TaffyStyle leafStyle = new TaffyStyle();
        NodeId leaf = tree.newLeafWithMeasure(leafStyle, countingMeasure);
        
        // One parent wrapping the leaf
        TaffyStyle parentStyle = new TaffyStyle();
        NodeId parent = tree.newWithChildren(parentStyle, leaf);
        
        System.out.println("=== First computeLayout with one level nesting ===");
        tree.computeLayout(parent, TaffySize.maxContent());
        int firstCount = measureCount.get();
        System.out.println("After first layout: measureCount=" + firstCount);
        
        // Ideally measure should be called <= 4 times (for different sizing modes)
        assertTrue(firstCount <= 10, 
            "Measure called " + firstCount + " times for single nested node, expected <= 10");
    }

    @Test  
    @DisplayName("two_level_nesting_measure")
    void twoLevelNestingMeasure() {
        TaffyTree tree = new TaffyTree();
        
        AtomicInteger measureCount = new AtomicInteger(0);
        
        MeasureFunc countingMeasure = (knownDimensions, availableSpace) -> {
            measureCount.incrementAndGet();
            return new FloatSize(50.0f, 50.0f);
        };
        
        TaffyStyle leafStyle = new TaffyStyle();
        NodeId leaf = tree.newLeafWithMeasure(leafStyle, countingMeasure);
        
        // Two levels of nesting
        NodeId node = tree.newWithChildren(new TaffyStyle(), leaf);
        node = tree.newWithChildren(new TaffyStyle(), node);
        
        System.out.println("=== computeLayout with two level nesting ===");
        tree.computeLayout(node, TaffySize.maxContent());
        int count = measureCount.get();
        System.out.println("After layout: measureCount=" + count);
        
        assertTrue(count <= 20, 
            "Measure called " + count + " times for two level nesting, expected <= 20");
    }

    @Test  
    @DisplayName("three_level_nesting_measure")
    void threeLevelNestingMeasure() {
        TaffyTree tree = new TaffyTree();
        
        AtomicInteger measureCount = new AtomicInteger(0);
        
        MeasureFunc countingMeasure = (knownDimensions, availableSpace) -> {
            measureCount.incrementAndGet();
            return new FloatSize(50.0f, 50.0f);
        };
        
        TaffyStyle leafStyle = new TaffyStyle();
        NodeId leaf = tree.newLeafWithMeasure(leafStyle, countingMeasure);
        
        // Three levels of nesting
        NodeId node = tree.newWithChildren(new TaffyStyle(), leaf);
        node = tree.newWithChildren(new TaffyStyle(), node);
        node = tree.newWithChildren(new TaffyStyle(), node);
        
        System.out.println("=== computeLayout with three level nesting ===");
        tree.computeLayout(node, TaffySize.maxContent());
        int count = measureCount.get();
        System.out.println("After layout: measureCount=" + count);
        
        assertTrue(count <= 50, 
            "Measure called " + count + " times for three level nesting, expected <= 50");
    }
}
