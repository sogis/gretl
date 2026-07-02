package ch.so.agi.gretl.steps.publisher.out.updateremote;

import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.AbstractSingleInputSingleOutputParameters;

public final class RemoteUpdaterParameters extends AbstractSingleInputSingleOutputParameters {
    private final String dataIdent;
    private final Date publishDate;

    private RemoteUpdaterParameters(Path localStageRoot, Path remoteTargetRoot, String dataIdent, Date publishDate) {
        super(localStageRoot, remoteTargetRoot);
        this.dataIdent = requireText(dataIdent, "dataIdent");
        this.publishDate = new Date(Objects.requireNonNull(publishDate, "publishDate must not be null").getTime());
    }

    public static RemoteUpdaterParameters of(Path localStageRoot, Path remoteTargetRoot, String dataIdent,
            Date publishDate) {
        return new RemoteUpdaterParameters(localStageRoot, remoteTargetRoot, dataIdent, publishDate);
    }

    public Path getLocalStageRoot() {
        return getInputDir();
    }

    public Path getRemoteTargetRoot() {
        return getOutputDir();
    }

    public String getDataIdent() {
        return dataIdent;
    }

    public Date getPublishDate() {
        return new Date(publishDate.getTime());
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }
}
