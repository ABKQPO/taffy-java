package dev.vfyjxf.taffy.style;

import dev.vfyjxf.taffy.geometry.TaffySize;

/** Style contract required from a Flexbox container by the low-level API. */
public interface FlexboxContainerStyle extends CoreStyle {
    default FlexDirection getFlexDirection() { return toTaffyStyle().getFlexDirection(); }
    default FlexWrap getFlexWrap() { return toTaffyStyle().getFlexWrap(); }
    default int getFlexLineCount() { return toTaffyStyle().getFlexLineCount(); }
    default AlignItems getAlignItems() { return toTaffyStyle().getAlignItems(); }
    default JustifyContent getJustifyContent() { return toTaffyStyle().getJustifyContent(); }
}
