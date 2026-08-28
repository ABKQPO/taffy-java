package dev.vfyjxf.taffy.gentest;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GeneratorOptionsTest {
    @Test
    void resolvesExplicitGeneratorPathsAgainstTheWorkingDirectory() {
        Path workingDirectory = Path.of("workspace").toAbsolutePath();

        GeneratorOptions options = GeneratorOptions.parse(workingDirectory, new String[] {
            "--fixturesRoot", "fixtures",
            "--outputRoot", "generated-tests",
            "--chromeBinary", "chrome/chrome.exe",
            "--chromeDriver", "chrome/chromedriver.exe",
            "--category", "grid"
        });

        assertEquals(workingDirectory.resolve("fixtures"), options.fixturesRoot());
        assertEquals(workingDirectory.resolve("generated-tests"), options.outputRoot());
        assertEquals(workingDirectory.resolve("chrome/chrome.exe"), options.chromeBinary());
        assertEquals(workingDirectory.resolve("chrome/chromedriver.exe"), options.chromeDriver());
        assertEquals("grid", options.categoryFilter());
    }

    @Test
    void rejectsOptionsWithoutAValue() {
        assertThrows(IllegalArgumentException.class, () ->
            GeneratorOptions.parse(Path.of("workspace").toAbsolutePath(), new String[] {"--fixturesRoot"})
        );
    }

    @Test
    void serializesNamedGridPlacementFromCurrentRustFixtureJson() {
        JsonObject placement = new JsonObject();
        placement.addProperty("kind", "named");
        placement.addProperty("value", "content-end -2");

        assertEquals(
            "GridPlacement.parse(\"content-end -2\")",
            TestGenerator.gridPlacementExpression(placement)
        );
    }
}
