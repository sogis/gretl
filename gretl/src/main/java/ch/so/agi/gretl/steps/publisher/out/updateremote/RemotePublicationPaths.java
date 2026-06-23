package ch.so.agi.gretl.steps.publisher.out.updateremote;

import java.nio.file.Path;

public final class RemotePublicationPaths {
    public static final String CURRENT_DIR = "aktuell";
    public static final String HISTORY_DIR = "hist";

    private RemotePublicationPaths() {
    }

    public static Path currentRoot(Path dataRoot) {
        return dataRoot.resolve(CURRENT_DIR);
    }

    public static Path historyRoot(Path dataRoot) {
        return dataRoot.resolve(HISTORY_DIR);
    }
}
