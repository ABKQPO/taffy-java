package dev.vfyjxf.taffy.style;

import dev.vfyjxf.taffy.geometry.TaffyLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generic style facade matching Rust's {@code Style<S>} custom-identifier capability.
 * Scalar properties remain in {@link TaffyStyle}; generic properties are normalized when a layout tree consumes them.
 */
public class Style<S> implements CoreStyle, FlexboxContainerStyle, FlexboxItemStyle,
    GridContainerStyle, GridItemStyle, BlockContainerStyle, BlockItemStyle {
    private final CustomIdentCodec<S> identifierCodec;
    private final TaffyStyle base;
    private TaffyLine<GenericGridPlacement<S>> gridRow = new TaffyLine<>(GenericGridPlacement.auto(), GenericGridPlacement.auto());
    private TaffyLine<GenericGridPlacement<S>> gridColumn = new TaffyLine<>(GenericGridPlacement.auto(), GenericGridPlacement.auto());
    private List<List<S>> gridTemplateColumnNameGroups = List.of();
    private List<List<S>> gridTemplateRowNameGroups = List.of();
    private GenericGridTemplateAreas<S> gridTemplateAreas;
    private List<GenericGridTemplateComponent<S>> gridTemplateRowsWithRepeat;
    private List<GenericGridTemplateComponent<S>> gridTemplateColumnsWithRepeat;

    public Style(CustomIdentCodec<S> identifierCodec) {
        this(new TaffyStyle(), identifierCodec);
    }

    public Style(TaffyStyle base, CustomIdentCodec<S> identifierCodec) {
        this.base = Objects.requireNonNull(base, "base");
        this.identifierCodec = Objects.requireNonNull(identifierCodec, "identifierCodec");
    }

    /** Creates the default style using String custom identifiers, matching Rust's default style type. */
    public static Style<String> defaultStyle() {
        return new Style<>(CustomIdentCodec.strings());
    }

    /** Creates an independent copy of this generic style. */
    public Style<S> copy() {
        Style<S> copy = new Style<>(base.copy(), identifierCodec);
        copy.setGridRow(gridRow);
        copy.setGridColumn(gridColumn);
        copy.setGridTemplateColumnNameGroups(gridTemplateColumnNameGroups);
        copy.setGridTemplateRowNameGroups(gridTemplateRowNameGroups);
        copy.setGridTemplateAreas(gridTemplateAreas);
        copy.setGridTemplateRowsWithRepeat(gridTemplateRowsWithRepeat());
        copy.setGridTemplateColumnsWithRepeat(gridTemplateColumnsWithRepeat());
        return copy;
    }

    /**
     * Reconstructs the generic identifier view of an existing runtime style.
     * Scalar values remain in the copied runtime style while grid identifiers are decoded through the codec.
     */
    public static <S> Style<S> fromTaffyStyle(TaffyStyle runtime, CustomIdentCodec<S> identifierCodec) {
        Objects.requireNonNull(runtime, "runtime");
        Objects.requireNonNull(identifierCodec, "identifierCodec");
        Style<S> style = new Style<>(runtime.copy(), identifierCodec);
        style.setGridRow(new TaffyLine<>(
            decodePlacement(runtime.getGridRowStart(), identifierCodec),
            decodePlacement(runtime.getGridRowEnd(), identifierCodec)));
        style.setGridColumn(new TaffyLine<>(
            decodePlacement(runtime.getGridColumnStart(), identifierCodec),
            decodePlacement(runtime.getGridColumnEnd(), identifierCodec)));
        style.setGridTemplateColumnNameGroups(decodeGroups(runtime.gridTemplateColumnNameGroups, identifierCodec));
        style.setGridTemplateRowNameGroups(decodeGroups(runtime.gridTemplateRowNameGroups, identifierCodec));
        if (runtime.gridTemplateAreas != null) {
            style.setGridTemplateAreas(decodeAreas(runtime.gridTemplateAreas, identifierCodec));
        }
        if (runtime.gridTemplateRowsWithRepeat != null && !runtime.gridTemplateRowsWithRepeat.isEmpty()) {
            style.setGridTemplateRowsWithRepeat(decodeComponents(runtime.gridTemplateRowsWithRepeat, identifierCodec));
        }
        if (runtime.gridTemplateColumnsWithRepeat != null && !runtime.gridTemplateColumnsWithRepeat.isEmpty()) {
            style.setGridTemplateColumnsWithRepeat(decodeComponents(runtime.gridTemplateColumnsWithRepeat, identifierCodec));
        }
        return style;
    }

    public TaffyStyle base() {
        return base;
    }

    public CustomIdentCodec<S> identifierCodec() {
        return identifierCodec;
    }

    public TaffyLine<GenericGridPlacement<S>> gridRow() {
        return gridRow.copy();
    }

    public void setGridRow(TaffyLine<GenericGridPlacement<S>> gridRow) {
        this.gridRow = Objects.requireNonNull(gridRow, "gridRow").copy();
    }

    public TaffyLine<GenericGridPlacement<S>> gridColumn() {
        return gridColumn.copy();
    }

    public void setGridColumn(TaffyLine<GenericGridPlacement<S>> gridColumn) {
        this.gridColumn = Objects.requireNonNull(gridColumn, "gridColumn").copy();
    }

    public List<List<S>> gridTemplateColumnNameGroups() {
        return copyGroups(gridTemplateColumnNameGroups);
    }

    public void setGridTemplateColumnNameGroups(List<List<S>> groups) {
        gridTemplateColumnNameGroups = copyGroups(groups);
    }

    public List<List<S>> gridTemplateRowNameGroups() {
        return copyGroups(gridTemplateRowNameGroups);
    }

    public void setGridTemplateRowNameGroups(List<List<S>> groups) {
        gridTemplateRowNameGroups = copyGroups(groups);
    }

    public GenericGridTemplateAreas<S> gridTemplateAreas() {
        return gridTemplateAreas;
    }

    public void setGridTemplateAreas(GenericGridTemplateAreas<S> areas) {
        gridTemplateAreas = areas;
    }

    public List<GenericGridTemplateComponent<S>> gridTemplateRowsWithRepeat() {
        return gridTemplateRowsWithRepeat == null ? List.of() : gridTemplateRowsWithRepeat;
    }

    public void setGridTemplateRowsWithRepeat(List<GenericGridTemplateComponent<S>> components) {
        gridTemplateRowsWithRepeat = List.copyOf(Objects.requireNonNull(components, "components"));
    }

    /** Set generic row tracks parsed from a complete grid-template value. */
    public void setGridTemplateRows(GenericGridTemplateTracks<S> tracks) {
        Objects.requireNonNull(tracks, "tracks");
        setGridTemplateRowsWithRepeat(tracks.tracks());
        setGridTemplateRowNameGroups(tracks.lineNames());
    }

    public List<GenericGridTemplateComponent<S>> gridTemplateColumnsWithRepeat() {
        return gridTemplateColumnsWithRepeat == null ? List.of() : gridTemplateColumnsWithRepeat;
    }

    public void setGridTemplateColumnsWithRepeat(List<GenericGridTemplateComponent<S>> components) {
        gridTemplateColumnsWithRepeat = List.copyOf(Objects.requireNonNull(components, "components"));
    }

    /** Set generic column tracks parsed from a complete grid-template value. */
    public void setGridTemplateColumns(GenericGridTemplateTracks<S> tracks) {
        Objects.requireNonNull(tracks, "tracks");
        setGridTemplateColumnsWithRepeat(tracks.tracks());
        setGridTemplateColumnNameGroups(tracks.lineNames());
    }

    public TaffyStyle toTaffyStyle() {
        TaffyStyle runtime = base.copy();
        runtime.gridRow = new TaffyLine<>(gridRow.start.toGridPlacement(identifierCodec), gridRow.end.toGridPlacement(identifierCodec));
        runtime.gridColumn = new TaffyLine<>(gridColumn.start.toGridPlacement(identifierCodec), gridColumn.end.toGridPlacement(identifierCodec));
        runtime.gridTemplateColumnNameGroups = encodeGroups(gridTemplateColumnNameGroups);
        runtime.gridTemplateRowNameGroups = encodeGroups(gridTemplateRowNameGroups);
        runtime.gridTemplateAreas = gridTemplateAreas == null ? null : gridTemplateAreas.toGridTemplateAreas(identifierCodec);
        if (gridTemplateRowsWithRepeat != null) {
            runtime.gridTemplateRows = new ArrayList<>();
            runtime.gridTemplateRowsWithRepeat = encodeComponents(gridTemplateRowsWithRepeat);
            runtime.gridTemplateRowNames = new ArrayList<>();
        }
        if (gridTemplateColumnsWithRepeat != null) {
            runtime.gridTemplateColumns = new ArrayList<>();
            runtime.gridTemplateColumnsWithRepeat = encodeComponents(gridTemplateColumnsWithRepeat);
            runtime.gridTemplateColumnNames = new ArrayList<>();
        }
        return runtime;
    }

    private List<GridTemplateComponent> encodeComponents(List<GenericGridTemplateComponent<S>> components) {
        List<GridTemplateComponent> runtime = new ArrayList<>(components.size());
        for (GenericGridTemplateComponent<S> component : components) {
            runtime.add(component.toGridTemplateComponent(identifierCodec));
        }
        return runtime;
    }

    private static <S> GenericGridPlacement<S> decodePlacement(
        GridPlacement placement, CustomIdentCodec<S> identifierCodec) {
        return switch (placement.getType()) {
            case AUTO -> GenericGridPlacement.auto();
            case LINE -> GenericGridPlacement.line(placement.getValue());
            case NAMED_LINE -> GenericGridPlacement.namedLine(
                identifierCodec.decode(placement.getLineName()), placement.getNthIndex());
            case SPAN -> GenericGridPlacement.span(placement.getValue());
            case NAMED_SPAN -> GenericGridPlacement.namedSpan(
                identifierCodec.decode(placement.getLineName()), placement.getValue());
        };
    }

    private static <S> GenericGridTemplateAreas<S> decodeAreas(
        GridTemplateAreas areas, CustomIdentCodec<S> identifierCodec) {
        List<GenericGridTemplateArea<S>> decoded = new ArrayList<>(areas.areas().size());
        for (GridTemplateArea area : areas.areas()) {
            decoded.add(new GenericGridTemplateArea<>(identifierCodec.decode(area.getName()),
                area.getRowStart(), area.getRowEnd(), area.getColumnStart(), area.getColumnEnd()));
        }
        return new GenericGridTemplateAreas<>(decoded, areas.rowCount(), areas.columnCount());
    }

    private static <S> List<GenericGridTemplateComponent<S>> decodeComponents(
        List<GridTemplateComponent> components, CustomIdentCodec<S> identifierCodec) {
        List<GenericGridTemplateComponent<S>> decoded = new ArrayList<>(components.size());
        for (GridTemplateComponent component : components) {
            if (component.isSingle()) {
                decoded.add(GenericGridTemplateComponent.single(component.getSingle()));
                continue;
            }
            GridRepetition repetition = component.getRepeat();
            List<List<S>> lineNames = decodeGroups(repetition.getLineNames(), identifierCodec);
            GenericGridRepetition<S> generic = switch (repetition.getType()) {
                case COUNT -> GenericGridRepetition.count(repetition.getCount(), repetition.getTracks(), lineNames);
                case AUTO_FILL -> GenericGridRepetition.autoFill(repetition.getTracks(), lineNames);
                case AUTO_FIT -> GenericGridRepetition.autoFit(repetition.getTracks(), lineNames);
            };
            decoded.add(GenericGridTemplateComponent.repeat(generic));
        }
        return decoded;
    }

    private List<List<S>> copyGroups(List<List<S>> groups) {
        Objects.requireNonNull(groups, "groups");
        List<List<S>> copied = new ArrayList<>(groups.size());
        for (List<S> group : groups) {
            copied.add(List.copyOf(Objects.requireNonNull(group, "group")));
        }
        return List.copyOf(copied);
    }

    private List<List<String>> encodeGroups(List<List<S>> groups) {
        List<List<String>> encoded = new ArrayList<>(groups.size());
        for (List<S> group : groups) {
            List<String> names = new ArrayList<>(group.size());
            for (S identifier : group) {
                names.add(identifierCodec.encode(identifier));
            }
            encoded.add(List.copyOf(names));
        }
        return encoded;
    }

    private static <S> List<List<S>> decodeGroups(List<List<String>> groups, CustomIdentCodec<S> identifierCodec) {
        if (groups == null || groups.isEmpty()) return List.of();
        List<List<S>> decoded = new ArrayList<>(groups.size());
        for (List<String> group : groups) {
            List<S> identifiers = new ArrayList<>(group.size());
            for (String identifier : group) {
                identifiers.add(identifierCodec.decode(identifier));
            }
            decoded.add(List.copyOf(identifiers));
        }
        return List.copyOf(decoded);
    }
}
