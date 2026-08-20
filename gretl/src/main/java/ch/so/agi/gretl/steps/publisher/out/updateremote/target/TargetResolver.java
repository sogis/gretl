package ch.so.agi.gretl.steps.publisher.out.updateremote.target;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Validates a publication target path that has already been resolved by the
 * caller.
 */
public final class TargetResolver {
    public Path resolve(Path target) {
        return Objects.requireNonNull(target, "target must not be null").toAbsolutePath().normalize();
    }
}
