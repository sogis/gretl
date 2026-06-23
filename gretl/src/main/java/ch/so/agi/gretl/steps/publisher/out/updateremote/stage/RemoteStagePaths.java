package ch.so.agi.gretl.steps.publisher.out.updateremote.stage;

import java.nio.file.Path;
import java.util.Objects;

public final class RemoteStagePaths {
    private final Path dataRoot;
    private final Path tempStageRoot;

    public RemoteStagePaths(Path dataRoot, Path tempStageRoot) {
        this.dataRoot = Objects.requireNonNull(dataRoot, "dataRoot");
        this.tempStageRoot = Objects.requireNonNull(tempStageRoot, "tempStageRoot");
    }

    public Path getDataRoot() {
        return dataRoot;
    }

    public Path getTempStageRoot() {
        return tempStageRoot;
    }
}
