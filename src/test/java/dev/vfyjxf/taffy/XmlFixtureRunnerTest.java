package dev.vfyjxf.taffy;

import dev.vfyjxf.taffy.xml.XmlFixtureRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class XmlFixtureRunnerTest {
    @Test
    void runsAnUpstreamStyleFixtureFromXml() throws IOException {
        Path fixture = Files.createTempFile("taffy-xml-fixture", ".xml");
        Files.writeString(fixture, """
            <test name="simple" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input><div display="flex" width="100px" height="20px"><div width="40px" height="20px"/></div></input>
              <expectations><node x="0" y="0" width="100" height="20"><node x="0" y="0" width="40" height="20"/></node></expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void runsGridTemplateAndPlacementFromXml() throws IOException {
        Path fixture = Files.createTempFile("taffy-grid-xml-fixture", ".xml");
        Files.writeString(fixture, """
            <test name="grid" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input><div display="grid" width="100px" height="20px" grid-template-columns="40px 60px"><div grid-column-start="2" width="60px" height="20px"/></div></input>
              <expectations><node x="0" y="0" width="100" height="20" resolved-rows="20.0000px" resolved-columns="40.0000px 60.0000px"><node x="40" y="0" width="60" height="20"/></node></expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void appliesInsetsForAnUpstreamFixtureWithoutAnExplicitPosition() throws IOException {
        Path fixture = Files.createTempFile("taffy-relative-inset", ".xml");
        Files.writeString(fixture, """
            <test name="relative-inset" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input><div display="block" width="50px"><div height="10px" top="4px" left="2px"/></div></input>
              <expectations><node x="0" y="0" width="50" height="10"><node x="2" y="4" width="50" height="10"/></node></expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void runsAnUpstreamGridAutoTrackFixtureFromXml() throws IOException {
        Path fixture = Files.createTempFile("taffy-grid-auto-track", ".xml");
        Files.writeString(fixture, """
            <test name="grid-auto-track" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input><div display="grid" grid-auto-flow="column" grid-template-rows="10px" grid-template-columns="40px" grid-auto-columns="20px"><div/><div/></div></input>
              <expectations><node x="0" y="0" width="60" height="10"><node x="0" y="0" width="40" height="10"/><node x="40" y="0" width="20" height="10"/></node></expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void deductsGapsBeforeDistributingMaxContentAcrossSpanningTracks() throws IOException {
        Path fixture = Files.createTempFile("taffy-grid-spanning-max-content-gap", ".xml");
        Files.writeString(fixture, """
            <test name="grid-spanning-max-content-gap" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input>
                <div display="grid" column-gap="20px" grid-template-rows="40px" grid-template-columns="40px max-content max-content">
                  <div/>
                  <text grid-column-start="span 2">HH​HH​HH</text>
                </div>
              </input>
              <expectations>
                <node x="0" y="0" width="120" height="40" resolved-columns="40px 20px 20px">
                  <node x="0" y="0" width="40" height="40"/>
                  <node x="60" y="0" width="60" height="40"/>
                </node>
              </expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void keepsMinmaxZeroFlexTracksAtTheirFlexAllocation() throws IOException {
        Path fixture = Files.createTempFile("taffy-grid-minmax-zero-flex", ".xml");
        Files.writeString(fixture, """
            <test name="grid-minmax-zero-flex" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input>
                <div display="grid" width="50px" height="50px" border-top="3px" border-left="3px" border-bottom="3px" border-right="3px" grid-template-rows="minmax(0px, 1fr)" grid-template-columns="minmax(0px, 1fr)">
                  <div width="100px" height="100px" grid-row-start="1" grid-row-end="span 1" grid-column-start="1" grid-column-end="span 1"/>
                  <div grid-row-start="1" grid-column-start="1"/>
                </div>
              </input>
              <expectations>
                <node x="0" y="0" width="50" height="50" resolved-rows="44px" resolved-columns="44px">
                  <node x="3" y="3" width="100" height="100"/>
                  <node x="3" y="3" width="44" height="44"/>
                </node>
              </expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void doesNotMaximizeSubunitMinmaxFlexTracks() throws IOException {
        Path fixture = Files.createTempFile("taffy-grid-minmax-subunit-flex", ".xml");
        Files.writeString(fixture, """
            <test name="grid-minmax-subunit-flex" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input>
                <div display="grid" width="50px" height="50px" border-top="3px" border-left="3px" border-bottom="3px" border-right="3px" grid-template-rows="minmax(0px, 0.5fr)" grid-template-columns="minmax(0px, 0.5fr)">
                  <div width="100px" height="100px" grid-row-start="1" grid-row-end="span 1" grid-column-start="1" grid-column-end="span 1"/>
                  <div grid-row-start="1" grid-column-start="1"/>
                </div>
              </input>
              <expectations>
                <node x="0" y="0" width="50" height="50" resolved-rows="22px" resolved-columns="22px">
                  <node x="3" y="3" width="100" height="100"/>
                  <node x="3" y="3" width="22" height="22"/>
                </node>
              </expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void growsMinmaxZeroAutoTracksForAnIntrinsicSpanningTextItem() throws IOException {
        Path fixture = Files.createTempFile("taffy-grid-intrinsic-auto", ".xml");
        Files.writeString(fixture, """
            <test name="grid-intrinsic-auto" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input>
                <div display="grid" direction="ltr" width="120px" height="120px" border-top="3px" border-left="3px" border-bottom="3px" border-right="3px" grid-template-rows="20px minmax(0px, auto)" grid-template-columns="20px minmax(0px, auto)">
                  <text direction="ltr" min-width="12px" min-height="12px" grid-row-start="1" grid-row-end="span 2" grid-column-start="1" grid-column-end="span 2">XXX​XX​X</text>
                  <div direction="ltr" grid-row-start="1" grid-column-start="1"/>
                  <div direction="ltr" grid-row-start="2" grid-column-start="2"/>
                </div>
              </input>
              <expectations>
                <node x="0" y="0" width="120" height="120" resolved-rows="20px 94px" resolved-columns="20px 94px">
                  <node x="3" y="3" width="114" height="114"/>
                  <node x="3" y="3" width="20" height="20"/>
                  <node x="23" y="23" width="94" height="94"/>
                </node>
              </expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void capsAnIntrinsicSpanningTextItemAtAMinmaxAutoFixedTrack() throws IOException {
        Path fixture = Files.createTempFile("taffy-grid-intrinsic-auto-fixed", ".xml");
        Files.writeString(fixture, """
            <test name="grid-intrinsic-auto-fixed" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input>
                <div display="grid" direction="ltr" width="120px" height="120px" border-top="3px" border-left="3px" border-bottom="3px" border-right="3px" grid-template-rows="20px minmax(auto, 30px)" grid-template-columns="20px minmax(auto, 30px)">
                  <text direction="ltr" min-width="12px" min-height="12px" grid-row-start="1" grid-row-end="span 2" grid-column-start="1" grid-column-end="span 2">XXX​XX​X</text>
                  <div direction="ltr" grid-row-start="1" grid-column-start="1"/>
                  <div direction="ltr" grid-row-start="2" grid-column-start="2"/>
                </div>
              </input>
              <expectations>
                <node x="0" y="0" width="120" height="120" resolved-rows="20px 30px" resolved-columns="20px 30px">
                  <node x="3" y="3" width="50" height="50"/>
                  <node x="3" y="3" width="20" height="20"/>
                  <node x="23" y="23" width="30" height="30"/>
                </node>
              </expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void resolvesIntrinsicMaximumGrowthLimitsForSpanningFixedMinimumRows() throws IOException {
        Path fixture = Files.createTempFile("taffy-grid-intrinsic-maximum", ".xml");
        Files.writeString(fixture, """
            <test name="grid-intrinsic-maximum" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input>
                <div display="grid" direction="ltr" width="120px" height="120px" border-top="3px" border-left="3px" border-bottom="3px" border-right="3px" grid-template-rows="minmax(0px, max-content) minmax(0px, max-content)" grid-template-columns="minmax(0px, max-content) minmax(0px, max-content)">
                  <text direction="ltr" min-width="12px" min-height="12px" grid-row-start="1" grid-row-end="span 2" grid-column-start="1" grid-column-end="span 2">XXX​XX​X</text>
                  <div direction="ltr" grid-row-start="1" grid-column-start="1"/>
                  <div direction="ltr" grid-row-start="2" grid-column-start="2"/>
                </div>
              </input>
              <expectations>
                <node x="0" y="0" width="120" height="120" resolved-rows="6px 6px" resolved-columns="30px 30px">
                  <node x="3" y="3" width="60" height="12"/>
                  <node x="3" y="3" width="30" height="6"/>
                  <node x="33" y="9" width="30" height="6"/>
                </node>
              </expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void limitsSpanningAutomaticMinimumTracksAtTheirFixedMaximum() throws IOException {
        Path fixture = Files.createTempFile("taffy-grid-auto-fixed-span", ".xml");
        Files.writeString(fixture, """
            <test name="grid-auto-fixed-span" use-rounding="true">
              <viewport width="max-content" height="max-content"/>
              <input>
                <div display="grid" direction="ltr" width="120px" height="120px" border-top="3px" border-left="3px" border-bottom="3px" border-right="3px" grid-template-rows="minmax(auto, 10px) minmax(auto, 10px)" grid-template-columns="minmax(auto, 10px) minmax(auto, 10px)">
                  <text direction="ltr" min-width="12px" min-height="12px" grid-row-start="1" grid-row-end="span 2" grid-column-start="1" grid-column-end="span 2">XXX​XX​X</text>
                  <div direction="ltr" grid-row-start="1" grid-column-start="1"/>
                  <div direction="ltr" grid-row-start="2" grid-column-start="2"/>
                </div>
              </input>
              <expectations>
                <node x="0" y="0" width="120" height="120" resolved-rows="10px 10px" resolved-columns="10px 10px">
                  <node x="3" y="3" width="20" height="20"/>
                  <node x="3" y="3" width="10" height="10"/>
                  <node x="13" y="13" width="10" height="10"/>
                </node>
              </expectations>
            </test>
            """);

        try {
            assertDoesNotThrow(() -> XmlFixtureRunner.run(fixture));
        } finally {
            Files.deleteIfExists(fixture);
        }
    }
}
