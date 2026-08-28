package dev.vfyjxf.taffy.style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.math.BigInteger;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffySize;

/** Parses the CSS value subset represented by the public Taffy style classes. */
public class CssParser {
    private CssParser() {
    }

    public static LengthPercentage parseLengthPercentage(String input) {
        Parser parser = new Parser(input);
        LengthPercentage value = parser.lengthPercentage();
        parser.end();
        return value;
    }

    /** Returns the public parser contract for length-percentage values. */
    public static FromCss<LengthPercentage> lengthPercentageParser() {
        return CssParser::parseLengthPercentage;
    }

    /** Returns the public parser contract for length-percentage-auto values. */
    public static FromCss<LengthPercentageAuto> lengthPercentageAutoParser() {
        return CssParser::parseLengthPercentageAuto;
    }

    /** Returns the public parser contract for dimension values. */
    public static FromCss<TaffyDimension> dimensionParser() {
        return CssParser::parseDimension;
    }

    /** Returns the public parser contract for available-space values. */
    public static FromCss<AvailableSpace> availableSpaceParser() {
        return CssParser::parseAvailableSpace;
    }

    public static LengthPercentageAuto parseLengthPercentageAuto(String input) {
        Parser parser = new Parser(input);
        LengthPercentageAuto value = parser.lengthPercentageAuto();
        parser.end();
        return value;
    }

    public static TaffyDimension parseDimension(String input) {
        Parser parser = new Parser(input);
        TaffyDimension value = parser.dimension();
        parser.end();
        return value;
    }

    public static AvailableSpace parseAvailableSpace(String input) {
        Parser parser = new Parser(input);
        AvailableSpace value = parser.availableSpace();
        parser.end();
        return value;
    }

    public static GridAutoFlow parseGridAutoFlow(String input) {
        Parser parser = new Parser(input);
        GridAutoFlow value = parser.gridAutoFlow();
        parser.end();
        return value;
    }

    public static FromCss<GridAutoFlow> gridAutoFlowParser() {
        return CssParser::parseGridAutoFlow;
    }

    public static TrackSizingFunction parseTrackSizingFunction(String input) {
        Parser parser = new Parser(input);
        TrackSizingFunction value = parser.trackSizingFunction();
        parser.end();
        return value;
    }

    public static FromCss<TrackSizingFunction> trackSizingFunctionParser() {
        return CssParser::parseTrackSizingFunction;
    }

    /** Parses one complete CSS Grid minimum track sizing function. */
    public static MinTrackSizingFunction parseMinTrackSizingFunction(String input) {
        Parser parser = new Parser(input);
        MinTrackSizingFunction value = MinTrackSizingFunction.from(parser.trackMinFunction());
        parser.end();
        return value;
    }

    public static FromCss<MinTrackSizingFunction> minTrackSizingFunctionParser() {
        return CssParser::parseMinTrackSizingFunction;
    }

    /** Parses one complete CSS Grid maximum track sizing function. */
    public static MaxTrackSizingFunction parseMaxTrackSizingFunction(String input) {
        Parser parser = new Parser(input);
        MaxTrackSizingFunction value = MaxTrackSizingFunction.from(parser.trackMaxFunction());
        parser.end();
        return value;
    }

    public static FromCss<MaxTrackSizingFunction> maxTrackSizingFunctionParser() {
        return CssParser::parseMaxTrackSizingFunction;
    }

    /** Parses one complete CSS Grid repeat count. */
    public static RepetitionCount parseRepetitionCount(String input) {
        Parser parser = new Parser(input);
        RepetitionCount value = parser.repetitionCount();
        parser.end();
        return value;
    }

    public static FromCss<RepetitionCount> repetitionCountParser() {
        return CssParser::parseRepetitionCount;
    }

    public static List<GridTemplateComponent> parseGridTemplateComponents(String input) {
        Parser parser = new Parser(input);
        List<GridTemplateComponent> value = parser.gridTemplateComponents();
        parser.end();
        return value;
    }

    /** Parse a complete grid-template track list, including outer positional line names. */
    public static GridTemplateTracks parseGridTemplateTracks(String input) {
        Parser parser = new Parser(input);
        GridTemplateTracks value = parser.gridTemplateTracks();
        parser.end();
        return value;
    }

    public static FromCss<GridTemplateTracks> gridTemplateTracksParser() {
        return CssParser::parseGridTemplateTracks;
    }

    /** Parse the track sizes accepted by grid-auto-rows and grid-auto-columns. */
    public static List<TrackSizingFunction> parseGridAutoTracks(String input) {
        Parser parser = new Parser(input);
        List<TrackSizingFunction> value = parser.gridAutoTracks();
        parser.end();
        return value;
    }

    public static FromCss<List<TrackSizingFunction>> gridAutoTracksParser() {
        return CssParser::parseGridAutoTracks;
    }

    /** Parse a CSS {@code grid-template-areas} value. */
    public static GridTemplateAreas parseGridTemplateAreas(String input) {
        if (input == null) throw new ParseError("Grid template areas value must not be null");
        String value = input.trim();
        if (value.equalsIgnoreCase("none")) return null;
        List<String> rows = new ArrayList<>();
        int index = 0;
        while (index < value.length()) {
            while (index < value.length() && Character.isWhitespace(value.charAt(index))) index++;
            if (index == value.length()) break;
            char quote = value.charAt(index++);
            if (quote != '\'' && quote != '"') {
                throw new ParseError("Grid template areas rows must be quoted");
            }
            StringBuilder row = new StringBuilder();
            boolean closed = false;
            while (index < value.length()) {
                char character = value.charAt(index++);
                if (character == '\\') {
                    if (index == value.length()) throw new ParseError("Unterminated grid template area escape");
                    row.append(value.charAt(index++));
                } else if (character == quote) {
                    closed = true;
                    break;
                } else {
                    row.append(character);
                }
            }
            if (!closed) throw new ParseError("Unterminated grid template area row");
            rows.add(row.toString());
        }
        return GridTemplateAreas.fromRows(rows);
    }

    public static FromCss<GridTemplateAreas> gridTemplateAreasParser() {
        return CssParser::parseGridTemplateAreas;
    }

    public static GridPlacement parseGridPlacement(String input) {
        Parser parser = new Parser(input);
        GridPlacement value = parser.gridPlacement();
        parser.end();
        return value;
    }

    public static FromCss<GridPlacement> gridPlacementParser() {
        return CssParser::parseGridPlacement;
    }

    /** Parse a grid placement while retaining the caller's custom identifier type. */
    public static <S> GenericGridPlacement<S> parseGenericGridPlacement(
        String input, CustomIdentCodec<S> codec) {
        return decodeGridPlacement(parseGridPlacement(input), codec);
    }

    public static <S> FromCss<GenericGridPlacement<S>> genericGridPlacementParser(CustomIdentCodec<S> codec) {
        if (codec == null) throw new ParseError("Custom identifier codec must not be null");
        return input -> parseGenericGridPlacement(input, codec);
    }

    /** Parse a complete generic grid-template track list, including positional line names. */
    public static <S> GenericGridTemplateTracks<S> parseGenericGridTemplateTracks(
        String input, CustomIdentCodec<S> codec) {
        GridTemplateTracks runtime = parseGridTemplateTracks(input);
        List<GenericGridTemplateComponent<S>> tracks = new ArrayList<>(runtime.getTracks().size());
        for (GridTemplateComponent component : runtime.getTracks()) {
            tracks.add(decodeGridTemplateComponent(component, codec));
        }
        return new GenericGridTemplateTracks<>(tracks, decodeGroups(runtime.getLineNames(), codec));
    }

    public static <S> FromCss<GenericGridTemplateTracks<S>> genericGridTemplateTracksParser(
        CustomIdentCodec<S> codec) {
        if (codec == null) throw new ParseError("Custom identifier codec must not be null");
        return input -> parseGenericGridTemplateTracks(input, codec);
    }

    private static <S> GenericGridPlacement<S> decodeGridPlacement(
        GridPlacement placement, CustomIdentCodec<S> codec) {
        return switch (placement.getType()) {
            case AUTO -> GenericGridPlacement.auto();
            case LINE -> GenericGridPlacement.line(placement.getValue());
            case NAMED_LINE -> GenericGridPlacement.namedLine(
                codec.decode(placement.getLineName()), placement.getNthIndex());
            case SPAN -> GenericGridPlacement.span(placement.getValue());
            case NAMED_SPAN -> GenericGridPlacement.namedSpan(
                codec.decode(placement.getLineName()), placement.getValue());
        };
    }

    private static <S> GenericGridTemplateComponent<S> decodeGridTemplateComponent(
        GridTemplateComponent component, CustomIdentCodec<S> codec) {
        if (component.isSingle()) return GenericGridTemplateComponent.single(component.getSingle());
        GridRepetition repetition = component.getRepeat();
        List<List<S>> lineNames = decodeGroups(repetition.getLineNames(), codec);
        return switch (repetition.getType()) {
            case COUNT -> GenericGridTemplateComponent.repeat(
                GenericGridRepetition.count(repetition.getCount(), repetition.getTracks(), lineNames));
            case AUTO_FILL -> GenericGridTemplateComponent.repeat(
                GenericGridRepetition.autoFill(repetition.getTracks(), lineNames));
            case AUTO_FIT -> GenericGridTemplateComponent.repeat(
                GenericGridRepetition.autoFit(repetition.getTracks(), lineNames));
        };
    }

    private static <S> List<List<S>> decodeGroups(List<List<String>> groups, CustomIdentCodec<S> codec) {
        List<List<S>> decoded = new ArrayList<>(groups.size());
        for (List<String> group : groups) {
            List<S> names = new ArrayList<>(group.size());
            for (String name : group) {
                names.add(codec.decode(name));
            }
            decoded.add(List.copyOf(names));
        }
        return List.copyOf(decoded);
    }

    public static TaffyDisplay parseDisplay(String input) {
        return keyword(input, TaffyDisplay.class, "display", new String[] {"block", "flow-root", "flex", "grid", "none"});
    }

    public static FromCss<TaffyDisplay> displayParser() {
        return CssParser::parseDisplay;
    }

    /**
     * Parses a set of CSS declarations into a generic style. Property names are ASCII
     * case-insensitive and may use either hyphens or underscores. Unknown properties are
     * rejected so callers do not silently lose layout input.
     */
    public static <S> Style<S> parseStyle(Map<String, String> declarations,
                                          CustomIdentCodec<S> identifierCodec) {
        if (declarations == null) throw new ParseError("Style declarations must not be null");
        if (identifierCodec == null) throw new ParseError("Custom identifier codec must not be null");
        Style<S> style = new Style<>(identifierCodec);
        for (Map.Entry<String, String> declaration : declarations.entrySet()) {
            if (declaration.getKey() == null || declaration.getValue() == null) {
                throw new ParseError("CSS declaration names and values must not be null");
            }
            String property = declaration.getKey().trim().toLowerCase(Locale.ROOT).replace('_', '-');
            String value = declaration.getValue();
            switch (property) {
                case "display" -> style.base().display = parseDisplay(value);
                case "position" -> style.base().position = parsePosition(value);
                case "box-sizing" -> style.base().boxSizing = parseBoxSizing(value);
                case "width" -> style.base().size.width = parseDimension(value);
                case "height" -> style.base().size.height = parseDimension(value);
                case "min-width" -> {
                    TaffySize<LengthPercentageAuto> size = style.base().getMinSize();
                    style.base().setMinSize(new TaffySize<>(parseLengthPercentageAuto(value), size.height));
                }
                case "min-height" -> {
                    TaffySize<LengthPercentageAuto> size = style.base().getMinSize();
                    style.base().setMinSize(new TaffySize<>(size.width, parseLengthPercentageAuto(value)));
                }
                case "max-width" -> {
                    TaffySize<LengthPercentageAuto> size = style.base().getMaxSize();
                    style.base().setMaxSize(new TaffySize<>(parseLengthPercentageAuto(value), size.height));
                }
                case "max-height" -> {
                    TaffySize<LengthPercentageAuto> size = style.base().getMaxSize();
                    style.base().setMaxSize(new TaffySize<>(size.width, parseLengthPercentageAuto(value)));
                }
                case "grid-template-columns" -> style.setGridTemplateColumns(parseGenericGridTemplateTracks(value, identifierCodec));
                case "grid-template-rows" -> style.setGridTemplateRows(parseGenericGridTemplateTracks(value, identifierCodec));
                case "grid-column" -> style.setGridColumn(parseGenericLine(value, identifierCodec));
                case "grid-row" -> style.setGridRow(parseGenericLine(value, identifierCodec));
                case "grid-template-areas" -> style.setGridTemplateAreas(parseGenericAreas(value, identifierCodec));
                case "float" -> style.base().floatMode = parseFloat(value);
                case "clear" -> style.base().clear = parseClear(value);
                case "contain" -> style.base().contain = parseContain(value);
                default -> throw new ParseError("Unsupported CSS property: " + declaration.getKey());
            }
        }
        return style;
    }

    /** Parses declarations using String as the custom identifier type. */
    public static Style<String> parseStyle(Map<String, String> declarations) {
        return parseStyle(declarations, CustomIdentCodec.strings());
    }

    /** Parses a semicolon-delimited declaration list into a generic style. */
    public static <S> Style<S> parseStyle(String declarations, CustomIdentCodec<S> identifierCodec) {
        if (declarations == null) throw new ParseError("Style declarations must not be null");
        Map<String, String> values = new LinkedHashMap<>();
        for (String declaration : declarations.split(";")) {
            String trimmed = declaration.trim();
            if (trimmed.isEmpty()) continue;
            int colon = trimmed.indexOf(':');
            if (colon <= 0 || colon == trimmed.length() - 1) {
                throw new ParseError("CSS declaration must contain a property and value: " + trimmed);
            }
            values.put(trimmed.substring(0, colon).trim(), trimmed.substring(colon + 1).trim());
        }
        return parseStyle(values, identifierCodec);
    }

    /** Returns a parser for semicolon-delimited declarations and caller-defined identifiers. */
    public static <S> FromCss<Style<S>> styleParser(CustomIdentCodec<S> identifierCodec) {
        if (identifierCodec == null) throw new ParseError("Custom identifier codec must not be null");
        return input -> parseStyle(input, identifierCodec);
    }

    private static <S> TaffyLine<GenericGridPlacement<S>> parseGenericLine(
        String input, CustomIdentCodec<S> codec) {
        String[] parts = input.trim().split("\\s*/\\s*", -1);
        if (parts.length != 2) throw new ParseError("Grid line must contain exactly one '/'");
        return new TaffyLine<>(parseGenericGridPlacement(parts[0], codec), parseGenericGridPlacement(parts[1], codec));
    }

    private static <S> GenericGridTemplateAreas<S> parseGenericAreas(
        String input, CustomIdentCodec<S> codec) {
        GridTemplateAreas runtime = parseGridTemplateAreas(input);
        if (runtime == null) return null;
        List<GenericGridTemplateArea<S>> areas = new ArrayList<>();
        for (GridTemplateArea area : runtime.areas()) {
            areas.add(new GenericGridTemplateArea<>(codec.decode(area.getName()), area.getRowStart(),
                area.getRowEnd(), area.getColumnStart(), area.getColumnEnd()));
        }
        return new GenericGridTemplateAreas<>(areas, runtime.rowCount(), runtime.columnCount());
    }

    public static TaffyPosition parsePosition(String input) {
        return keyword(input, TaffyPosition.class, "position", new String[] {"static", "relative", "absolute", "fixed"});
    }

    public static FromCss<TaffyPosition> positionParser() {
        return CssParser::parsePosition;
    }

    public static BoxSizing parseBoxSizing(String input) {
        return keyword(input, BoxSizing.class, "box-sizing", new String[] {"border-box", "content-box"});
    }

    public static FromCss<BoxSizing> boxSizingParser() {
        return CssParser::parseBoxSizing;
    }

    public static Overflow parseOverflow(String input) {
        return keyword(input, Overflow.class, "overflow", new String[] {"visible", "clip", "hidden", "scroll"});
    }

    public static FromCss<Overflow> overflowParser() {
        return CssParser::parseOverflow;
    }

    /** Parse the CSS float property. */
    public static TaffyFloat parseFloat(String input) {
        return TaffyFloat.parse(input);
    }

    public static FromCss<TaffyFloat> floatParser() {
        return CssParser::parseFloat;
    }

    /** Parse the CSS clear property. */
    public static Clear parseClear(String input) {
        return Clear.parse(input);
    }

    public static FromCss<Clear> clearParser() {
        return CssParser::parseClear;
    }

    /** Parse the layout-affecting CSS contain property. */
    public static Contain parseContain(String input) {
        return Contain.parse(input);
    }

    public static FromCss<Contain> containParser() {
        return CssParser::parseContain;
    }

    public static TaffyDirection parseDirection(String input) {
        return keyword(input, TaffyDirection.class, "direction", new String[] {"inherit", "ltr", "rtl"});
    }

    public static FromCss<TaffyDirection> directionParser() {
        return CssParser::parseDirection;
    }

    public static TextAlign parseTextAlign(String input) {
        String value = singleKeyword(input, "text-align");
        return switch (value) {
            case "auto" -> TextAlign.AUTO;
            case "start" -> TextAlign.START;
            case "end" -> TextAlign.END;
            case "left" -> TextAlign.LEFT;
            case "right" -> TextAlign.RIGHT;
            case "center", "-webkit-center", "-moz-center" -> TextAlign.CENTER;
            case "justify" -> TextAlign.JUSTIFY;
            case "justify-all" -> TextAlign.JUSTIFY_ALL;
            case "-webkit-left", "-moz-left" -> TextAlign.LEFT;
            case "-webkit-right", "-moz-right" -> TextAlign.RIGHT;
            default -> throw invalidKeyword("text-align", value);
        };
    }

    public static FromCss<TextAlign> textAlignParser() {
        return CssParser::parseTextAlign;
    }

    public static FlexDirection parseFlexDirection(String input) {
        String value = singleKeyword(input, "flex-direction");
        return switch (value) {
            case "row" -> FlexDirection.ROW;
            case "column" -> FlexDirection.COLUMN;
            case "row-reverse" -> FlexDirection.ROW_REVERSE;
            case "column-reverse" -> FlexDirection.COLUMN_REVERSE;
            default -> throw invalidKeyword("flex-direction", value);
        };
    }

    public static FromCss<FlexDirection> flexDirectionParser() {
        return CssParser::parseFlexDirection;
    }

    public static FlexWrap parseFlexWrap(String input) {
        return FlexWrap.parse(input);
    }

    public static FromCss<FlexWrap> flexWrapParser() {
        return CssParser::parseFlexWrap;
    }

    /** Parse an align-self value using the same alignment grammar as align-items. */
    public static AlignSelf parseAlignSelf(String input) {
        AlignItems value = parseAlignItems(input);
        if (value == AlignItems.AUTO) return AlignSelf.AUTO;
        return switch (value) {
            case START -> AlignSelf.START;
            case END -> AlignSelf.END;
            case FLEX_START -> AlignSelf.FLEX_START;
            case FLEX_END -> AlignSelf.FLEX_END;
            case SELF_START -> AlignSelf.SELF_START;
            case SELF_END -> AlignSelf.SELF_END;
            case CENTER -> AlignSelf.CENTER;
            case BASELINE -> AlignSelf.BASELINE;
            case STRETCH -> AlignSelf.STRETCH;
            case SAFE_START -> AlignSelf.SAFE_START;
            case SAFE_END -> AlignSelf.SAFE_END;
            case SAFE_FLEX_START -> AlignSelf.SAFE_FLEX_START;
            case SAFE_FLEX_END -> AlignSelf.SAFE_FLEX_END;
            case SAFE_SELF_START -> AlignSelf.SAFE_SELF_START;
            case SAFE_SELF_END -> AlignSelf.SAFE_SELF_END;
            case SAFE_CENTER -> AlignSelf.SAFE_CENTER;
            default -> throw new IllegalStateException("Unexpected align-self value: " + value);
        };
    }

    public static FromCss<AlignSelf> alignSelfParser() {
        return CssParser::parseAlignSelf;
    }

    /** Parse a justify-items value. */
    public static AlignItems parseJustifyItems(String input) {
        return parseAlignItems(input);
    }

    public static FromCss<AlignItems> justifyItemsParser() {
        return CssParser::parseJustifyItems;
    }

    /** Parse a justify-self value. */
    public static AlignSelf parseJustifySelf(String input) {
        return parseAlignSelf(input);
    }

    public static FromCss<AlignSelf> justifySelfParser() {
        return CssParser::parseJustifySelf;
    }

    /** Parse a justify-content value. */
    public static AlignContent parseJustifyContent(String input) {
        return parseAlignContent(input);
    }

    public static FromCss<AlignContent> justifyContentParser() {
        return CssParser::parseJustifyContent;
    }

    public static AlignItems parseAlignItems(String input) {
        String[] values = alignmentKeywords(input, "align-items");
        if (values.length == 1) {
            return switch (values[0]) {
                case "auto" -> AlignItems.AUTO;
                case "start" -> AlignItems.START;
                case "end" -> AlignItems.END;
                case "flex-start" -> AlignItems.FLEX_START;
                case "flex-end" -> AlignItems.FLEX_END;
                case "self-start" -> AlignItems.SELF_START;
                case "self-end" -> AlignItems.SELF_END;
                case "center" -> AlignItems.CENTER;
                case "baseline" -> AlignItems.BASELINE;
                case "stretch" -> AlignItems.STRETCH;
                default -> throw invalidKeyword("align-items", values[0]);
            };
        }
        if (values.length == 2 && ("safe".equals(values[0]) || "unsafe".equals(values[0]))) {
            boolean safe = "safe".equals(values[0]);
            return switch (values[1]) {
                case "start" -> safe ? AlignItems.SAFE_START : AlignItems.START;
                case "end" -> safe ? AlignItems.SAFE_END : AlignItems.END;
                case "flex-start" -> safe ? AlignItems.SAFE_FLEX_START : AlignItems.FLEX_START;
                case "flex-end" -> safe ? AlignItems.SAFE_FLEX_END : AlignItems.FLEX_END;
                case "self-start" -> safe ? AlignItems.SAFE_SELF_START : AlignItems.SELF_START;
                case "self-end" -> safe ? AlignItems.SAFE_SELF_END : AlignItems.SELF_END;
                case "center" -> safe ? AlignItems.SAFE_CENTER : AlignItems.CENTER;
                default -> throw invalidKeyword("align-items", values[1]);
            };
        }
        throw new ParseError("Invalid align-items value: " + input);
    }

    public static FromCss<AlignItems> alignItemsParser() {
        return CssParser::parseAlignItems;
    }

    public static AlignContent parseAlignContent(String input) {
        String[] values = alignmentKeywords(input, "align-content");
        if (values.length == 1) {
            return switch (values[0]) {
                case "auto" -> AlignContent.AUTO;
                case "start" -> AlignContent.START;
                case "end" -> AlignContent.END;
                case "flex-start" -> AlignContent.FLEX_START;
                case "flex-end" -> AlignContent.FLEX_END;
                case "center" -> AlignContent.CENTER;
                case "stretch" -> AlignContent.STRETCH;
                case "space-between" -> AlignContent.SPACE_BETWEEN;
                case "space-evenly" -> AlignContent.SPACE_EVENLY;
                case "space-around" -> AlignContent.SPACE_AROUND;
                default -> throw invalidKeyword("align-content", values[0]);
            };
        }
        if (values.length == 2 && ("safe".equals(values[0]) || "unsafe".equals(values[0]))) {
            boolean safe = "safe".equals(values[0]);
            return switch (values[1]) {
                case "start" -> safe ? AlignContent.SAFE_START : AlignContent.START;
                case "end" -> safe ? AlignContent.SAFE_END : AlignContent.END;
                case "flex-start" -> safe ? AlignContent.SAFE_FLEX_START : AlignContent.FLEX_START;
                case "flex-end" -> safe ? AlignContent.SAFE_FLEX_END : AlignContent.FLEX_END;
                case "center" -> safe ? AlignContent.SAFE_CENTER : AlignContent.CENTER;
                default -> throw invalidKeyword("align-content", values[1]);
            };
        }
        throw new ParseError("Invalid align-content value: " + input);
    }

    public static FromCss<AlignContent> alignContentParser() {
        return CssParser::parseAlignContent;
    }

    private static String[] alignmentKeywords(String input, String property) {
        if (input == null) throw new ParseError(property + " value must not be null");
        String value = input.trim().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) throw new ParseError("Invalid " + property + " value: " + input);
        return value.split("\\s+");
    }

    private static String singleKeyword(String input, String property) {
        if (input == null) throw new ParseError(property + " value must not be null");
        String value = input.trim().toLowerCase(Locale.ROOT);
        if (value.isEmpty() || value.indexOf(' ') >= 0 || value.indexOf('\t') >= 0 || value.indexOf('\n') >= 0) {
            throw new ParseError("Invalid " + property + " value: " + input);
        }
        return value;
    }

    private static <E extends Enum<E>> E keyword(String input, Class<E> type, String property, String[] accepted) {
        String value = singleKeyword(input, property);
        for (String candidate : accepted) {
            if (candidate.equals(value)) {
                String enumName = candidate.replace('-', '_').toUpperCase(Locale.ROOT);
                return Enum.valueOf(type, enumName);
            }
        }
        throw invalidKeyword(property, value);
    }

    private static ParseError invalidKeyword(String property, String value) {
        return new ParseError("Unknown " + property + " keyword: " + value);
    }

    private static class Parser {
        private final String input;
        private int index;

        private Parser(String input) {
            if (input == null) throw new ParseError("CSS value must not be null");
            this.input = input;
        }

        private void end() {
            whitespace();
            if (index != input.length()) fail("Unexpected trailing input");
        }

        private LengthPercentage lengthPercentage() {
            String token = token();
            ParsedNumber number = number(token);
            if (number.unit.equals("px")) return LengthPercentage.length(number.value);
            if (number.unit.equals("%")) return LengthPercentage.percent(number.value / 100f);
            fail("Expected px or %");
            return LengthPercentage.ZERO;
        }

        private LengthPercentageAuto lengthPercentageAuto() {
            String value = token();
            String keyword = value.toLowerCase(Locale.ROOT);
            switch (keyword) {
                case "auto": return LengthPercentageAuto.AUTO;
                case "min-content": return LengthPercentageAuto.MIN_CONTENT;
                case "max-content": return LengthPercentageAuto.MAX_CONTENT;
                case "stretch": return LengthPercentageAuto.STRETCH;
                case "fit-content": return LengthPercentageAuto.FIT_CONTENT;
                default:
                    ParsedNumber number = number(value);
                    if (number.unit.equals("px")) return LengthPercentageAuto.length(number.value);
                    if (number.unit.equals("%")) return LengthPercentageAuto.percent(number.value / 100f);
                    fail("Expected a supported length-percentage-auto value");
                    return LengthPercentageAuto.AUTO;
            }
        }

        private TaffyDimension dimension() {
            String value = token();
            String keyword = value.toLowerCase(Locale.ROOT);
            switch (keyword) {
                case "auto": return TaffyDimension.AUTO;
                case "min-content": return TaffyDimension.MIN_CONTENT;
                case "max-content": return TaffyDimension.MAX_CONTENT;
                case "fit-content": return TaffyDimension.FIT_CONTENT;
                case "stretch": return TaffyDimension.STRETCH;
                case "content": return TaffyDimension.CONTENT;
                default:
                    if (keyword.startsWith("fit-content(")) {
                        return TaffyDimension.fitContent(parseFunctionArgument(value, "fit-content"));
                    }
                    ParsedNumber number = number(value);
                    if (number.unit.equals("px")) return TaffyDimension.length(number.value);
                    if (number.unit.equals("%")) return TaffyDimension.percent(number.value / 100f);
                    fail("Expected a supported dimension value");
                    return TaffyDimension.AUTO;
            }
        }

        private AvailableSpace availableSpace() {
            String value = token();
            String keyword = value.toLowerCase(Locale.ROOT);
            if (keyword.equals("min-content")) return AvailableSpace.MIN_CONTENT;
            if (keyword.equals("max-content")) return AvailableSpace.MAX_CONTENT;
            ParsedNumber number = number(value);
            if (number.value < 0f || !(number.unit.isEmpty() || number.unit.equals("px"))) {
                fail("Expected a non-negative length");
            }
            return AvailableSpace.definite(number.value);
        }

        private GridAutoFlow gridAutoFlow() {
            List<String> values = words();
            boolean dense = false;
            String axis = null;
            for (String value : values) {
                String keyword = value.toLowerCase(Locale.ROOT);
                if (keyword.equals("dense")) {
                    if (dense) fail("Duplicate dense keyword");
                    dense = true;
                } else if (keyword.equals("row") || keyword.equals("column")) {
                    if (axis != null) fail("Duplicate grid-auto-flow axis");
                    axis = keyword;
                } else {
                    fail("Unknown grid-auto-flow keyword");
                }
            }
            if (axis == null) return dense ? GridAutoFlow.ROW_DENSE : GridAutoFlow.ROW;
            if (axis.equals("row")) return dense ? GridAutoFlow.ROW_DENSE : GridAutoFlow.ROW;
            return dense ? GridAutoFlow.COLUMN_DENSE : GridAutoFlow.COLUMN;
        }

        private TrackSizingFunction trackSizingFunction() {
            String value = token();
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.startsWith("minmax(")) {
                List<String> args = functionArguments(value, "minmax");
                if (args.size() != 2) fail("minmax() requires two arguments");
                TrackSizingFunction min = new Parser(args.get(0)).trackMinFunction();
                TrackSizingFunction max = new Parser(args.get(1)).trackMaxFunction();
                return TrackSizingFunction.minmax(min, max);
            }
            if (lower.startsWith("fit-content(")) {
                return TrackSizingFunction.fitContent(parseFunctionArgument(value, "fit-content"));
            }
            return trackMaxFunction(value);
        }

        private TrackSizingFunction trackMinFunction() {
            String value = token();
            return trackMinFunction(value);
        }

        private TrackSizingFunction trackMinFunction(String value) {
            String lower = value.toLowerCase(Locale.ROOT);
            switch (lower) {
                case "auto" -> {
                    return TrackSizingFunction.AUTO;
                }
                case "min-content" -> {
                    return TrackSizingFunction.MIN_CONTENT;
                }
                case "max-content" -> {
                    return TrackSizingFunction.MAX_CONTENT;
                }
            }
            ParsedNumber number = number(value);
            if (number.unit.equals("px")) return TrackSizingFunction.fixed(number.value);
            if (number.unit.equals("%")) return TrackSizingFunction.percent(number.value / 100f);
            fail("Invalid min track sizing function");
            return TrackSizingFunction.AUTO;
        }

        private TrackSizingFunction trackMaxFunction() {
            return trackMaxFunction(token());
        }

        private TrackSizingFunction trackMaxFunction(String value) {
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.equals("auto")) return TrackSizingFunction.AUTO;
            if (lower.equals("min-content")) return TrackSizingFunction.MIN_CONTENT;
            if (lower.equals("max-content")) return TrackSizingFunction.MAX_CONTENT;
            if (lower.startsWith("fit-content(")) return TrackSizingFunction.fitContent(parseFunctionArgument(value, "fit-content"));
            ParsedNumber number = number(value);
            if (number.unit.equals("fr")) {
                if (number.value < 0f) fail("fr value must be non-negative");
                return TrackSizingFunction.fr(number.value);
            }
            if (number.unit.equals("px")) return TrackSizingFunction.fixed(number.value);
            if (number.unit.equals("%")) return TrackSizingFunction.percent(number.value / 100f);
            fail("Invalid max track sizing function");
            return TrackSizingFunction.AUTO;
        }

        private RepetitionCount repetitionCount() {
            String value = token().toLowerCase(Locale.ROOT);
            if (value.equals("auto-fill")) return RepetitionCount.autoFill();
            if (value.equals("auto-fit")) return RepetitionCount.autoFit();
            return RepetitionCount.count(parseCount(value));
        }

        private List<GridTemplateComponent> gridTemplateComponents() {
            List<GridTemplateComponent> result = new ArrayList<>();
            while (hasMore()) {
                result.add(gridTemplateComponent(token()));
            }
            if (result.isEmpty()) fail("Grid template must not be empty");
            return result;
        }

        private GridTemplateTracks gridTemplateTracks() {
            List<GridTemplateComponent> tracks = new ArrayList<>();
            List<List<String>> lineNames = new ArrayList<>();
            lineNames.add(lineNames());
            while (hasMore()) {
                tracks.add(gridTemplateComponent(token()));
                lineNames.add(lineNames());
            }
            if (tracks.isEmpty()) fail("Grid template must not be empty");
            return new GridTemplateTracks(tracks, lineNames);
        }

        private List<TrackSizingFunction> gridAutoTracks() {
            List<TrackSizingFunction> tracks = new ArrayList<>();
            while (hasMore()) {
                tracks.add(trackSizingFunction());
            }
            if (tracks.isEmpty()) fail("Grid auto tracks must not be empty");
            return tracks;
        }

        private GridTemplateComponent gridTemplateComponent(String value) {
            String lower = value.toLowerCase(Locale.ROOT);
            if (!lower.startsWith("repeat(")) {
                return GridTemplateComponent.single(new Parser(value).trackSizingFunction());
            }
            List<String> args = functionArguments(value, "repeat");
            if (args.size() != 2) fail("repeat() requires two arguments");
            String repetition = args.get(0).trim().toLowerCase(Locale.ROOT);
            RepeatedTrackList repeatedTracks = new Parser(args.get(1)).repeatedTrackList();
            if (repetition.equals("auto-fill")) {
                return GridTemplateComponent.repeat(
                    GridRepetition.autoFill(repeatedTracks.tracks, repeatedTracks.lineNames));
            }
            if (repetition.equals("auto-fit")) {
                return GridTemplateComponent.repeat(
                    GridRepetition.autoFit(repeatedTracks.tracks, repeatedTracks.lineNames));
            }
            return GridTemplateComponent.repeat(
                GridRepetition.count(parseCount(repetition), repeatedTracks.tracks, repeatedTracks.lineNames));
        }

        private List<TrackSizingFunction> trackList() {
            List<TrackSizingFunction> result = new ArrayList<>();
            while (hasMore()) result.add(trackSizingFunction());
            return result;
        }

        private RepeatedTrackList repeatedTrackList() {
            List<TrackSizingFunction> tracks = new ArrayList<>();
            List<List<String>> lineNames = new ArrayList<>();
            lineNames.add(lineNames());
            while (hasMore()) {
                tracks.add(trackSizingFunction());
                lineNames.add(lineNames());
            }
            if (tracks.isEmpty()) fail("repeat() requires at least one track");
            return new RepeatedTrackList(tracks, lineNames);
        }

        private List<String> lineNames() {
            whitespace();
            if (index >= input.length() || input.charAt(index) != '[') return List.of();
            int start = ++index;
            while (index < input.length() && input.charAt(index) != ']') index++;
            if (index >= input.length()) fail("Unclosed grid line name group");
            String body = input.substring(start, index).trim();
            index++;
            if (body.isEmpty()) return List.of();
            return List.of(body.split("\\s+"));
        }

        private GridPlacement gridPlacement() {
            List<String> values = words();
            if (values.size() == 1 && values.get(0).equalsIgnoreCase("auto")) return GridPlacement.auto();
            boolean span = false;
            BigInteger number = null;
            String name = null;
            for (String value : values) {
                if (value.equalsIgnoreCase("span")) {
                    if (span) fail("Duplicate span keyword");
                    span = true;
                } else if (isInteger(value)) {
                    BigInteger parsed = integerValue(value);
                    if (number != null || parsed.signum() == 0) fail("Invalid grid line number");
                    number = parsed;
                } else {
                    if (name != null) fail("Multiple grid line names");
                    name = value;
                }
            }
            if (span) {
                if (name != null) return GridPlacement.namedSpan(name, saturatingUnsigned(number));
                return GridPlacement.span(saturatingUnsigned(number));
            }
            if (name != null) return number == null ? GridPlacement.namedLine(name) : GridPlacement.namedLine(name, saturatingSigned(number));
            if (number != null) return GridPlacement.line(saturatingSigned(number));
            fail("Invalid grid placement");
            return GridPlacement.auto();
        }

        private int saturatingUnsigned(BigInteger value) {
            if (value == null) return 0;
            return value.max(BigInteger.ZERO).min(BigInteger.valueOf(65535L)).intValue();
        }

        private int saturatingSigned(BigInteger value) {
            return value.max(BigInteger.valueOf(Short.MIN_VALUE)).min(BigInteger.valueOf(Short.MAX_VALUE)).intValue();
        }

        private int parseCount(String value) {
            if (!isInteger(value)) fail("repeat() count must be an integer");
            BigInteger count = integerValue(value);
            if (count.signum() < 1) fail("repeat() count must be positive");
            return count.min(BigInteger.valueOf(65535L)).intValue();
        }

        private LengthPercentage parseFunctionArgument(String value, String function) {
            List<String> args = functionArguments(value, function);
            if (args.size() != 1) fail(function + "() requires one argument");
            return new Parser(args.get(0)).lengthPercentage();
        }

        private List<String> functionArguments(String value, String function) {
            String prefix = function.toLowerCase(Locale.ROOT) + "(";
            if (!value.toLowerCase(Locale.ROOT).startsWith(prefix) || !value.endsWith(")")) fail("Malformed " + function + "() function");
            String body = value.substring(prefix.length(), value.length() - 1);
            List<String> result = splitTopLevel(body, ',');
            result.replaceAll(String::trim);
            return result;
        }

        private List<String> words() {
            whitespace();
            int start = index;
            while (index < input.length()) index++;
            if (start == index) fail("Expected a value");
            String body = input.substring(start, index).trim();
            if (body.isEmpty()) fail("Expected a value");
            String[] values = body.split("\\s+");
            return new ArrayList<>(Arrays.asList(values));
        }

        private String token() {
            whitespace();
            int start = index;
            int depth = 0;
            while (index < input.length()) {
                char character = input.charAt(index);
                if (character == '(') depth++;
                else if (character == ')') {
                    if (depth == 0) fail("Unexpected closing parenthesis");
                    depth--;
                } else if (Character.isWhitespace(character) && depth == 0) {
                    break;
                }
                index++;
            }
            if (depth != 0) fail("Unclosed function");
            if (start == index) fail("Expected a value");
            return input.substring(start, index);
        }

        private boolean hasMore() {
            whitespace();
            return index < input.length();
        }

        private void whitespace() {
            while (index < input.length() && Character.isWhitespace(input.charAt(index))) index++;
        }

        private ParsedNumber number(String value) {
            int unitStart = 0;
            while (unitStart < value.length() && (Character.isDigit(value.charAt(unitStart)) || value.charAt(unitStart) == '.' || value.charAt(unitStart) == '-' || value.charAt(unitStart) == '+')) unitStart++;
            if (unitStart == 0 || unitStart == value.length() && value.charAt(unitStart - 1) == '.') fail("Invalid number");
            try {
                return new ParsedNumber(Float.parseFloat(value.substring(0, unitStart)), value.substring(unitStart).toLowerCase(Locale.ROOT));
            } catch (NumberFormatException exception) {
                fail("Invalid number");
                return new ParsedNumber(0f, "");
            }
        }

        private boolean isInteger(String value) {
            return integerValue(value) != null;
        }

        private BigInteger integerValue(String value) {
            try {
                return new BigInteger(value);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        private void fail(String message) {
            throw new ParseError(message + " in CSS value '" + input + "'");
        }

        private static List<String> splitTopLevel(String value, char separator) {
            List<String> result = new ArrayList<>();
            int depth = 0;
            int start = 0;
            for (int i = 0; i < value.length(); i++) {
                char character = value.charAt(i);
                if (character == '(') depth++;
                else if (character == ')') {
                    depth--;
                    if (depth < 0) throw new ParseError("Unbalanced CSS parentheses");
                } else if (character == separator && depth == 0) {
                    result.add(value.substring(start, i));
                    start = i + 1;
                }
            }
            if (depth != 0) throw new ParseError("Unbalanced CSS parentheses");
            result.add(value.substring(start));
            return result;
        }
    }

    private static class ParsedNumber {
        private final float value;
        private final String unit;

        private ParsedNumber(float value, String unit) {
            this.value = value;
            this.unit = unit;
        }
    }

    private static class RepeatedTrackList {
        private final List<TrackSizingFunction> tracks;
        private final List<List<String>> lineNames;

        private RepeatedTrackList(List<TrackSizingFunction> tracks, List<List<String>> lineNames) {
            this.tracks = tracks;
            this.lineNames = lineNames;
        }
    }
}
