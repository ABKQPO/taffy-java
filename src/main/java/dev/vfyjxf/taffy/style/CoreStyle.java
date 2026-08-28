package dev.vfyjxf.taffy.style;

/**
 * The style surface required by Taffy's common layout entry points.
 *
 * <p>Applications may implement this interface with their own style storage and materialize the
 * runtime style only when a layout algorithm needs it. This mirrors Rust Taffy's independently
 * implementable low-level style traits while retaining the existing Java runtime representation.</p>
 */
@FunctionalInterface
public interface CoreStyle {
    /** Materializes the immutable view consumed by the layout algorithms. */
    TaffyStyle toTaffyStyle();
}
