package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Border and padding tests ported from taffy/tests/border_and_padding.rs
 * All tests are ignored in Rust; kept disabled here as well.
 */
// @Disabled("Ignored in Rust; grid/border edge cases pending") - Temporarily enabled for investigation
public class BorderAndPaddingTest {

    private TaffyRect<LengthPercentage> arrToRect(LengthPercentage[] items) {
        return new TaffyRect<>(items[0], items[1], items[2], items[3]);
    }

    @Test
    @DisplayName("border_on_a_single_axis_doesnt_increase_size")
    void borderOnSingleAxisDoesntIncreaseSize() {
        for (int i = 0; i < 4; i++) {
            TaffyTree tree = new TaffyTree();
            LengthPercentage[] lengths = new LengthPercentage[] {
                LengthPercentage.ZERO, LengthPercentage.ZERO, LengthPercentage.ZERO, LengthPercentage.ZERO
            };
            lengths[i] = LengthPercentage.length(10f);
            TaffyStyle style = new TaffyStyle();
            style.border = arrToRect(lengths);
            NodeId node = tree.newLeaf(style);

            tree.computeLayout(node, new TaffySize<>(
                AvailableSpace.definite(100f),
                AvailableSpace.definite(100f)
            ));

            Layout layout = tree.getLayout(node);
            assertEquals(0f, layout.size().width * layout.size().height, 0.0001f);
        }
    }

    @Test
    @DisplayName("padding_on_a_single_axis_doesnt_increase_size")
    void paddingOnSingleAxisDoesntIncreaseSize() {
        for (int i = 0; i < 4; i++) {
            TaffyTree tree = new TaffyTree();
            LengthPercentage[] lengths = new LengthPercentage[] {
                LengthPercentage.ZERO, LengthPercentage.ZERO, LengthPercentage.ZERO, LengthPercentage.ZERO
            };
            lengths[i] = LengthPercentage.length(10f);
            TaffyStyle style = new TaffyStyle();
            style.padding = arrToRect(lengths);
            NodeId node = tree.newLeaf(style);

            tree.computeLayout(node, new TaffySize<>(
                AvailableSpace.definite(100f),
                AvailableSpace.definite(100f)
            ));

            Layout layout = tree.getLayout(node);
            assertEquals(0f, layout.size().width * layout.size().height, 0.0001f);
        }
    }

    @Test
    @DisplayName("border_and_padding_on_a_single_axis_doesnt_increase_size")
    void borderAndPaddingOnSingleAxisDoesntIncreaseSize() {
        for (int i = 0; i < 4; i++) {
            TaffyTree tree = new TaffyTree();
            LengthPercentage[] lengths = new LengthPercentage[] {
                LengthPercentage.ZERO, LengthPercentage.ZERO, LengthPercentage.ZERO, LengthPercentage.ZERO
            };
            lengths[i] = LengthPercentage.length(10f);
            TaffyRect<LengthPercentage> rect = arrToRect(lengths);
            TaffyStyle style = new TaffyStyle();
            style.border = rect;
            style.padding = rect;
            NodeId node = tree.newLeaf(style);

            tree.computeLayout(node, new TaffySize<>(
                AvailableSpace.definite(100f),
                AvailableSpace.definite(100f)
            ));

            Layout layout = tree.getLayout(node);
            assertEquals(0f, layout.size().width * layout.size().height, 0.0001f);
        }
    }

    @Test
    @DisplayName("vertical_border_and_padding_percentage_values_use_available_space_correctly")
    void verticalBorderAndPaddingPercentageValuesUseAvailableSpaceCorrectly() {
        TaffyTree tree = new TaffyTree();

        TaffyStyle style = new TaffyStyle();
        style.padding = new TaffyRect<>(
            LengthPercentage.percent(1.0f),
            LengthPercentage.ZERO,
            LengthPercentage.percent(1.0f),
            LengthPercentage.ZERO
        );

        NodeId node = tree.newLeaf(style);
        tree.computeLayout(node, new TaffySize<>(
            AvailableSpace.definite(200f),
            AvailableSpace.definite(100f)
        ));

        Layout layout = tree.getLayout(node);
        assertEquals(200f, layout.size().width, 0.0001f);
        assertEquals(200f, layout.size().height, 0.0001f);
    }

    @Test
    @DisplayName("block_percentage_padding_uses_containing_block_width_for_nonstretch_width")
    void blockPercentagePaddingUsesContainingBlockWidthForNonstretchWidth() {
        TaffyTree tree = new TaffyTree();

        TaffyStyle grandchildStyle = new TaffyStyle();
        grandchildStyle.display = TaffyDisplay.BLOCK;
        grandchildStyle.size = new TaffySize<>(TaffyDimension.AUTO, TaffyDimension.length(10f));
        NodeId grandchild = tree.newLeaf(grandchildStyle);

        TaffyStyle childStyle = new TaffyStyle();
        childStyle.display = TaffyDisplay.BLOCK;
        childStyle.size = new TaffySize<>(TaffyDimension.length(100f), TaffyDimension.AUTO);
        childStyle.padding = new TaffyRect<>(
            LengthPercentage.percent(0.1f),
            LengthPercentage.percent(0.1f),
            LengthPercentage.percent(0.1f),
            LengthPercentage.percent(0.1f)
        );
        NodeId child = tree.newWithChildren(childStyle, grandchild);

        TaffyStyle rootStyle = new TaffyStyle();
        rootStyle.display = TaffyDisplay.BLOCK;
        rootStyle.size = new TaffySize<>(TaffyDimension.length(40f), TaffyDimension.AUTO);
        NodeId root = tree.newWithChildren(rootStyle, child);

        tree.computeLayout(root, TaffySize.maxContent());

        assertEquals(18f, tree.getLayout(root).size().height, 0.01f);
        assertEquals(100f, tree.getLayout(child).size().width, 0.01f);
        assertEquals(18f, tree.getLayout(child).size().height, 0.01f);
        assertEquals(4f, tree.getLayout(grandchild).location().x, 0.01f);
        assertEquals(4f, tree.getLayout(grandchild).location().y, 0.01f);
        assertEquals(92f, tree.getLayout(grandchild).size().width, 0.01f);
    }
}
