package ch.so.agi.gretl.steps.publisher.out.updateremote.target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class TargetResolverTest {
    @Test
    void normalizesToAbsolutePath() {
        Path target = Path.of("build", "..", "publication");

        Path resolved = new TargetResolver().resolve(target);

        assertEquals(target.toAbsolutePath().normalize(), resolved);
    }

    @Test
    void rejectsNullTarget() {
        assertThrows(NullPointerException.class, () -> new TargetResolver().resolve(null));
    }
}
