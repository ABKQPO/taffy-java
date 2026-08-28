package dev.vfyjxf.taffy.style;

import dev.vfyjxf.taffy.geometry.TaffyPoint;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;

/**
 * The style surface required by Taffy's common layout entry points.
 *
 * <p>Applications may implement this interface with their own style storage and materialize the
 * runtime style only when a layout algorithm needs it. This mirrors Rust Taffy's independently
 * implementable low-level style traits while retaining the existing Java runtime representation.</p>
 */
public interface CoreStyle {
    /** Materializes the immutable view consumed by the layout algorithms. */
    TaffyStyle toTaffyStyle();

    default BoxGenerationMode boxGenerationMode() { return toTaffyStyle().boxGenerationMode(); }
    default boolean isBlock() { return toTaffyStyle().isBlock(); }
    default boolean isCompressibleReplaced() { return toTaffyStyle().getItemIsReplaced(); }
    default BoxSizing getBoxSizing() { return toTaffyStyle().getBoxSizing(); }
    default TaffyDirection getDirection() { return toTaffyStyle().getDirection(); }
    default TaffyPoint<Overflow> getOverflow() { return toTaffyStyle().getOverflow(); }
    default float getScrollbarWidth() { return toTaffyStyle().getScrollbarWidth(); }
    default TaffyPosition getPosition() { return toTaffyStyle().getPosition(); }
    default TaffyRect<LengthPercentageAuto> getInset() { return toTaffyStyle().getInset(); }
    default TaffySize<TaffyDimension> getSize() { return toTaffyStyle().getSize(); }
    default TaffySize<LengthPercentageAuto> getMinSize() { return toTaffyStyle().getMinSize(); }
    default TaffySize<LengthPercentageAuto> getMaxSize() { return toTaffyStyle().getMaxSize(); }
    default float getAspectRatio() { return toTaffyStyle().getAspectRatio(); }
    default TaffyRect<LengthPercentageAuto> getMargin() { return toTaffyStyle().getMargin(); }
    default TaffyRect<LengthPercentage> getPadding() { return toTaffyStyle().getPadding(); }
    default TaffyRect<LengthPercentage> getBorder() { return toTaffyStyle().getBorder(); }
    default Contain getContain() { return toTaffyStyle().getContain(); }
    default AlignContent getAlignContent() { return toTaffyStyle().getAlignContent(); }
    default TaffySize<LengthPercentage> getGap() { return toTaffyStyle().getGap(); }
}
