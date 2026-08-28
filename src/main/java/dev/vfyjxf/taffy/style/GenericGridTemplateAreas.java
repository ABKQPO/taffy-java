package dev.vfyjxf.taffy.style;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A complete grid-area template with application-defined identifiers. */
public class GenericGridTemplateAreas<S> {
    private final List<GenericGridTemplateArea<S>> areas;
    private final int rowCount;
    private final int columnCount;

    public GenericGridTemplateAreas(List<GenericGridTemplateArea<S>> areas, int rowCount, int columnCount) {
        this.areas = List.copyOf(Objects.requireNonNull(areas, "areas"));
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public List<GenericGridTemplateArea<S>> areas() {
        return areas;
    }

    public int rowCount() {
        return rowCount;
    }

    public int columnCount() {
        return columnCount;
    }

    public GridTemplateAreas toGridTemplateAreas(CustomIdentCodec<S> codec) {
        List<GridTemplateArea> runtimeAreas = new ArrayList<>(areas.size());
        for (GenericGridTemplateArea<S> area : areas) {
            runtimeAreas.add(area.toGridTemplateArea(codec));
        }
        return new GridTemplateAreas(runtimeAreas, rowCount, columnCount);
    }
}
