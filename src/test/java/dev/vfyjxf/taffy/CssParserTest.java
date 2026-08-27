package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.AlignItemsKeyword;
import dev.vfyjxf.taffy.style.AlignContentKeyword;
import dev.vfyjxf.taffy.style.AlignSelf;
import dev.vfyjxf.taffy.style.BoxSizing;
import dev.vfyjxf.taffy.style.Clear;
import dev.vfyjxf.taffy.style.Contain;
import dev.vfyjxf.taffy.style.CssParser;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import dev.vfyjxf.taffy.style.GridAutoFlow;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.GridTemplateComponent;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.Overflow;
import dev.vfyjxf.taffy.style.TaffyDirection;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyFloat;
import dev.vfyjxf.taffy.style.TextAlign;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.style.GridRepetition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CssParserTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void parsesLengthAndDimensionValuesUsingRustParseGrammar() {
        LengthPercentage percentage = CssParser.parseLengthPercentage("12.5%");
        assertTrue(percentage.isPercent());
        assertEquals(0.125f, percentage.getValue(), EPSILON);
        assertEquals(24f, CssParser.parseLengthPercentage("24px").getValue(), EPSILON);

        TaffyDimension dimension = CssParser.parseDimension("fit-content(30%)");
        assertTrue(dimension.isFitContent());
        assertEquals(0.3f, dimension.getFitContentLimit().getValue(), EPSILON);
        assertTrue(CssParser.parseLengthPercentageAuto("MAX-CONTENT").isMaxContent());
        assertTrue(CssParser.parseDimension("content").isContent());
        assertThrows(IllegalArgumentException.class, () -> CssParser.parseLengthPercentage("24em"));
        assertThrows(IllegalArgumentException.class, () -> CssParser.parseDimension("auto trailing"));
    }

    @Test
    void parsesAvailableSpaceAndGridAutoFlow() {
        assertEquals(AvailableSpace.maxContent(), CssParser.parseAvailableSpace("max-content"));
        assertEquals(18f, CssParser.parseAvailableSpace("18px").getValue(), EPSILON);
        assertEquals(GridAutoFlow.COLUMN_DENSE, CssParser.parseGridAutoFlow("dense column"));
        assertThrows(IllegalArgumentException.class, () -> CssParser.parseAvailableSpace("-1px"));
        assertThrows(IllegalArgumentException.class, () -> CssParser.parseGridAutoFlow("row column"));
    }

    @Test
    void parsesTrackFunctionsAndRepeatComponents() {
        TrackSizingFunction minmax = CssParser.parseTrackSizingFunction("minmax(auto, 2fr)");
        assertTrue(minmax.isMinmax());
        assertTrue(minmax.getMinFunc().isAuto());
        assertEquals(2f, minmax.getMaxFunc().getFrValue(), EPSILON);

        TrackSizingFunction fitContent = CssParser.parseTrackSizingFunction("fit-content(40px)");
        assertTrue(fitContent.isFitContent());
        assertEquals(40f, fitContent.getFitContentArgument().getValue(), EPSILON);

        List<GridTemplateComponent> components = CssParser.parseGridTemplateComponents("40px repeat(2, minmax(10px, 1fr) 20%)");
        assertEquals(2, components.size());
        assertTrue(components.get(1).isRepeat());
        assertEquals(2, components.get(1).getRepeat().getCount());
        assertEquals(2, components.get(1).getRepeat().getTrackCount());
        assertThrows(IllegalArgumentException.class, () -> CssParser.parseTrackSizingFunction("minmax(1fr, auto)"));
    }

    @Test
    void parsesNamedAndNumericGridPlacements() {
        GridPlacement namedLine = CssParser.parseGridPlacement("header -2");
        assertTrue(namedLine.isNamedLine());
        assertEquals("header", namedLine.getLineName());
        assertEquals(-2, namedLine.getNthIndex());

        GridPlacement namedSpan = CssParser.parseGridPlacement("span 3 content");
        assertTrue(namedSpan.isNamedSpan());
        assertEquals("content", namedSpan.getLineName());
        assertEquals(3, namedSpan.getValue());
        assertEquals(12, CssParser.parseGridPlacement("12").getLineNumber());
        assertEquals(0, CssParser.parseGridPlacement("span").getValue());
        assertEquals(0, CssParser.parseGridPlacement("span content").getValue());
        assertEquals(Short.MAX_VALUE, CssParser.parseGridPlacement("40000").getLineNumber());
        assertEquals(Short.MIN_VALUE, CssParser.parseGridPlacement("header -40000").getNthIndex());
        assertThrows(IllegalArgumentException.class, () -> CssParser.parseGridPlacement("span 0"));
        assertThrows(IllegalArgumentException.class, () -> CssParser.parseGridPlacement("auto header"));
    }

    @Test
    void parsesRustKeywordEnumsAndRejectsTrailingTokens() {
        assertEquals(TaffyDisplay.FLOW_ROOT, CssParser.parseDisplay("flow-root"));
        assertEquals(TaffyPosition.ABSOLUTE, CssParser.parsePosition("absolute"));
        assertEquals(BoxSizing.CONTENT_BOX, CssParser.parseBoxSizing("content-box"));
        assertEquals(Overflow.CLIP, CssParser.parseOverflow("clip"));
        assertEquals(TaffyDirection.RTL, CssParser.parseDirection("rtl"));
        assertEquals(TextAlign.CENTER, CssParser.parseTextAlign("-webkit-center"));
        assertEquals(FlexDirection.ROW_REVERSE, CssParser.parseFlexDirection("row-reverse"));
        assertEquals(FlexWrap.BALANCE_REVERSE, CssParser.parseFlexWrap("wrap-reverse balance"));
        assertEquals(AlignItems.CENTER, CssParser.parseAlignItems("center"));
        assertEquals(AlignContent.SPACE_EVENLY, CssParser.parseAlignContent("space-evenly"));
        assertThrows(IllegalArgumentException.class, () -> CssParser.parseDisplay("flex none"));
        assertEquals(AlignItems.SAFE_CENTER, CssParser.parseAlignItems("safe center"));
        assertEquals(AlignItems.SAFE_SELF_END, CssParser.parseAlignItems("SAFE self-end"));
        assertEquals(AlignContent.SAFE_FLEX_START, CssParser.parseAlignContent("safe flex-start"));
        assertEquals(AlignItems.SELF_START, CssParser.parseAlignItems("self-start"));
        assertEquals(AlignItems.CENTER, CssParser.parseAlignItems("unsafe center"));
        assertEquals(AlignContent.SPACE_BETWEEN, CssParser.parseAlignContent("space-between"));
        assertEquals(AlignItemsKeyword.SELF_END, AlignItems.SAFE_SELF_END.keyword());
        assertEquals(AlignContentKeyword.FLEX_START, AlignContent.SAFE_FLEX_START.keyword());
        assertTrue(AlignItems.SAFE_CENTER.isSafe());
        assertTrue(AlignItems.SAFE_SELF_END.isSelfRelative());
        assertEquals(AlignItems.START, AlignItems.SAFE_START.withoutSafety());
        assertEquals(AlignContent.CENTER, AlignContent.SAFE_CENTER.withoutSafety());
        assertThrows(IllegalArgumentException.class, () -> CssParser.parseAlignItems("safe stretch"));
        assertThrows(IllegalArgumentException.class, () -> CssParser.parseAlignContent("unsafe space-between"));
        assertEquals(AlignSelf.SAFE_CENTER, CssParser.parseAlignSelf("safe center"));
        assertEquals(AlignSelf.SELF_END, CssParser.parseJustifySelf("self-end"));
        assertEquals(TaffyFloat.LEFT, CssParser.parseFloat("left"));
        assertEquals(Clear.BOTH, CssParser.parseClear("both"));
        assertEquals(Contain.CONTENT, CssParser.parseContain("layout paint"));
    }

    @Test
    void parsesAutomaticGridRepetitionAndSaturatesRepeatCount() {
        List<GridTemplateComponent> components = CssParser.parseGridTemplateComponents(
                "repeat(auto-fill, 1fr) repeat(auto-fit, minmax(10px, 1fr)) repeat(999999999999, 2px)");
        assertEquals(GridRepetition.RepetitionType.AUTO_FILL, components.get(0).getRepeat().getType());
        assertEquals(GridRepetition.RepetitionType.AUTO_FIT, components.get(1).getRepeat().getType());
        assertEquals(65535, components.get(2).getRepeat().getCount());
    }

    @Test
    void exposesParseEntrypointsOnStyleTypes() {
        assertEquals(TaffyDisplay.GRID, TaffyDisplay.parse("grid"));
        assertEquals(TaffyPosition.RELATIVE, TaffyPosition.parse("relative"));
        assertEquals(BoxSizing.BORDER_BOX, BoxSizing.parse("border-box"));
        assertEquals(Overflow.HIDDEN, Overflow.parse("hidden"));
        assertEquals(TaffyDirection.LTR, TaffyDirection.parse("ltr"));
        assertEquals(FlexDirection.COLUMN, FlexDirection.parse("column"));
        assertEquals(AlignItems.STRETCH, AlignItems.parse("stretch"));
        assertEquals(AlignContent.SPACE_AROUND, AlignContent.parse("space-around"));
        assertEquals(0.25f, LengthPercentage.parse("25%").getValue(), EPSILON);
        assertTrue(LengthPercentageAuto.parse("auto").isAuto());
        assertEquals(AvailableSpace.minContent(), AvailableSpace.parse("min-content"));
        assertEquals(GridAutoFlow.ROW_DENSE, GridAutoFlow.parse("row dense"));
        assertTrue(TrackSizingFunction.parse("2fr").isFr());
        assertTrue(GridPlacement.parse("span 2").isSpan());
        assertTrue(GridTemplateComponent.parse("1fr").isSingle());
        assertEquals(2, GridTemplateComponent.parseList("1fr 2fr").size());
    }
}
