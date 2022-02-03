package ch.so.agi.gretl.steps;

import java.nio.file.Path;

public class PublisherStepLocalTest extends AbstractPublisherStepTest {
    public PublisherStepLocalTest() {
        super();
    }
    @Override
    protected Path getTargetPath() {
        return localTestOut;
    }
}
