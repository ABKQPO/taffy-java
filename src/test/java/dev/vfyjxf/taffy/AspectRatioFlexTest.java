package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.*;
import dev.vfyjxf.taffy.style.*;
import dev.vfyjxf.taffy.tree.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for aspect-ratio in flex layouts.
 * Based on GitHub issue DioxusLabs/taffy#804 and WPT flex-aspect-ratio tests.
 */
public class AspectRatioFlexTest {

    private static final float EPSILON = 0.1f;

    // Issue 804 regression coverage
    // Issue #804 regression test
    // WPT flex aspect-ratio coverage

    @Test
    @DisplayName("issue_804_aspect_ratio_flex_grow_row")
    void issue804AspectRatioFlexGrowRow() {
        // 8 items with min:64x64, max:128x128, flex-grow:1, aspect-ratio:1 in 2048x512 container
        // Each item should grow to 256 wide (2048/8), but be clamped to 128 by max-width.
        // With aspect-ratio:1, height should equal width = 128.
        TaffyTree tree = new TaffyTree();

        NodeId[] children = new NodeId[8];
        for (int i = 0; i < 8; i++) {
            TaffyStyle childStyle = new TaffyStyle();
            childStyle.direction = TaffyDirection.LTR;
            childStyle.minSize = new TaffySize<>(TaffyDimension.length(64.0f), TaffyDimension.length(64.0f));
            childStyle.maxSize = new TaffySize<>(TaffyDimension.length(128.0f), TaffyDimension.length(128.0f));
            childStyle.flexGrow = 1.0f;
            childStyle.aspectRatio = 1.0f;
            children[i] = tree.newLeaf(childStyle);
        }

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.ROW;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(2048.0f), TaffyDimension.length(512.0f));
        NodeId container = tree.newWithChildren(containerStyle, children);

        tree.computeLayout(container, TaffySize.maxContent());

        for (int i = 0; i < 8; i++) {
            Layout layout = tree.getLayout(children[i]);
            assertEquals(128.0f, layout.size().width, EPSILON, "width of child " + i);
            assertEquals(128.0f, layout.size().height, EPSILON, "height of child " + i);
        }
    }

    @Test
    @DisplayName("issue_804_aspect_ratio_flex_grow_column")
    void issue804AspectRatioFlexGrowColumn() {
        // Column version: 8 items with min:64x64, max:128x128, flex-grow:1, aspect-ratio:1
        // in 512x2048 container. Each should be 128x128.
        TaffyTree tree = new TaffyTree();

        NodeId[] children = new NodeId[8];
        for (int i = 0; i < 8; i++) {
            TaffyStyle childStyle = new TaffyStyle();
            childStyle.direction = TaffyDirection.LTR;
            childStyle.minSize = new TaffySize<>(TaffyDimension.length(64.0f), TaffyDimension.length(64.0f));
            childStyle.maxSize = new TaffySize<>(TaffyDimension.length(128.0f), TaffyDimension.length(128.0f));
            childStyle.flexGrow = 1.0f;
            childStyle.aspectRatio = 1.0f;
            children[i] = tree.newLeaf(childStyle);
        }

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.COLUMN;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(512.0f), TaffyDimension.length(2048.0f));
        NodeId container = tree.newWithChildren(containerStyle, children);

        tree.computeLayout(container, TaffySize.maxContent());

        for (int i = 0; i < 8; i++) {
            Layout layout = tree.getLayout(children[i]);
            assertEquals(128.0f, layout.size().width, EPSILON, "width of child " + i);
            assertEquals(128.0f, layout.size().height, EPSILON, "height of child " + i);
        }
    }

    // WPT flex aspect-ratio tests

    @Test
    @DisplayName("wpt_flex_aspect_ratio_001_row_height_ar1")
    void wptFlexAspectRatio001RowHeightAr1() {
        // Row flex, child has height:100px and aspect-ratio:1/1
        // Expected: child is 100x100
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.direction = TaffyDirection.LTR;
        childStyle.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(100.0f));
        childStyle.aspectRatio = 1.0f;
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.ROW;
        containerStyle.alignItems = AlignItems.START;
        NodeId container = tree.newWithChildren(containerStyle, child);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout childLayout = tree.getLayout(child);
        assertEquals(100.0f, childLayout.size().width, EPSILON, "width");
        assertEquals(100.0f, childLayout.size().height, EPSILON, "height");
    }

    @Test
    @DisplayName("wpt_flex_aspect_ratio_003_column_width_ar1")
    void wptFlexAspectRatio003ColumnWidthAr1() {
        // Column flex, child has width:100px and aspect-ratio:1/1
        // Expected: child is 100x100
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.direction = TaffyDirection.LTR;
        childStyle.size = new TaffySize<>(TaffyDimension.length(100.0f), TaffyDimension.AUTO);
        childStyle.aspectRatio = 1.0f;
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.COLUMN;
        containerStyle.alignItems = AlignItems.START;
        NodeId container = tree.newWithChildren(containerStyle, child);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout childLayout = tree.getLayout(child);
        assertEquals(100.0f, childLayout.size().width, EPSILON, "width");
        assertEquals(100.0f, childLayout.size().height, EPSILON, "height");
    }

    @Test
    @DisplayName("wpt_flex_aspect_ratio_005_row_nonsquare")
    void wptFlexAspectRatio005RowNonsquare() {
        // Row flex, two children with height:100px and aspect-ratio:1/2 (width/height = 0.5)
        // Expected: each child is 50x100
        TaffyTree tree = new TaffyTree();

        TaffyStyle child0Style = new TaffyStyle();
        child0Style.direction = TaffyDirection.LTR;
        child0Style.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(100.0f));
        child0Style.aspectRatio = 0.5f; // 1/2
        NodeId child0 = tree.newLeaf(child0Style);

        TaffyStyle child1Style = new TaffyStyle();
        child1Style.direction = TaffyDirection.LTR;
        child1Style.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(100.0f));
        child1Style.aspectRatio = 0.5f; // 1/2
        NodeId child1 = tree.newLeaf(child1Style);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.ROW;
        containerStyle.alignItems = AlignItems.START;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(200.0f), TaffyDimension.AUTO);
        NodeId container = tree.newWithChildren(containerStyle, child0, child1);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout child0Layout = tree.getLayout(child0);
        assertEquals(50.0f, child0Layout.size().width, EPSILON, "child0 width");
        assertEquals(100.0f, child0Layout.size().height, EPSILON, "child0 height");
        Layout child1Layout = tree.getLayout(child1);
        assertEquals(50.0f, child1Layout.size().width, EPSILON, "child1 width");
        assertEquals(100.0f, child1Layout.size().height, EPSILON, "child1 height");
    }

    @Test
    @DisplayName("wpt_flex_aspect_ratio_009_stretch_ar1")
    void wptFlexAspectRatio009StretchAr1() {
        // Row flex container height:100px, child has aspect-ratio:1/1, no explicit size
        // Default align-items:stretch -> child stretches cross to 100px
        // Then with AR 1:1, width should also be 100px
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.direction = TaffyDirection.LTR;
        childStyle.aspectRatio = 1.0f;
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.ROW;
        containerStyle.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(100.0f));
        NodeId container = tree.newWithChildren(containerStyle, child);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout childLayout = tree.getLayout(child);
        assertEquals(100.0f, childLayout.size().width, EPSILON, "width should match stretched height via AR");
        assertEquals(100.0f, childLayout.size().height, EPSILON, "height should be stretched to 100");
    }

    @Test
    @DisplayName("wpt_flex_aspect_ratio_010_auto_margins_no_stretch")
    void wptFlexAspectRatio010AutoMarginsNoStretch() {
        // Row flex container height:100px, child has aspect-ratio:1/1 and margin:auto
        // auto margins prevent stretch → child should be 0x0 (no content)
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.direction = TaffyDirection.LTR;
        childStyle.aspectRatio = 1.0f;
        childStyle.margin = TaffyRect.all(LengthPercentageAuto.auto());
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.ROW;
        containerStyle.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(100.0f));
        NodeId container = tree.newWithChildren(containerStyle, child);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout childLayout = tree.getLayout(child);
        assertEquals(0.0f, childLayout.size().width, EPSILON, "auto margins prevent stretch, no content -> 0 width");
        assertEquals(0.0f, childLayout.size().height, EPSILON, "auto margins prevent stretch, no content -> 0 height");
    }

    @Test
    @DisplayName("wpt_flex_aspect_ratio_stretch_with_max_cross")
    void wptFlexAspectRatioStretchWithMaxCross() {
        // Row flex container height:200px, child has aspect-ratio:2 (w/h), max-height:50px
        // Stretch would set cross to 200, but max-height:50 limits it
        // The max-height clamp in stretch should NOT be affected by aspect-ratio transfer
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.direction = TaffyDirection.LTR;
        childStyle.aspectRatio = 2.0f;
        childStyle.maxSize = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(50.0f));
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.ROW;
        containerStyle.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(200.0f));
        NodeId container = tree.newWithChildren(containerStyle, child);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout childLayout = tree.getLayout(child);
        // max-height:50 should clamp the stretched cross size to 50
        // then AR 2:1 gives width = 100
        assertEquals(100.0f, childLayout.size().width, EPSILON, "width = max-height * AR");
        assertEquals(50.0f, childLayout.size().height, EPSILON, "height clamped by max-height");
    }

    @Test
    @DisplayName("wpt_flex_aspect_ratio_stretch_column_with_max_cross")
    void wptFlexAspectRatioStretchColumnWithMaxCross() {
        // Column flex container width:200px, child has aspect-ratio:0.5 (w/h), max-width:50px
        // Stretch would set cross(width) to 200, but max-width:50 limits it
        // The max-width clamp in stretch should NOT be affected by aspect-ratio transfer
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.direction = TaffyDirection.LTR;
        childStyle.aspectRatio = 0.5f;
        childStyle.maxSize = new TaffySize<>(TaffyDimension.length(50.0f), TaffyDimension.AUTO);
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.COLUMN;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(200.0f), TaffyDimension.AUTO);
        NodeId container = tree.newWithChildren(containerStyle, child);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout childLayout = tree.getLayout(child);
        // max-width:50 should clamp the stretched cross size to 50
        // then AR 0.5 gives height = 100
        assertEquals(50.0f, childLayout.size().width, EPSILON, "width clamped by max-width");
        assertEquals(100.0f, childLayout.size().height, EPSILON, "height = max-width / AR");
    }

    @Test
    @DisplayName("aspect_ratio_flex_grow_positioning_row")
    void aspectRatioFlexGrowPositioningRow() {
        // Two items with flex-grow:1 and aspect-ratio:1 in 200px wide container
        // Each gets 100px width, with AR 1 -> 100px height
        // Item 0 at x=0, item 1 at x=100
        TaffyTree tree = new TaffyTree();

        TaffyStyle child0Style = new TaffyStyle();
        child0Style.direction = TaffyDirection.LTR;
        child0Style.flexGrow = 1.0f;
        child0Style.aspectRatio = 1.0f;
        child0Style.alignSelf = AlignItems.START;
        NodeId child0 = tree.newLeaf(child0Style);

        TaffyStyle child1Style = new TaffyStyle();
        child1Style.direction = TaffyDirection.LTR;
        child1Style.flexGrow = 1.0f;
        child1Style.aspectRatio = 1.0f;
        child1Style.alignSelf = AlignItems.START;
        NodeId child1 = tree.newLeaf(child1Style);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.ROW;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(200.0f), TaffyDimension.length(200.0f));
        containerStyle.alignItems = AlignItems.START;
        NodeId container = tree.newWithChildren(containerStyle, child0, child1);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout child0Layout = tree.getLayout(child0);
        assertEquals(100.0f, child0Layout.size().width, EPSILON, "child0 width");
        assertEquals(100.0f, child0Layout.size().height, EPSILON, "child0 height via AR");
        assertEquals(0.0f, child0Layout.location().x, EPSILON, "child0 x");

        Layout child1Layout = tree.getLayout(child1);
        assertEquals(100.0f, child1Layout.size().width, EPSILON, "child1 width");
        assertEquals(100.0f, child1Layout.size().height, EPSILON, "child1 height via AR");
        assertEquals(100.0f, child1Layout.location().x, EPSILON, "child1 x should be 100");
    }

    @Test
    @DisplayName("aspect_ratio_flex_grow_with_max_clamped_row")
    void aspectRatioFlexGrowWithMaxClampedRow() {
        // Two items with flex-grow:1, aspect-ratio:2 (w/h), max-width:40px in 200px container
        // Items get 100px from flex-grow but clamped to 40px by max-width
        // AR 2 -> height = 20px
        // Positioning: child0 at x=0, child1 at x=40 (offset by actual size, not target)
        TaffyTree tree = new TaffyTree();

        TaffyStyle child0Style = new TaffyStyle();
        child0Style.direction = TaffyDirection.LTR;
        child0Style.flexGrow = 1.0f;
        child0Style.aspectRatio = 2.0f;
        child0Style.maxSize = new TaffySize<>(TaffyDimension.length(40.0f), TaffyDimension.AUTO);
        child0Style.alignSelf = AlignItems.START;
        NodeId child0 = tree.newLeaf(child0Style);

        TaffyStyle child1Style = new TaffyStyle();
        child1Style.direction = TaffyDirection.LTR;
        child1Style.flexGrow = 1.0f;
        child1Style.aspectRatio = 2.0f;
        child1Style.maxSize = new TaffySize<>(TaffyDimension.length(40.0f), TaffyDimension.AUTO);
        child1Style.alignSelf = AlignItems.START;
        NodeId child1 = tree.newLeaf(child1Style);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.ROW;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(200.0f), TaffyDimension.length(200.0f));
        containerStyle.alignItems = AlignItems.START;
        NodeId container = tree.newWithChildren(containerStyle, child0, child1);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout child0Layout = tree.getLayout(child0);
        assertEquals(40.0f, child0Layout.size().width, EPSILON, "child0 width clamped to max");
        assertEquals(20.0f, child0Layout.size().height, EPSILON, "child0 height via AR 2:1");
        assertEquals(0.0f, child0Layout.location().x, EPSILON, "child0 x");

        Layout child1Layout = tree.getLayout(child1);
        assertEquals(40.0f, child1Layout.size().width, EPSILON, "child1 width clamped to max");
        assertEquals(20.0f, child1Layout.size().height, EPSILON, "child1 height via AR 2:1");
        // child1 x should be positioned after child0's actual size (40), not target size (100)
        assertEquals(40.0f, child1Layout.location().x, EPSILON, "child1 x should be after child0 actual size");
    }

    @Test
    @DisplayName("aspect_ratio_stretch_fill_min_width_column")
    void aspectRatioStretchFillMinWidthColumn() {
        // Column flex, child with aspect-ratio:2 (w/h), stretch cross (width)
        // Container width:100, child stretches to width=100, AR -> height=50
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.direction = TaffyDirection.LTR;
        childStyle.aspectRatio = 2.0f;
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.COLUMN;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(100.0f), TaffyDimension.length(200.0f));
        NodeId container = tree.newWithChildren(containerStyle, child);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout childLayout = tree.getLayout(child);
        assertEquals(100.0f, childLayout.size().width, EPSILON, "width stretched to container");
        assertEquals(50.0f, childLayout.size().height, EPSILON, "height from AR");
    }

    @Test
    @DisplayName("aspect_ratio_stretch_fill_min_height_row")
    void aspectRatioStretchFillMinHeightRow() {
        // Row flex, child with aspect-ratio:0.5 (w/h), stretch cross (height)
        // Container height:100, child stretches to height=100, AR -> width=50
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.direction = TaffyDirection.LTR;
        childStyle.aspectRatio = 0.5f;
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.ROW;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(200.0f), TaffyDimension.length(100.0f));
        NodeId container = tree.newWithChildren(containerStyle, child);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout childLayout = tree.getLayout(child);
        assertEquals(50.0f, childLayout.size().width, EPSILON, "width from AR");
        assertEquals(100.0f, childLayout.size().height, EPSILON, "height stretched to container");
    }

    @Test
    @DisplayName("parent_auto_height_child_ar_maxsize")
    void parentAutoHeightChildArMaxsize() {
        // Bug: parent with auto height contains a child with width:100%, maxSize(100,100), AR=1
        // In a column flex grandparent, parent is stretched to full width (500).
        // Child width = 100% of 500 = 500, clamped to maxWidth=100. AR=1 -> height=100.
        // Parent height should = 100 (content height), NOT 500.
        TaffyTree tree = new TaffyTree();

        // Child: width=100%, maxSize(100,100), AR=1
        TaffyStyle childStyle = new TaffyStyle();
        childStyle.size = new TaffySize<>(TaffyDimension.percent(1.0f), TaffyDimension.AUTO);
        childStyle.maxSize = new TaffySize<>(TaffyDimension.length(100.0f), TaffyDimension.length(100.0f));
        childStyle.aspectRatio = 1.0f;
        NodeId child = tree.newLeaf(childStyle);

        // Parent: auto size, default row flex
        TaffyStyle parentStyle = new TaffyStyle();
        parentStyle.display = TaffyDisplay.FLEX;
        parentStyle.flexDirection = FlexDirection.ROW;
        NodeId parent = tree.newWithChildren(parentStyle, child);

        // Grandparent: column flex, 500x500
        TaffyStyle grandparentStyle = new TaffyStyle();
        grandparentStyle.display = TaffyDisplay.FLEX;
        grandparentStyle.flexDirection = FlexDirection.COLUMN;
        grandparentStyle.size = new TaffySize<>(TaffyDimension.length(500.0f), TaffyDimension.length(500.0f));
        NodeId grandparent = tree.newWithChildren(grandparentStyle, parent);

        tree.computeLayout(grandparent, TaffySize.maxContent());

        Layout parentLayout = tree.getLayout(parent);
        Layout childLayout = tree.getLayout(child);

        System.out.println("=== Row parent in column grandparent ===");
        System.out.println("Parent: " + parentLayout.size().width + " x " + parentLayout.size().height);
        System.out.println("Child: " + childLayout.size().width + " x " + childLayout.size().height);

        // Child should be 100x100 (clamped by maxSize)
        assertEquals(100.0f, childLayout.size().width, EPSILON, "child width clamped by maxWidth");
        assertEquals(100.0f, childLayout.size().height, EPSILON, "child height from AR");

        // Parent width = 500 (stretched by grandparent column flex)
        assertEquals(500.0f, parentLayout.size().width, EPSILON, "parent width stretched");
        // Parent height should be 100 (auto, sized by child content), NOT 500
        assertEquals(100.0f, parentLayout.size().height, EPSILON,
            "parent auto height should match child height (100), not parent width (500)");
    }

    @Test
    @DisplayName("parent_auto_height_child_ar_maxsize_column")
    void parentAutoHeightChildArMaxsizeColumn() {
        // Same as above but parent is column flex (user's default might be column)
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.size = new TaffySize<>(TaffyDimension.percent(1.0f), TaffyDimension.AUTO);
        childStyle.maxSize = new TaffySize<>(TaffyDimension.length(100.0f), TaffyDimension.length(100.0f));
        childStyle.aspectRatio = 1.0f;
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle parentStyle = new TaffyStyle();
        parentStyle.display = TaffyDisplay.FLEX;
        parentStyle.flexDirection = FlexDirection.COLUMN;
        NodeId parent = tree.newWithChildren(parentStyle, child);

        TaffyStyle grandparentStyle = new TaffyStyle();
        grandparentStyle.display = TaffyDisplay.FLEX;
        grandparentStyle.flexDirection = FlexDirection.COLUMN;
        grandparentStyle.size = new TaffySize<>(TaffyDimension.length(500.0f), TaffyDimension.length(500.0f));
        NodeId grandparent = tree.newWithChildren(grandparentStyle, parent);

        tree.computeLayout(grandparent, TaffySize.maxContent());

        Layout parentLayout = tree.getLayout(parent);
        Layout childLayout = tree.getLayout(child);

        System.out.println("=== Column parent in column grandparent ===");
        System.out.println("Parent: " + parentLayout.size().width + " x " + parentLayout.size().height);
        System.out.println("Child: " + childLayout.size().width + " x " + childLayout.size().height);

        assertEquals(100.0f, childLayout.size().width, EPSILON, "child width clamped by maxWidth");
        assertEquals(100.0f, childLayout.size().height, EPSILON, "child height from AR");
        assertEquals(500.0f, parentLayout.size().width, EPSILON, "parent width stretched");
        assertEquals(100.0f, parentLayout.size().height, EPSILON,
            "parent auto height should match child height (100)");
    }

    @Test
    @DisplayName("parent_auto_height_child_ar_maxsize_root")
    void parentAutoHeightChildArMaxsizeRoot() {
        // Parent IS the root, computeLayout with definite available space
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.size = new TaffySize<>(TaffyDimension.percent(1.0f), TaffyDimension.AUTO);
        childStyle.maxSize = new TaffySize<>(TaffyDimension.length(100.0f), TaffyDimension.length(100.0f));
        childStyle.aspectRatio = 1.0f;
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle parentStyle = new TaffyStyle();
        parentStyle.display = TaffyDisplay.FLEX;
        parentStyle.flexDirection = FlexDirection.ROW;
        NodeId parent = tree.newWithChildren(parentStyle, child);

        tree.computeLayout(parent, new TaffySize<>(AvailableSpace.definite(500.0f), AvailableSpace.definite(500.0f)));

        Layout parentLayout = tree.getLayout(parent);
        Layout childLayout = tree.getLayout(child);

        System.out.println("=== Root scenario ===");
        System.out.println("Parent: " + parentLayout.size().width + " x " + parentLayout.size().height);
        System.out.println("Child: " + childLayout.size().width + " x " + childLayout.size().height);

        assertEquals(100.0f, childLayout.size().width, EPSILON, "child width clamped by maxWidth");
        assertEquals(100.0f, childLayout.size().height, EPSILON, "child height from AR");
        // Root with auto size should size to content
        assertEquals(100.0f, parentLayout.size().height, EPSILON,
            "parent auto height should match child height (100)");
    }

    @Test
    @DisplayName("aspect_ratio_flex_basis_content_row")
    void aspectRatioFlexBasisContentRow() {
        // flex-basis:content with aspect-ratio, child has height:50px, AR:2(w/h)
        // In row direction, flex-basis is about main(width), which should be derived from height via AR
        // Expected: width=100, height=50
        TaffyTree tree = new TaffyTree();

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.direction = TaffyDirection.LTR;
        childStyle.flexBasis = TaffyDimension.AUTO; // flex-basis:content is the default
        childStyle.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(50.0f));
        childStyle.aspectRatio = 2.0f;
        childStyle.alignSelf = AlignItems.START;
        NodeId child = tree.newLeaf(childStyle);

        TaffyStyle containerStyle = new TaffyStyle();
        containerStyle.direction = TaffyDirection.LTR;
        containerStyle.display = TaffyDisplay.FLEX;
        containerStyle.flexDirection = FlexDirection.ROW;
        containerStyle.size = new TaffySize<>(TaffyDimension.length(300.0f), TaffyDimension.length(200.0f));
        containerStyle.alignItems = AlignItems.START;
        NodeId container = tree.newWithChildren(containerStyle, child);

        tree.computeLayout(container, TaffySize.maxContent());

        Layout childLayout = tree.getLayout(child);
        assertEquals(100.0f, childLayout.size().width, EPSILON, "width from AR * height");
        assertEquals(50.0f, childLayout.size().height, EPSILON, "explicit height");
    }
    @Test
    @DisplayName("parent_auto_height_child_ar_maxsize_with_padding")
    void parentAutoHeightChildArMaxsizeWithPadding() {
        TaffyTree tree = new TaffyTree();

        // Child: width=100%, maxSize(100,100), AR=1
        TaffyStyle childStyle = new TaffyStyle();
        childStyle.flexDirection = FlexDirection.COLUMN;
        childStyle.flexShrink = 0;
        childStyle.minSize = TaffySize.all(TaffyDimension.ZERO);
        childStyle.alignContent = AlignContent.FLEX_START;
        childStyle.size = new TaffySize<>(TaffyDimension.percent(1.0f), TaffyDimension.AUTO);
        childStyle.maxSize = new TaffySize<>(TaffyDimension.length(100.0f), TaffyDimension.length(100.0f));
        childStyle.aspectRatio = 1.0f;
        NodeId child = tree.newLeaf(childStyle);

        // Parent: auto size with padding
        TaffyStyle parentStyle = new TaffyStyle();
        parentStyle.flexDirection = FlexDirection.COLUMN;
        parentStyle.flexShrink = 0;
        parentStyle.minSize = TaffySize.all(TaffyDimension.ZERO);
        parentStyle.alignContent = AlignContent.FLEX_START;
        parentStyle.padding = TaffyRect.all(LengthPercentage.length(5));
        NodeId parent = tree.newWithChildren(parentStyle, child);

        // Root: definite size + padding + gap
        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.flexDirection = FlexDirection.COLUMN;
        rootStyle.flexShrink = 0;
        rootStyle.minSize = TaffySize.all(TaffyDimension.ZERO);
        rootStyle.alignContent = AlignContent.FLEX_START;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(300.0f), TaffyDimension.length(300.0f));
        rootStyle.padding = TaffyRect.all(LengthPercentage.length(4));
        rootStyle.gap = TaffySize.all(LengthPercentage.length(3));
        NodeId root = tree.newWithChildren(rootStyle, parent);

        tree.computeLayout(root, new TaffySize<>(
            AvailableSpace.definite(300.0f),
            AvailableSpace.definite(300.0f)
        ));

        Layout parentLayout = tree.getLayout(parent);
        Layout childLayout = tree.getLayout(child);

        assertEquals(100.0f, childLayout.size().width, EPSILON, "child width clamped by max");
        assertEquals(100.0f, childLayout.size().height, EPSILON, "child height from AR");
        assertEquals(110.0f, parentLayout.size().height, EPSILON,
            "parent auto height should be child height + padding (100 + 10)");
    }
}
