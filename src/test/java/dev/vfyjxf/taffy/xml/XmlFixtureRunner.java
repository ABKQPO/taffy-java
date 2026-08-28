package dev.vfyjxf.taffy.xml;

import dev.vfyjxf.taffy.geometry.TaffyPoint;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.geometry.FloatSize;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.CssParser;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.DetailedLayoutInfo;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import dev.vfyjxf.taffy.util.MeasureFunc;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs an upstream Taffy XML fixture against the Java layout tree. */
public class XmlFixtureRunner {
    private static final float EPSILON = 0.1f;
    private static final Pattern TRACK_LIST_TOKEN = Pattern.compile("\\[([^]]*)]|(-?(?:\\d+\\.?\\d*|\\.\\d+))px");

    private XmlFixtureRunner() {
    }

    public static void run(Path fixture) throws IOException, SAXException, ParserConfigurationException {
        Document document = parse(fixture);
        Element test = document.getDocumentElement();
        TaffyTree tree = new TaffyTree();
        if (!Boolean.parseBoolean(attribute(test, "use-rounding", "true"))) {
            tree.disableRounding();
        }

        Element viewport = child(test, "viewport");
        Element input = child(test, "input");
        Element expectations = child(test, "expectations");
        Element inputRoot = firstElement(input);
        Element expectedRoot = firstElement(expectations);
        NodeId root = buildTree(tree, inputRoot, expectedRoot);
        tree.computeLayout(root, new TaffySize<>(
            CssParser.parseAvailableSpace(attribute(viewport, "width", "max-content")),
            CssParser.parseAvailableSpace(attribute(viewport, "height", "max-content"))
        ));
        assertNode(tree, root, expectedRoot, fixture.toString());
    }

    private static NodeId buildTree(TaffyTree tree, Element input, Element expected) {
        List<Element> inputChildren = children(input);
        List<Element> expectedChildren = children(expected);
        if (inputChildren.size() != expectedChildren.size()) {
            throw new IllegalArgumentException("Input and expectation child counts differ");
        }
        List<NodeId> childIds = new ArrayList<>();
        for (int index = 0; index < inputChildren.size(); index++) {
            childIds.add(buildTree(tree, inputChildren.get(index), expectedChildren.get(index)));
        }
        TaffyStyle style = style(input);
        if (childIds.isEmpty() && input.getTagName().equals("text")) {
            boolean vertical = attribute(input, "writing-mode", "horizontal-tb").startsWith("vertical");
            return tree.newLeafWithMeasure(style, ahemTextMeasure(input.getTextContent(), vertical));
        }
        return tree.newWithChildren(style, childIds);
    }

    private static TaffyStyle style(Element element) {
        TaffyStyle style = new TaffyStyle();
        style.display = CssParser.parseDisplay(attribute(element, "display", "flex"));
        style.direction = CssParser.parseDirection(attribute(element, "direction", "inherit"));
        style.position = CssParser.parsePosition(attribute(element, "position", "relative"));
        style.boxSizing = CssParser.parseBoxSizing(attribute(element, "box-sizing", "border-box"));
        style.contain = CssParser.parseContain(attribute(element, "contain", "none"));
        style.floatMode = CssParser.parseFloat(attribute(element, "float", "none"));
        style.clear = CssParser.parseClear(attribute(element, "clear", "none"));
        style.overflow = new TaffyPoint<>(
            CssParser.parseOverflow(attribute(element, "overflow-x", "visible")),
            CssParser.parseOverflow(attribute(element, "overflow-y", "visible"))
        );
        style.scrollbarWidth = number(element, "scrollbar-width", 0f);
        style.size = new TaffySize<>(dimension(element, "width"), dimension(element, "height"));
        style.minSize = new TaffySize<>(lengthPercentageAuto(element, "min-width", "auto"), lengthPercentageAuto(element, "min-height", "auto"));
        style.maxSize = new TaffySize<>(lengthPercentageAuto(element, "max-width", "auto"), lengthPercentageAuto(element, "max-height", "auto"));
        style.inset = rectAuto(element, "top", "right", "bottom", "left", "auto");
        style.margin = rectAuto(element, "margin-top", "margin-right", "margin-bottom", "margin-left", "0px");
        style.padding = rectLength(element, "padding-top", "padding-right", "padding-bottom", "padding-left");
        style.border = rectLength(element, "border-top", "border-right", "border-bottom", "border-left");
        style.gap = new TaffySize<>(lengthPercentage(element, "column-gap", "0px"), lengthPercentage(element, "row-gap", "0px"));
        if (element.hasAttribute("text-align")) style.textAlign = CssParser.parseTextAlign(element.getAttribute("text-align"));
        if (element.hasAttribute("aspect-ratio")) style.aspectRatio = number(element, "aspect-ratio", Float.NaN);
        if (element.hasAttribute("flex-direction")) style.flexDirection = CssParser.parseFlexDirection(element.getAttribute("flex-direction"));
        if (element.hasAttribute("flex-wrap")) style.flexWrap = CssParser.parseFlexWrap(element.getAttribute("flex-wrap"));
        if (element.hasAttribute("align-items")) style.alignItems = CssParser.parseAlignItems(element.getAttribute("align-items"));
        if (element.hasAttribute("align-self")) style.alignSelf = CssParser.parseAlignItems(element.getAttribute("align-self"));
        if (element.hasAttribute("justify-items")) style.justifyItems = CssParser.parseJustifyItems(element.getAttribute("justify-items"));
        if (element.hasAttribute("justify-self")) style.justifySelf = CssParser.parseJustifyItems(element.getAttribute("justify-self"));
        if (element.hasAttribute("align-content")) style.alignContent = CssParser.parseAlignContent(element.getAttribute("align-content"));
        if (element.hasAttribute("justify-content")) style.justifyContent = CssParser.parseJustifyContent(element.getAttribute("justify-content"));
        style.flexGrow = number(element, "flex-grow", 0f);
        style.flexShrink = number(element, "flex-shrink", 1f);
        style.flexBasis = CssParser.parseDimension(attribute(element, "flex-basis", "auto"));
        style.flexLineCount = integer(element, "flex-line-count", 1);
        if (element.hasAttribute("grid-template-rows")) style.setGridTemplateRows(CssParser.parseGridTemplateTracks(element.getAttribute("grid-template-rows")));
        if (element.hasAttribute("grid-template-columns")) style.setGridTemplateColumns(CssParser.parseGridTemplateTracks(element.getAttribute("grid-template-columns")));
        if (element.hasAttribute("grid-auto-rows")) style.gridAutoRows = CssParser.parseGridAutoTracks(element.getAttribute("grid-auto-rows"));
        if (element.hasAttribute("grid-auto-columns")) style.gridAutoColumns = CssParser.parseGridAutoTracks(element.getAttribute("grid-auto-columns"));
        if (element.hasAttribute("grid-auto-flow")) style.gridAutoFlow = CssParser.parseGridAutoFlow(element.getAttribute("grid-auto-flow"));
        style.gridRow = new TaffyLine<>(
            CssParser.parseGridPlacement(attribute(element, "grid-row-start", "auto")),
            CssParser.parseGridPlacement(attribute(element, "grid-row-end", "auto")));
        style.gridColumn = new TaffyLine<>(
            CssParser.parseGridPlacement(attribute(element, "grid-column-start", "auto")),
            CssParser.parseGridPlacement(attribute(element, "grid-column-end", "auto")));
        return style;
    }

    private static void assertNode(TaffyTree tree, NodeId node, Element expected, String fixtureName) {
        Layout layout = tree.getLayout(node);
        assertEquals(number(expected, "x", 0f), layout.location().x, EPSILON, fixtureName + " x");
        assertEquals(number(expected, "y", 0f), layout.location().y, EPSILON, fixtureName + " y");
        assertEquals(number(expected, "width", 0f), layout.size().width, EPSILON, fixtureName + " width");
        assertEquals(number(expected, "height", 0f), layout.size().height, EPSILON, fixtureName + " height");
        if (expected.hasAttribute("scroll_width")) {
            assertEquals(number(expected, "scroll_width", 0f), layout.scrollWidth(), EPSILON, fixtureName + " scroll width");
        }
        if (expected.hasAttribute("scroll_height")) {
            assertEquals(number(expected, "scroll_height", 0f), layout.scrollHeight(), EPSILON, fixtureName + " scroll height");
        }
        DetailedLayoutInfo detailedLayout = tree.getDetailedLayoutInfo(node);
        if (expected.hasAttribute("resolved-rows")) {
            String actualRows = detailedLayout.grid().gridTemplateRows();
            assertTrue(trackListsMatch(expected.getAttribute("resolved-rows"), actualRows), fixtureName + " resolved rows: expected " + expected.getAttribute("resolved-rows") + ", actual " + actualRows);
        }
        if (expected.hasAttribute("resolved-columns")) {
            String actualColumns = detailedLayout.grid().gridTemplateColumns();
            assertTrue(trackListsMatch(expected.getAttribute("resolved-columns"), actualColumns), fixtureName + " resolved columns: expected " + expected.getAttribute("resolved-columns") + ", actual " + actualColumns);
        }
        List<Element> expectedChildren = children(expected);
        List<NodeId> childIds = tree.getChildren(node);
        assertEquals(expectedChildren.size(), childIds.size(), fixtureName + " child count");
        for (int index = 0; index < childIds.size(); index++) {
            assertNode(tree, childIds.get(index), expectedChildren.get(index), fixtureName + " child[" + index + "]");
        }
    }

    private static Document parse(Path fixture) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(fixture.toFile());
    }

    private static Element child(Element parent, String name) {
        for (Element element : children(parent)) if (element.getTagName().equals(name)) return element;
        throw new IllegalArgumentException("Missing XML element: " + name);
    }

    private static Element firstElement(Element parent) {
        List<Element> elements = children(parent);
        if (elements.isEmpty()) throw new IllegalArgumentException("Missing XML element child");
        return elements.get(0);
    }

    private static List<Element> children(Element parent) {
        NodeList nodes = parent.getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);
            if (node instanceof Element element) elements.add(element);
        }
        return elements;
    }

    private static String attribute(Element element, String name, String fallback) {
        return element.hasAttribute(name) ? element.getAttribute(name) : fallback;
    }

    private static float number(Element element, String name, float fallback) {
        return element.hasAttribute(name) ? Float.parseFloat(element.getAttribute(name).replace("px", "")) : fallback;
    }

    private static int integer(Element element, String name, int fallback) {
        return element.hasAttribute(name) ? Integer.parseInt(element.getAttribute(name)) : fallback;
    }

    private static boolean trackListsMatch(String expected, String actual) {
        List<TrackListToken> expectedTokens = parseTrackList(expected);
        List<TrackListToken> actualTokens = parseTrackList(actual);
        if (expectedTokens.size() != actualTokens.size()) return false;
        for (int index = 0; index < expectedTokens.size(); index++) {
            TrackListToken expectedToken = expectedTokens.get(index);
            TrackListToken actualToken = actualTokens.get(index);
            if (expectedToken.size != null && actualToken.size != null) {
                if (Math.abs(expectedToken.size - actualToken.size) >= EPSILON) return false;
            } else if (!expectedToken.names.equals(actualToken.names)) {
                return false;
            }
        }
        return true;
    }

    private static List<TrackListToken> parseTrackList(String value) {
        if (value.trim().equals("none")) return List.of();
        List<TrackListToken> tokens = new ArrayList<>();
        Matcher matcher = TRACK_LIST_TOKEN.matcher(value);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                String names = matcher.group(1).trim();
                tokens.add(new TrackListToken(names.isEmpty() ? List.of() : List.of(names.split("\\s+")), null));
            } else {
                tokens.add(new TrackListToken(null, Float.parseFloat(matcher.group(2))));
            }
        }
        return tokens;
    }

    private record TrackListToken(List<String> names, Float size) {
    }

    private static MeasureFunc ahemTextMeasure(String textContent, boolean vertical) {
        String text = textContent == null ? "" : textContent.trim();
        String[] parts = text.isEmpty() ? new String[0] : text.split("\\u200B", -1);
        int minLineLength = 0;
        int maxLineLength = 0;
        for (String part : parts) {
            minLineLength = Math.max(minLineLength, part.length());
            maxLineLength += part.length();
        }
        int minimum = minLineLength;
        int maximum = maxLineLength;
        return (knownDimensions, availableSpace) -> {
            float knownInline = vertical ? knownDimensions.height : knownDimensions.width;
            float knownBlock = vertical ? knownDimensions.width : knownDimensions.height;
            AvailableSpace inlineSpace = vertical ? availableSpace.height : availableSpace.width;
            float inlineSize = knownInline;
            if (Float.isNaN(inlineSize)) {
                if (inlineSpace.isMinContent()) inlineSize = minimum * 10f;
                else if (inlineSpace.isDefinite()) inlineSize = Math.min(inlineSpace.getValue(), maximum * 10f);
                else inlineSize = maximum * 10f;
            }
            inlineSize = Math.max(inlineSize, minimum * 10f);
            float blockSize = knownBlock;
            if (Float.isNaN(blockSize)) {
                int inlineLineLength = Math.max(1, (int) Math.floor(inlineSize / 10f));
                int lineCount = 1;
                int currentLineLength = 0;
                for (String part : parts) {
                    if (currentLineLength + part.length() > inlineLineLength && currentLineLength > 0) {
                        lineCount++;
                        currentLineLength = part.length();
                    } else {
                        currentLineLength += part.length();
                    }
                }
                blockSize = lineCount * 10f;
            }
            FloatSize computed = vertical
                ? new FloatSize(blockSize, inlineSize)
                : new FloatSize(inlineSize, blockSize);
            return new FloatSize(
                Float.isNaN(knownDimensions.width) ? computed.width : knownDimensions.width,
                Float.isNaN(knownDimensions.height) ? computed.height : knownDimensions.height
            );
        };
    }

    private static TaffyDimension dimension(Element element, String name) {
        return CssParser.parseDimension(attribute(element, name, "auto"));
    }

    private static LengthPercentage lengthPercentage(Element element, String name, String fallback) {
        return CssParser.parseLengthPercentage(attribute(element, name, fallback));
    }

    private static LengthPercentageAuto lengthPercentageAuto(Element element, String name, String fallback) {
        return CssParser.parseLengthPercentageAuto(attribute(element, name, fallback));
    }

    private static TaffyRect<LengthPercentageAuto> rectAuto(Element element, String top, String right, String bottom, String left, String fallback) {
        return TaffyRect.ltrb(lengthPercentageAuto(element, left, fallback), lengthPercentageAuto(element, top, fallback),
            lengthPercentageAuto(element, right, fallback), lengthPercentageAuto(element, bottom, fallback));
    }

    private static TaffyRect<LengthPercentage> rectLength(Element element, String top, String right, String bottom, String left) {
        return TaffyRect.ltrb(lengthPercentage(element, left, "0px"), lengthPercentage(element, top, "0px"),
            lengthPercentage(element, right, "0px"), lengthPercentage(element, bottom, "0px"));
    }
}
