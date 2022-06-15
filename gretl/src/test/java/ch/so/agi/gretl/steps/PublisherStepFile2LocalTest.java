package ch.so.agi.gretl.steps;

import java.nio.file.Path;

public class PublisherStepFile2LocalTest extends AbstractPublisherStepTest {
    public PublisherStepFile2LocalTest() {
        super();
    }
    @Override
    protected Path getTargetPath() {
        return localTestOut;
    }
}
