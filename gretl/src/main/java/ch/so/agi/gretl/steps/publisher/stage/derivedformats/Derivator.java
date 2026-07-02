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

public final class Derivator implements Operation<DerivatorParameters> {
    @Override
    public void execute(DerivatorParameters operationParameters) {
        deriveAllTransferFiles(operationParameters);
    }

    void deriveAllTransferFiles(DerivatorParameters operationParameters) {
        for (Path transferFile : findTransferFiles(operationParameters)) {
            new SingleTransferDerivator(transferFile, operationParameters.getRequestedFormats()).derive();
        }
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
