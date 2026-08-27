package dev.vfyjxf.taffy.style;

/**
 * Sets the layout used for the children of this node.
 */
public enum TaffyDisplay {
    /** The children will follow the block layout algorithm */
    BLOCK,

    /** The children follow block layout in an independent formatting context */
    FLOW_ROOT,
    
    /** The children will follow the flexbox layout algorithm */
    FLEX,
    
    /** The children will follow the CSS Grid layout algorithm */
    GRID,
    
    /** The node is hidden, and its children will also be hidden */
    NONE;

    /** The default Display mode */
    public static final TaffyDisplay DEFAULT = FLEX;

    /** Parse a CSS display keyword. */
    public static TaffyDisplay parse(String value) {
        return CssParser.parseDisplay(value);
    }
}
