package ch.so.agi.gretl.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.File;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivedFormat;

class PublisherTest {
    @Test
    void acceptsCanonicalDbAndOutputDslValues() {
        Project project = ProjectBuilder.builder().build();
        Publisher task = project.getTasks().create("publish", Publisher.class);

        task.setDbDatabase(List.of("jdbc:postgresql://source/db", "source_user", "source_pass"));
        task.setDbSchema("live");
        task.setDbIliIdent_Type("dataset");
        task.setDbIliIdent_Values(List.of("2401", "2402"));
        task.setDbMergeToSingleXtf(false);
        task.setOutBasePath(List.of("/publication-root"));
        task.setOutDataIdent("ch.so.agi.demo");
        task.setOutWriteToThisLocalFolderOnly(new File("build/local-publication"));
        task.setOutDerivedFormats(List.of("gpkg", DerivedFormat.SHP));

        assertEquals("jdbc:postgresql://source/db", task.getDbDatabase().getDbUri());
        assertEquals(List.of("2401", "2402"), task.getDbIliIdent_Values().get());
        assertEquals("/publication-root", task.getOutBasePath().getUrl());
        assertEquals(List.of(DerivedFormat.GPKG, DerivedFormat.SHP), task.getOutDerivedFormats().get());
        assertEquals(project.file("build/local-publication").toPath().toAbsolutePath().normalize().toString(),
                task.getOutWriteToThisLocalFolderOnly());
    }

    @Test
    void keepsLegacyTaskAsAnExplicitSeparateType() {
        Project project = ProjectBuilder.builder().build();

        Object oldTask = project.getTasks().create("publishOld", PublisherOld.class);

        assertInstanceOf(PublisherOld.class, oldTask);
    }
}
