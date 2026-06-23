package ch.so.agi.gretl.steps.publisher;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.api.Endpoint;

class PublisherStepConstructionTest {
    @Test
    void rejectsNullParameters() {
        assertThrows(NullPointerException.class, () -> new PublisherStep(null));
    }

    @Test
    void acceptsDbParameters() {
        PublisherStepParameters parameters = baseBuilder()
                .dbDatabase(new Connector("jdbc:postgresql://localhost:5432/postgres"))
                .dbSchema("live")
                .dbIliIdentType("dataset")
                .dbIliIdentValues(List.of("ch.so.agi.alpha"))
                .build();

        PublisherStep step = new PublisherStep(parameters);

        assertSame(parameters, step.getParameters());
    }

    @Test
    void acceptsXtfParameters() {
        PublisherStepParameters parameters = baseBuilder()
                .xtfFilePath("data.xtf")
                .build();

        PublisherStep step = new PublisherStep(parameters);

        assertSame(parameters, step.getParameters());
    }

    private static PublisherStepParameters.Builder baseBuilder() {
        return PublisherStepParameters.builder()
                .outBasePath(new Endpoint("/tmp/publisher-target"))
                .outDataIdent("ch.so.agi.alpha");
    }
}
