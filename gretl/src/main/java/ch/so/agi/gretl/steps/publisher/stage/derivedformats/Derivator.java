package ch.so.agi.gretl.steps.publisher.stage.derivedformats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.PartWorkspace;

public final class Derivator implements Operation {
    private DerivatorParameters parameters;
    private int derivedTransferFileCount;

    public Derivator(DerivatorParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "parameters must not be null");
    }
    @Deprecated public Derivator() { }
    @Deprecated public void execute(DerivatorParameters parameters) { this.parameters = parameters; execute(); }

    @Override
    public void execute() {
        derivedTransferFileCount = deriveAllTransferFiles(parameters);
    }

    @Override
    public String getHumanReadableName() { return "Format Derivator (from xtf/itf)"; }

    @Override
    public String getSuccessLogDetail() {
        return "created " + parameters.getRequestedFormats() + " for " + derivedTransferFileCount + " transfer file(s)";
    }

    int deriveAllTransferFiles(DerivatorParameters operationParameters) {
        List<Path> transferFiles = findTransferFiles(operationParameters);
        for (Path transferFile : transferFiles) {
            new SingleTransferDerivator(transferFile, operationParameters.getRequestedFormats(),
                    operationParameters.getCustomModelDir(),
                    PartWorkspace.workRoot(operationParameters.getCacheDir().getParent())).derive();
        }
        return transferFiles.size();
    }

    private List<Path> findTransferFiles(DerivatorParameters operationParameters) {
        Path cacheDir = operationParameters.getCacheDir().toAbsolutePath().normalize();
        try (Stream<Path> stream = Files.walk(cacheDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(this::isTransferFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("failed to inspect cacheDir " + cacheDir, e);
        }
    }

    private boolean isTransferFile(Path file) {
        String lowerName = file.getFileName().toString().toLowerCase();
        return lowerName.endsWith(".xtf") || lowerName.endsWith(".itf");
    }
}
