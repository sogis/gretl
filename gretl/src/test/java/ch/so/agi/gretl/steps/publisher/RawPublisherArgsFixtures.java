package ch.so.agi.gretl.steps.publisher;

import java.util.ArrayList;
import java.util.List;

import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;

final class RawPublisherArgsFixtures {
    private static final Endpoint TARGET = new Endpoint("/tmp/publisher-target");
    private static final String DATA_IDENT = "ch.so.agi.demo";

    private RawPublisherArgsFixtures() {
    }

    static RawPublisherArgs.Builder publisherArgs() {
        return RawPublisherArgs.builder().output(TARGET, DATA_IDENT).outFormats(List.of(OutputFormat.XTF));
    }

    static ArrayList<String> list(String... values) {
        return new ArrayList<>(List.of(values));
    }

    static Endpoint target() {
        return TARGET;
    }
}
