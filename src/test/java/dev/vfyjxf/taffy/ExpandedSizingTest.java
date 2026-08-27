package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.style.ExpandedDimension;
import dev.vfyjxf.taffy.style.ExpandedLengthPercentage;
import dev.vfyjxf.taffy.style.ExpandedLengthPercentageAuto;
import dev.vfyjxf.taffy.style.ExpandedTrackSizingFunction;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpandedSizingTest {
    @Test
    public void expandsAndReconstructsLengthPercentageValues() {
        LengthPercentage length = LengthPercentage.length(12f);
        LengthPercentage percent = LengthPercentage.percent(0.25f);

        ExpandedLengthPercentage expandedLength = length.expand();
        ExpandedLengthPercentage expandedPercent = percent.expand();

        assertEquals(ExpandedLengthPercentage.Type.LENGTH, expandedLength.getType());
        assertEquals(12f, expandedLength.getValue());
        assertEquals(length, expandedLength.toLengthPercentage());
        assertEquals(ExpandedLengthPercentage.Type.PERCENT, expandedPercent.getType());
        assertEquals(percent, expandedPercent.toLengthPercentage());
    }

    @Test
    public void expandsLengthPercentageAutoIntrinsicValues() {
        LengthPercentageAuto fit = LengthPercentageAuto.fitContent(LengthPercentage.percent(0.5f));
        ExpandedLengthPercentageAuto expanded = fit.expand();

        assertEquals(ExpandedLengthPercentageAuto.Type.FIT_CONTENT, expanded.getType());
        assertEquals(fit, expanded.toLengthPercentageAuto());
        assertEquals(ExpandedLengthPercentageAuto.Type.AUTO, LengthPercentageAuto.AUTO.expand().getType());
    }

    @Test
    public void expandsDimensionAndPreservesContentKeyword() {
        TaffyDimension dimension = TaffyDimension.content();
        ExpandedDimension expanded = dimension.expand();

        assertEquals(ExpandedDimension.Type.CONTENT, expanded.getType());
        assertEquals(dimension, expanded.toDimension());
        assertEquals(TaffyDimension.fitContent(LengthPercentage.length(40f)),
            TaffyDimension.fitContent(LengthPercentage.length(40f)).expand().toDimension());
    }

    @Test
    public void expandsNestedTrackSizingFunctions() {
        TrackSizingFunction track = TrackSizingFunction.minmax(
            TrackSizingFunction.minContent(), TrackSizingFunction.flex(2f));
        ExpandedTrackSizingFunction expanded = track.expand();

        assertEquals(ExpandedTrackSizingFunction.Type.MINMAX, expanded.getType());
        assertEquals(ExpandedTrackSizingFunction.Type.MIN_CONTENT, expanded.getMinFunction().getType());
        assertEquals(2f, expanded.getMaxFunction().getFlexValue());
        assertEquals(track, expanded.toTrackSizingFunction());
    }
}
