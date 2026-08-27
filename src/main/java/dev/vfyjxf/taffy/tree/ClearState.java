package dev.vfyjxf.taffy.tree;

/**
 * Describes the outcome of clearing a layout cache.
 */
public enum ClearState {
    /** Cache entries were removed. */
    CLEARED,

    /** The cache had no entries before the clear operation. */
    ALREADY_EMPTY
}
