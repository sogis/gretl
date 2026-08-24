package ch.so.agi.gretl.steps.publisher;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Formats operator-facing Publisher log messages without connection credentials. */
final class PublisherLogFormatter {
    String start(String taskName, ResolvedPublisherArgs args) {
        return taskName + ": Publishing " + args.getOutDataIdent() + " from " + sourceKind(args)
                + " (formats: " + formats(args.getOutFormats()) + ")";
    }

    String success(String taskName, ResolvedPublisherArgs args, Duration elapsed) {
        return taskName + ": Published " + args.getOutDataIdent() + " (formats: " + formats(args.getOutFormats())
                + ", duration: " + elapsed.toMillis() / 1000.0 + " s)";
    }

    String plan(List<? extends ch.so.agi.gretl.steps.publisher.operation.Operation> operations) {
        return "Publication plan: " + operations.stream().map(ch.so.agi.gretl.steps.publisher.operation.Operation::getHumanReadableName)
                .collect(Collectors.joining(", "));
    }

    private static String sourceKind(ResolvedPublisherArgs args) {
        return args.getXtfFile_FolderPath() == null ? "database" : "XTF files";
    }

    private static String formats(List<?> formats) {
        return formats.stream().map(Object::toString).collect(Collectors.joining(", ")).toLowerCase(Locale.ROOT);
    }

}
