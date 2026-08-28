package dev.vfyjxf.taffy.style;

/** Style contract required from a Block container by the low-level API. */
public interface BlockContainerStyle extends CoreStyle {
    default TextAlign getTextAlign() { return toTaffyStyle().getTextAlign(); }
}
