package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.MinMax;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.GridRepetition;
import dev.vfyjxf.taffy.style.GridTemplateComponent;
import dev.vfyjxf.taffy.style.StyleHelpers;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Style helper tests ported from taffy/src/style_helpers.rs
 */
public class StyleHelpersTest {

    @Test
    @DisplayName("test_repeat_u16")
    void testRepeatU16() {
        GridTemplateComponent comp = GridTemplateComponent.repeatCount(123);
        assertTrue(comp.isRepeat());
        GridRepetition repeat = comp.getRepeat();
        assertNotNull(repeat);
        assertEquals(GridRepetition.RepetitionType.COUNT, repeat.getType());
        assertEquals(123, repeat.getCount());
        assertEquals(0, repeat.getTracks().size());
    }

    @Test
    @DisplayName("test_repeat_auto_fit_str")
    void testRepeatAutoFitStr() {
        GridTemplateComponent comp = GridTemplateComponent.autoFit();
        assertTrue(comp.isRepeat());
        GridRepetition repeat = comp.getRepeat();
        assertNotNull(repeat);
        assertEquals(GridRepetition.RepetitionType.AUTO_FIT, repeat.getType());
        assertEquals(0, repeat.getCount());
        assertEquals(List.of(), repeat.getTracks());
    }

    @Test
    @DisplayName("test_repeat_auto_fill_str")
    void testRepeatAutoFillStr() {
        GridTemplateComponent comp = GridTemplateComponent.autoFill();
        assertTrue(comp.isRepeat());
        GridRepetition repeat = comp.getRepeat();
        assertNotNull(repeat);
        assertEquals(GridRepetition.RepetitionType.AUTO_FILL, repeat.getType());
        assertEquals(0, repeat.getCount());
        assertEquals(List.of(), repeat.getTracks());
    }

    @Test
    @DisplayName("grid helper functions preserve their source values")
    void gridHelperFunctionsPreserveTheirSourceValues() {
        MinMax<Integer, String> pair = new MinMax<>(1, "max");
        assertEquals(1, pair.min);
        assertEquals("max", pair.max);
        assertEquals(pair, new MinMax<>(1, "max"));

        GridTemplateComponent repeat = StyleHelpers.repeat(3, TrackSizingFunction.fixed(10f));
        assertEquals(3, repeat.getRepeat().getCount());
        assertEquals(List.of(TrackSizingFunction.fixed(10f)), repeat.getRepeat().getTracks());

        List<GridTemplateComponent> equalTracks = StyleHelpers.evenlySizedTracks(2);
        assertEquals(1, equalTracks.size());
        assertEquals(2, equalTracks.get(0).getRepeat().getCount());
        assertEquals(TrackSizingFunction.minmax(TrackSizingFunction.fixed(0f), TrackSizingFunction.fr(1f)),
            equalTracks.get(0).getRepeat().getTracks().get(0));
        assertEquals(GridPlacement.line(-2), StyleHelpers.line(-2));
        assertEquals(GridPlacement.span(3), StyleHelpers.span(3));
        assertEquals(TrackSizingFunction.minmax(TrackSizingFunction.fixed(5f), TrackSizingFunction.fr(2f)),
            StyleHelpers.minmax(TrackSizingFunction.fixed(5f), StyleHelpers.fr(2f)));
        assertEquals(TrackSizingFunction.minmax(TrackSizingFunction.fixed(0f), TrackSizingFunction.fr(2f)),
            StyleHelpers.flex(2f));
    }
}
