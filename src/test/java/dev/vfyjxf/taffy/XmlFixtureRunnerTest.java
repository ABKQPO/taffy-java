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
}
