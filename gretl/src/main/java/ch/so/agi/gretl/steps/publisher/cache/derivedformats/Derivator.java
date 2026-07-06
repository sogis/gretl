package ch.so.agi.gretl.steps.publisher.cache.derivedformats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Derivator {
    private final List<DerivedFormat> requestedFormats;
    private final Path cacheDir;

    public Derivator(List<DerivedFormat> requestedFormats, Path cacheDir) {
        Objects.requireNonNull(requestedFormats, "requestedFormats must not be null");
        Objects.requireNonNull(cacheDir, "cacheDir must not be null");
        this.requestedFormats = new ArrayList<>(requestedFormats);
        this.cacheDir = cacheDir.toAbsolutePath().normalize();
    }

    public void deriveAllTransferFiles() {
        for (Path transferFile : findTransferFiles()) {
            new SingleTransferDerivator(transferFile, requestedFormats).derive();
        }
    }

    private List<Path> findTransferFiles() {
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
