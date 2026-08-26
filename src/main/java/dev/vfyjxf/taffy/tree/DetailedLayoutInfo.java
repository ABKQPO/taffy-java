package dev.vfyjxf.taffy.tree;

/** Additional information produced by a layout algorithm. */
public class DetailedLayoutInfo {
    public enum Type {
        NONE,
        GRID
    }

    private static final DetailedLayoutInfo NONE = new DetailedLayoutInfo(Type.NONE, null);

    private final Type type;
    private final DetailedGridInfo grid;

    private DetailedLayoutInfo(Type type, DetailedGridInfo grid) {
        this.type = type;
        this.grid = grid;
    }

    /** Return an empty detail value for nodes without algorithm-specific metadata. */
    public static DetailedLayoutInfo none() {
        return NONE;
    }

    /** Wrap detailed grid metadata. */
    public static DetailedLayoutInfo grid(DetailedGridInfo grid) {
        if (grid == null) return NONE;
        return new DetailedLayoutInfo(Type.GRID, grid);
    }

    public Type type() {
        return type;
    }

    public boolean isGrid() {
        return type == Type.GRID;
    }

    public DetailedGridInfo grid() {
        return grid;
    }

    @Override
    public String toString() {
        return isGrid() ? "DetailedLayoutInfo{grid=" + grid + "}" : "DetailedLayoutInfo{none}";
    }
}
