package ch.so.agi.gretl.steps.publisher;

import static ch.so.agi.gretl.steps.publisher.RawPublisherArgsFixtures.publisherArgs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Files;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.in.xtf.regex.XtfByRegex;
import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;
import ch.so.agi.gretl.steps.publisher.util.env.PublisherEnv;
import ch.so.agi.gretl.steps.publisher.util.env.PupDateEnv;

class OpSequenceBuilderTest {
    @TempDir Path tempDir;

    @Test
    void buildsEagerOperationSequenceForXtfPublication() throws Exception {
        RawPublisherArgs raw = publisherArgs().xtfListSource(tempDir.toString(), List.of("north.xtf"))
                .outFormats(List.of(OutputFormat.XTF)).writeMetadata(false).build();
        List<Operation> operations = new OpSequenceBuilder().buildSequence(
                new ResolvedPublisherArgs(raw, PublisherEnv.empty(), new Date()), null, null, tempDir.resolve("cache"));
        assertEquals(List.of("Source xtf/itf list copy", "Validation", "Packer for publication zip's",
                "Metadata folder writer (meta/)", "Target (remote) repo updater"),
                operations.stream().map(Operation::getHumanReadableName).collect(java.util.stream.Collectors.toList()));
    }

    @Test
    void recordsMetadataAfterZippingAndBeforeRemotePromotion() throws Exception {
        RawPublisherArgs raw = publisherArgs().xtfListSource(tempDir.toString(), List.of("north.xtf"))
                .outFormats(List.of(OutputFormat.XTF)).build();
        PublisherEnv env = new PublisherEnv(new PupDateEnv("jdbc:postgresql://metadata", "publication", "user", "pass"),
                null, null, null, null, null, null);
        Connection connection = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class },
                (proxy, method, args) -> null);

        List<Operation> operations = new OpSequenceBuilder().buildSequence(
                new ResolvedPublisherArgs(raw, env, new Date()), null, connection, tempDir.resolve("cache"));

        assertEquals(List.of("Source xtf/itf list copy", "Validation", "Packer for publication zip's",
                "Metadata folder writer (meta/)", "Table metadata writer", "Target (remote) repo updater"),
                operations.stream().map(Operation::getHumanReadableName)
                .collect(java.util.stream.Collectors.toList()));
    }

    @Test
    void createsOneExactRegexOperationAndPartWorkspacePerMatchingXtf() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Files.writeString(sourceDir.resolve("a+b.xtf"), "first");
        Files.writeString(sourceDir.resolve("second.xtf"), "second");

        List<Operation> operations = regexSourceOperations(sourceDir, ".*\\.xtf$");

        assertEquals(2, operations.stream().filter(XtfByRegex.class::isInstance).count());
        executeSourceOperations(operations);
        Path rawRoot = tempDir.resolve("cache").toAbsolutePath().normalize().resolve(PartWorkspace.RAW);
        assertTrue(Files.exists(PartWorkspace.transferRoot(rawRoot, "a+b").resolve("a+b.xtf")));
        assertTrue(Files.exists(PartWorkspace.transferRoot(rawRoot, "second").resolve("second.xtf")));
    }

    @Test
    void preservesUppercaseTransferExtensionForRegexPublication() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Files.writeString(sourceDir.resolve("UPPER.XTF"), "content");

        List<Operation> operations = regexSourceOperations(sourceDir, ".*");

        executeSourceOperations(operations);
        Path rawRoot = tempDir.resolve("cache").toAbsolutePath().normalize().resolve(PartWorkspace.RAW);
        assertTrue(Files.exists(PartWorkspace.transferRoot(rawRoot, "UPPER").resolve("UPPER.XTF")));
    }

    @Test
    void supportsMixedXtfAndItfRegexMatches() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Files.writeString(sourceDir.resolve("one.xtf"), "xtf");
        Files.writeString(sourceDir.resolve("two.ITF"), "itf");

        List<Operation> operations = regexSourceOperations(sourceDir, ".*\\.(xtf|ITF)$");

        assertEquals(2, operations.stream().filter(XtfByRegex.class::isInstance).count());
        executeSourceOperations(operations);
        Path rawRoot = tempDir.resolve("cache").toAbsolutePath().normalize().resolve(PartWorkspace.RAW);
        assertTrue(Files.exists(PartWorkspace.transferRoot(rawRoot, "one").resolve("one.xtf")));
        assertTrue(Files.exists(PartWorkspace.transferRoot(rawRoot, "two").resolve("two.ITF")));
    }

    @Test
    void rejectsRegexMatchesWithDuplicatePublicationPartIdentifiers() throws Exception {
        Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        Files.writeString(sourceDir.resolve("same.xtf"), "xtf");
        Files.writeString(sourceDir.resolve("same.itf"), "itf");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> regexSourceOperations(sourceDir, ".*\\.(xtf|itf)$"));

        assertTrue(exception.getMessage().contains("multiple transfer files for publication part <same>"));
    }

    @Test
    void rejectsRegexWithoutTransferMatchesOrWithNonTransferMatches() throws Exception {
        Path emptySource = Files.createDirectory(tempDir.resolve("empty-source"));
        Files.writeString(emptySource.resolve("note.txt"), "note");
        IllegalArgumentException noMatch = assertThrows(IllegalArgumentException.class,
                () -> regexSourceOperations(emptySource, ".*\\.xtf$"));
        assertTrue(noMatch.getMessage().contains("did not match any files"));

        Path mixedSource = Files.createDirectory(tempDir.resolve("mixed-source"));
        Files.writeString(mixedSource.resolve("data.xtf"), "data");
        Files.writeString(mixedSource.resolve("note.txt"), "note");
        IllegalArgumentException nonTransfer = assertThrows(IllegalArgumentException.class,
                () -> regexSourceOperations(mixedSource, ".*"));
        assertTrue(nonTransfer.getMessage().contains("matched non-transfer file <note.txt>"));
    }

    private List<Operation> regexSourceOperations(Path sourceDir, String regex) throws Exception {
        RawPublisherArgs raw = publisherArgs().xtfRegexSource(sourceDir.toString(), regex)
                .outFormats(List.of(OutputFormat.XTF)).writeMetadata(false).build();
        return new OpSequenceBuilder().buildSequence(new ResolvedPublisherArgs(raw, PublisherEnv.empty(), new Date()),
                null, null, tempDir.resolve("cache"));
    }

    private static void executeSourceOperations(List<Operation> operations) throws Exception {
        for (Operation operation : operations) {
            if (operation instanceof XtfByRegex) {
                operation.execute();
            }
        }
    }
}
