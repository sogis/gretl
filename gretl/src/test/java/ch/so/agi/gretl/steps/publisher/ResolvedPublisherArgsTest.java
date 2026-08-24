package ch.so.agi.gretl.steps.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.steps.publisher.util.env.PubFolderEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PublisherEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PupDateEnv;
import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;

class ResolvedPublisherArgsTest {
    @Test
    void appliesGlobalDefaultsAndPreservesExplicitValues() {
        RawPublisherArgs rawArgs = RawPublisherArgs.builder()
                .output(new Endpoint("/explicit-output"), "ch.so.agi.demo")
                .xtfRegexSource("/incoming", ".*\\.xtf$")
                .groomingConfig("/explicit-grooming.json")
                .customModelDir("/explicit-models")
                .outFormats(List.of(OutputFormat.XTF))
                .build();
        Date timestamp = new Date(1_000L);

        ResolvedPublisherArgs resolved = new ResolvedPublisherArgs(rawArgs, environment(), timestamp);

        assertEquals("/explicit-output", resolved.getOutFolderPath().getUrl());
        assertEquals("/explicit-grooming.json", resolved.getOutGroomingConfigFilePath());
        assertEquals("/explicit-models", resolved.getCustomModelDir());
        assertEquals(timestamp, resolved.getPublicationTimestamp());
        assertEquals("jdbc:postgresql://metadata", resolved.getPublicationDatabase().getUrl());
        assertEquals("metadata", resolved.getMetadataSchema());
    }

    @Test
    void usesGlobalDefaultsWhenRawArgumentsDoNotOverrideThem() {
        RawPublisherArgs rawArgs = RawPublisherArgs.builder().output(null, "ch.so.agi.demo")
                .xtfRegexSource("/incoming", ".*\\.xtf$")
                .outFormats(List.of(OutputFormat.XTF))
                .build();

        ResolvedPublisherArgs resolved = new ResolvedPublisherArgs(rawArgs, environment(), new Date());

        assertEquals("sftp://publisher", resolved.getOutFolderPath().getUrl());
        assertEquals("/global-grooming.json", resolved.getOutGroomingConfigFilePath());
        assertEquals("/global-models", resolved.getCustomModelDir());
    }

    private static PublisherEnv environment() {
        return new PublisherEnv(new PupDateEnv("jdbc:postgresql://metadata", "metadata", "user", "password"),
                new PubFolderEnv("sftp://publisher", "user", "password"), "https://jsonmeta", "bucket", "file",
                "/global-models", Path.of("/global-grooming.json"));
    }
}
