# Taffy-Java

A pure Java implementation of the [Taffy](https://github.com/DioxusLabs/taffy) UI layout library, supporting CSS Flexbox, Grid, and Block layout algorithms.

The Java port delivers performance comparable to Rust in standard scenarios,
but experiences a noticeable drop under extreme conditions (based on benchmarks against the Rust version).

It passes the full Taffy test suite (ported to Java).

**This Java port was facilitated by AI.**

## JitPack

Add the JitPack repository and depend on the fork using the normal Maven coordinates:

```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ABKQPO:taffy-java:1.1.4'
}
```

The library is built for the configured modern Java toolchain. Consumers should use a compatible Java runtime; the published artifact keeps the normal Maven dependency metadata.

## Fixture Generation

The optional fixture generator uses the upstream Rust `test_fixtures` directory without assuming a sibling checkout. Point it at any Rust Taffy checkout using a Gradle property or environment variable:

```powershell
.\gradlew.bat :gentest:generateTaffyFixtures -PtaffyFixtureRoot=E:\Github\taffy\test_fixtures
```

Optional properties are `generatedTestRoot`, `taffyFixtureCategory`, `chromeBinary`, and `chromeDriver`. Their environment equivalents are `TAFFY_GENERATED_TEST_ROOT`, `TAFFY_FIXTURE_CATEGORY`, `CHROME_BINARY`, and `CHROME_DRIVER`.

Run upstream XML layout assertions directly with an explicit XML root. Restrict a run to one group while expanding the runner's supported style surface:

```powershell
.\gradlew.bat test -PtaffyXmlRoot=E:\Github\taffy\tests\xml -PtaffyXmlGroup=grid
```

## Credits

- Original [Taffy](https://github.com/DioxusLabs/taffy) library by DioxusLabs
- Python bindings [Stretchable](https://github.com/mortencombat/stretchable) for reference
