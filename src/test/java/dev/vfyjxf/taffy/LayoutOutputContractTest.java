package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.geometry.FloatPoint;
import dev.vfyjxf.taffy.geometry.FloatRect;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.tree.Baselines;
import dev.vfyjxf.taffy.tree.CollapsibleMarginSet;
import dev.vfyjxf.taffy.tree.LayoutOutput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LayoutOutputContractTest {
    @Test
    void hiddenOutputHasEmptyOverflowAndBaselines() {
        LayoutOutput hidden = LayoutOutput.hidden();

        assertEquals(FloatRect.zero(), hidden.scrollableOverflowRect());
        assertTrue(hidden.baselines().isNone());
        assertTrue(Float.isNaN(hidden.baselines().first()));
        assertTrue(Float.isNaN(hidden.baselines().last()));
    }

    @Test
    void compatibilityFactoryPopulatesRustOutputFields() {
        FloatSize size = new FloatSize(100f, 40f);
        FloatSize content = new FloatSize(120f, 50f);
        LayoutOutput output = LayoutOutput.fromSizesAndBaselines(
            size,
            content,
            new FloatPoint(Float.NaN, 12f)
        );

        assertEquals(FloatRect.ltrb(0f, 0f, 120f, 50f), output.scrollableOverflowRect());
        assertEquals(12f, output.baselines().first());
        assertTrue(Float.isNaN(output.baselines().last()));
        assertEquals(12f, output.firstBaselines().y);
    }

    @Test
    void fullConstructorPreservesBothBaselinesAndOverflow() {
        Baselines baselines = new Baselines(8f, 31f);
        FloatRect overflow = FloatRect.ltrb(-4f, -2f, 110f, 45f);
        LayoutOutput output = new LayoutOutput(
            new FloatSize(100f, 40f),
            new FloatSize(100f, 40f),
            new FloatPoint(Float.NaN, 8f),
            CollapsibleMarginSet.ZERO,
            CollapsibleMarginSet.ZERO,
            false,
            overflow,
            baselines
        );

        assertEquals(overflow, output.scrollableOverflowRect());
        assertEquals(baselines, output.baselines());
    }
}
