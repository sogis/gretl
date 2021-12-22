package ch.so.agi.gretl.steps;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PublisherStepFileTest extends AbstractPublisherStepTest {
    public Path localTestOut = Paths.get("build").resolve("out");
    public PublisherStepFileTest() {
        super();
    }
    @Override
    protected Path getTargetPath() {
        return localTestOut;
    }
}
