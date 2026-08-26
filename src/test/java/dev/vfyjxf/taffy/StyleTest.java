package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.style.Contain;

import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyStyle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Style tests ported from taffy/src/style/flex.rs
 */
public class StyleTest {

    @Test
    void containmentParsingAndCopyPreserveFlags() {
        assertEquals(Contain.CONTENT, Contain.parse("layout paint"));
        assertEquals(Contain.CONTENT, Contain.parse("paint style layout"));
        assertEquals("content", Contain.parse("layout paint").toString());
        assertThrows(IllegalArgumentException.class, () -> Contain.parse("layout layout"));

        TaffyStyle style = new TaffyStyle();
        style.contain = Contain.PAINT;
        assertEquals(Contain.PAINT, style.copy().contain);
    }

    @Nested
    @DisplayName("FlexDirection")
    class FlexDirectionTests {

        @Test
        @DisplayName("flex_direction_is_row")
        void flexDirectionIsRow() {
            assertTrue(FlexDirection.ROW.isRow());
            assertTrue(FlexDirection.ROW_REVERSE.isRow());
            assertFalse(FlexDirection.COLUMN.isRow());
            assertFalse(FlexDirection.COLUMN_REVERSE.isRow());
        }

        @Test
        @DisplayName("flex_direction_is_column")
        void flexDirectionIsColumn() {
            assertFalse(FlexDirection.ROW.isColumn());
            assertFalse(FlexDirection.ROW_REVERSE.isColumn());
            assertTrue(FlexDirection.COLUMN.isColumn());
            assertTrue(FlexDirection.COLUMN_REVERSE.isColumn());
        }

        @Test
        @DisplayName("flex_direction_is_reverse")
        void flexDirectionIsReverse() {
            assertFalse(FlexDirection.ROW.isReverse());
            assertTrue(FlexDirection.ROW_REVERSE.isReverse());
            assertFalse(FlexDirection.COLUMN.isReverse());
            assertTrue(FlexDirection.COLUMN_REVERSE.isReverse());
        }
    }
}
