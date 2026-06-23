package ch.so.agi.gretl.steps.publisher.out.updateremote.stage;

import ch.so.agi.gretl.util.Grooming;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public final class StagePreparer {
    public RemoteStagePaths prepare(Path remoteTargetRoot, String dataIdent, Date publishDate) throws IOException {
        Path checkedRemoteTargetRoot = Objects.requireNonNull(remoteTargetRoot, "remoteTargetRoot");
        String checkedDataIdent = requireText(dataIdent, "dataIdent");
        Date checkedPublishDate = Objects.requireNonNull(publishDate, "publishDate");

        Path dataRoot = checkedRemoteTargetRoot.resolve(checkedDataIdent);
        Files.createDirectories(dataRoot);

        Path tempStageRoot = checkedRemoteTargetRoot.resolve("." + dateTag(checkedPublishDate) + "-" + UUID.randomUUID());
        Files.createDirectories(tempStageRoot);

        return new RemoteStagePaths(dataRoot, tempStageRoot);
    }

    private static String dateTag(Date date) {
        return Grooming.getDateFormat().format(date);
    }

    private static String requireText(String value, String name) {
        if (value == null) {
            throw new NullPointerException(name);
        }
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
