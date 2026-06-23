package ch.so.agi.gretl.steps.publisher.out.updateremote;

import ch.so.agi.gretl.steps.publisher.out.updateremote.push.LocalToRemotePusher;
import ch.so.agi.gretl.steps.publisher.out.updateremote.rotate.LatestRotator;
import ch.so.agi.gretl.steps.publisher.out.updateremote.seed.StageSeeder;
import ch.so.agi.gretl.steps.publisher.out.updateremote.stage.RemoteStagePaths;
import ch.so.agi.gretl.steps.publisher.out.updateremote.stage.StagePreparer;
import ch.so.agi.gretl.steps.publisher.out.updateremote.target.TargetResolver;

import java.nio.file.Path;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Objects;

/**
 * Coordinates the remote update flow while keeping filesystem details in the
 * responsibility-specific packages.
 */
public final class RemoteUpdater {
    private final TargetResolver targetResolver;
    private final StagePreparer stagePreparer;
    private final StageSeeder stageSeeder;
    private final LocalToRemotePusher localToRemotePusher;
    private final LatestRotator latestRotator;

    public RemoteUpdater() {
        this(new TargetResolver(), new StagePreparer(), new StageSeeder(), new LocalToRemotePusher(), new LatestRotator());
    }

    RemoteUpdater(TargetResolver targetResolver, StagePreparer stagePreparer, StageSeeder stageSeeder,
            LocalToRemotePusher localToRemotePusher, LatestRotator latestRotator) {
        this.targetResolver = Objects.requireNonNull(targetResolver, "targetResolver");
        this.stagePreparer = Objects.requireNonNull(stagePreparer, "stagePreparer");
        this.stageSeeder = Objects.requireNonNull(stageSeeder, "stageSeeder");
        this.localToRemotePusher = Objects.requireNonNull(localToRemotePusher, "localToRemotePusher");
        this.latestRotator = Objects.requireNonNull(latestRotator, "latestRotator");
    }

    public void update(Path remoteTargetRoot, String dataIdent, Date publishDate, Path localStageRoot) throws Exception {
        Path resolvedTargetRoot = targetResolver.resolve(Objects.requireNonNull(remoteTargetRoot, "remoteTargetRoot"));
        String checkedDataIdent = requireText(dataIdent, "dataIdent");
        Date checkedPublishDate = Objects.requireNonNull(publishDate, "publishDate");
        Path checkedLocalStageRoot = Objects.requireNonNull(localStageRoot, "localStageRoot");

        RemoteStagePaths remoteStagePaths = stagePreparer.prepare(resolvedTargetRoot, checkedDataIdent, checkedPublishDate);
        try {
            stageSeeder.seed(remoteStagePaths.getDataRoot(), remoteStagePaths.getTempStageRoot());
            localToRemotePusher.push(checkedLocalStageRoot, remoteStagePaths.getTempStageRoot());
            latestRotator.rotate(remoteStagePaths.getDataRoot(), remoteStagePaths.getTempStageRoot(), checkedPublishDate);
        } catch (Exception ex) {
            deleteTreeIfExists(remoteStagePaths.getTempStageRoot());
            throw ex;
        }
    }

    private static void deleteTreeIfExists(Path path) throws java.io.IOException {
        if (Files.notExists(path)) {
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws java.io.IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, java.io.IOException exc) throws java.io.IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
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
